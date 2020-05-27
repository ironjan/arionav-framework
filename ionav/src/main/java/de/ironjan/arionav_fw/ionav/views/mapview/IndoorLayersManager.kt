package de.ironjan.arionav_fw.ionav.views.mapview

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import de.ironjan.arionav_fw.ionav.custom_view_mvvm.ModelDrivenUiComponent
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorData
import de.ironjan.arionav_fw.ionav.model.indoor_map.IndoorWay
import org.oscim.backend.CanvasAdapter
import org.oscim.backend.canvas.Color
import org.oscim.backend.canvas.Paint
import org.oscim.core.GeoPoint
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.layers.vector.VectorLayer
import org.oscim.layers.vector.geometries.PolygonDrawable
import org.oscim.layers.vector.geometries.RectangleDrawable
import org.oscim.layers.vector.geometries.Style
import org.oscim.map.Map
import org.slf4j.LoggerFactory

/**
 * Wrapper around multiple layers that will be added to {@param map}. Not an actual layer
 */
class IndoorLayersManager(private val map: Map, private val density: Float) :
    ModelDrivenUiComponent<IonavViewModel> {

    // region ModelDrivenMapExtension
    override fun observe(viewModel: IonavViewModel, lifecycleOwner: LifecycleOwner) {
        viewModel.indoorData.observe(lifecycleOwner, Observer {
            updateLayers(it)
        })
        viewModel.selectedLevel.observe(lifecycleOwner, Observer {
            selectedLevel = it
        })
    }
    // endregion


    // region callbacks
    var itemTapCallback = defaultTapCallback

    // endregion

    // region data handling


    private fun updateLayers(indoorData: IndoorData) {
        recreateRoomOutlineLayersFrom(indoorData)
        recreateLabelLayersFrom(indoorData)
        showLayersFor(selectedLevel)
    }

    private var selectedLevel = 0.0
        private set(value) {
            if (value == field) return

            showLayersFor(value)

            field = value
        }

    // endregion

    // region layer handling
    private var levelsToRoomBackgroundLayers = prepareRoomBackgroundLayers(IndoorData.empty())

    private fun recreateRoomOutlineLayersFrom(indoorData: IndoorData) {
        levelsToRoomBackgroundLayers = prepareRoomBackgroundLayers(indoorData)
    }

    private var levelsToLabelLayers = prepareLabelLayers(IndoorData.empty())

    private fun recreateLabelLayersFrom(indoorData: IndoorData) {
        levelsToLabelLayers = prepareLabelLayers(indoorData)
    }

    private var currentDrawableLayer: VectorLayer? = null
    private var currentLabelLayer: ItemizedLayer<MarkerItem>? = null

    private fun showLayersFor(newLevel: Double) {
        map.layers().remove(currentDrawableLayer)
        map.layers().remove(currentLabelLayer)

        val newDrawableLayer = levelsToRoomBackgroundLayers[newLevel] ?: return
        val newLabelLayer = levelsToLabelLayers[newLevel] ?: return

        map.layers().add(newDrawableLayer)
        map.layers().add(newLabelLayer)
        currentDrawableLayer = newDrawableLayer
        currentLabelLayer = newLabelLayer
    }

    private fun prepareRoomBackgroundLayers(id: IndoorData): kotlin.collections.Map<Double, VectorLayer> {
        return id.levels.map {
            Pair(it, prepareRoomBackgroundLayer(id, it))
        }.toMap()
    }

    private fun prepareRoomBackgroundLayer(id: IndoorData, level: Double): VectorLayer {
        val nodeDrawables = id.getNodes(level).map {
            RectangleDrawable(it.toGeoPoint(), it.toGeoPoint())
        }


        val roomStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(Color.GRAY)
            .fillColor(Color.DKGRAY)
            .strokeWidth(1 * density)
            .build()
        val corridorStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .fillColor(Color.BLUE)
            .strokeColor(Color.BLUE)
            .strokeWidth(0 * density)
            .build()
        val floorConnectorStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .fillColor(Color.RED)
            .strokeColor(Color.RED)
            .strokeWidth(0 * density)
            .build()

        val indoorWays = id.getWays(level)

        val roomDrawables = indoorWays
            .filter { it.isRoom }
            .filterNot { it.isFloorConnector }
            .filterNot { it.isArea || it.isCorridor }
            .mapNotNull { createOutline(it, roomStyle) }

        val corridorAndAreaDrawables = indoorWays
            .filter { it.isCorridor || it.isArea }
            .filterNot { it.isFloorConnector }
            .filterNot { it.isRoom }
            .mapNotNull { createOutline(it, corridorStyle) }
        val floorConnectorDrawables = indoorWays
            .filter { it.isFloorConnector }
            .mapNotNull { createOutline(it, floorConnectorStyle) }

        val levelLayer = VectorLayer(map)
        val drawables =
            nodeDrawables
                .union(roomDrawables)
                .union(corridorAndAreaDrawables)
                .union(floorConnectorDrawables)
        drawables.map { levelLayer.add(it) }

        return levelLayer
    }

    private fun createOutline(iw: IndoorWay, roomStyle: Style?): PolygonDrawable? {
        return try {
            val points = iw.nodeRefs.map { it.toGeoPoint() }
            val geometry = PolygonDrawable(points)
            geometry.style = roomStyle

            geometry
        } catch (e: Exception) {
            null
        }
    }


    private fun prepareLabelLayers(id: IndoorData): kotlin.collections.Map<Double, ItemizedLayer<MarkerItem>> {
        return id.levels.map {
            Pair(it, prepareLabelLayer(id, it))
        }.toMap()
    }

    private fun prepareLabelLayer(id: IndoorData, level: Double): ItemizedLayer<MarkerItem> {
        val labelLayer = ItemizedLayer<MarkerItem>(map, null as MarkerSymbol?)

        val canvas = CanvasAdapter.newCanvas()
        val paint = CanvasAdapter.newPaint()
        paint.setTypeface(Paint.FontFamily.DEFAULT, Paint.FontStyle.NORMAL)
        paint.setTextSize(12 * CanvasAdapter.getScale())
        paint.strokeWidth = 2 * CanvasAdapter.getScale()
        paint.color = Color.BLACK

        val wayMarkers = id.getWays(level).map {
            if (it.name.isNullOrEmpty()) return@map null

            val bitmap = CanvasAdapter.newBitmap((42 * CanvasAdapter.getScale()).toInt(), (20 * CanvasAdapter.getScale()).toInt(), 0)
            canvas.setBitmap(bitmap)
            canvas.fillColor(Color.GRAY)

            canvas.drawText(it.name, 3 * CanvasAdapter.getScale(), 17 * CanvasAdapter.getScale(), paint)

            val markerItem = MarkerItem(it.name, "", GeoPoint(it.centerLat, it.centerLon))
            val markerSymbol = MarkerSymbol(bitmap, 0.5f, 0f)
            markerItem.marker = markerSymbol

            markerItem
        }

        val markers = wayMarkers
            .filterNotNull()


        labelLayer.addItems(markers)


        val listener = object : ItemizedLayer.OnItemGestureListener<MarkerItem> {
            override fun onItemLongPress(index: Int, item: MarkerItem?): Boolean {
                if (item == null) return true

                LoggerFactory.getLogger(IndoorLayersManager::class.simpleName).info("long press on ${item.title}")
                itemTapCallback.longTap(item.title)

                return true
            }

            override fun onItemSingleTapUp(index: Int, item: MarkerItem?): Boolean {
                if (item == null) return true

                LoggerFactory.getLogger(IndoorLayersManager::class.simpleName).info("single tap on ${item.title}")
                itemTapCallback.singleTap(item.title)

                return true
            }

        }
        labelLayer.setOnItemGestureListener(listener)


        return labelLayer
    }

    // endregion


    companion object {
        val defaultTapCallback = object : IndoorItemTapCallback {
            override fun singleTap(placeName: String) {}
            override fun longTap(placeName: String) {}
        }
    }
}


interface IndoorItemTapCallback {
    fun singleTap(placeName: String)
    fun longTap(placeName: String)
}