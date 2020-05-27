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


        val blue = Color.get(80, 0, 0, 255)
        val lightGray = Color.get(120, 120, 120, 120)
        val darkGray = Color.get(60, 120, 120, 120)
        val lightBlue = Color.get(80, 50, 50, 220)
        val orange = Color.get(80, 250, 150, 0)
        val red = Color.get(80, 255, 0, 0)

        val roomStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(lightGray)
            .fillColor(darkGray)
            .strokeWidth(1 * density)
            .build()
        val corridorStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .fillColor(lightGray)
            .strokeColor(lightGray)
            .strokeWidth(0 * density)
            .build()
        val areaStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .fillColor(lightGray)
            .strokeColor(darkGray)
            .strokeWidth(0 * density)
            .build()
        val floorConnectorStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .fillColor(red)
            .strokeColor(red)
            .strokeWidth(1 * density)
            .build()

        val indoorWays = id.getWays(level)

        val f1_122 = indoorWays.firstOrNull { it.name == "F1.122" }
        val f0_054 = indoorWays.firstOrNull { it.name == "F0.054" }
        val f2_116 = indoorWays.firstOrNull { it.name == "F2.116" }

        val levelLayer = VectorLayer(map)

        indoorWays
            .filterNot { it.isRoom }
            .filterNot { it.isArea }
            .filter { it.isCorridor }
            .filterNot { it.isFloorConnector }
            .mapNotNull { createOutline(it, corridorStyle) }
            .map { levelLayer.add(it) }
        indoorWays
            .filterNot { it.isRoom }
            .filter { it.isArea }
            .filterNot { it.isCorridor }
            .filterNot { it.isFloorConnector }
            .mapNotNull { createOutline(it, areaStyle) }
            .map { levelLayer.add(it) }

        indoorWays
            .filter { it.isRoom }
            .filterNot { it.isArea || it.isCorridor }
            .filterNot { it.isFloorConnector }
            .mapNotNull { createOutline(it, roomStyle) }
            .map { levelLayer.add(it) }

        indoorWays
            .filter { it.isFloorConnector }
            .mapNotNull { createOutline(it, floorConnectorStyle) }
            .map { levelLayer.add(it) }

//        floorConnectorDrawables.map { levelLayer.add(it) }
//        nodeDrawables.map { levelLayer.add(it) }
        return levelLayer
    }

    private val logger = LoggerFactory.getLogger(IndoorLayersManager::class.simpleName)
    private fun createOutline(iw: IndoorWay, style: Style): PolygonDrawable? = try {
        logger.info("Creating outline (fill: ${style.fillColor}, stroke: ${style.strokeColor}) from $iw")
        PolygonDrawable(iw.distinctNodeRefs.map { it.toGeoPoint() }, style)
    } catch (e: Exception) {
        null
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