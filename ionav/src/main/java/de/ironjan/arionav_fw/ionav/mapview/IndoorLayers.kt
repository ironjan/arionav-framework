package de.ironjan.arionav_fw.ionav.mapview

import de.ironjan.arionav_fw.ionav.routing.model.indoor_map.IndoorData
import org.oscim.backend.CanvasAdapter
import org.oscim.backend.canvas.Color
import org.oscim.backend.canvas.Paint
import org.oscim.core.GeoPoint
import org.oscim.layers.Layer
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.layers.tile.vector.VectorTileLayer
import org.oscim.layers.vector.VectorLayer
import org.oscim.layers.vector.geometries.PolygonDrawable
import org.oscim.layers.vector.geometries.RectangleDrawable
import org.oscim.layers.vector.geometries.Style
import org.oscim.map.Map
import org.slf4j.LoggerFactory

/**
 * Inspired by <a href="https://github.com/mapsforge/vtm/blob/master/vtm-jeo/src/org/oscim/layers/OSMIndoorLayer.java">OSMIndoorLayer</a>
 * in mapsforge:vtm-jeo
 */
class IndoorLayers(private val map: Map, private val tileLayer: VectorTileLayer, private val density: Float) {

    var indoorData = IndoorData(emptyMap(), emptyMap())
        set(value) {
            field = value
            levelsToDrawableLayers = prepareDrawableLayers(value)
            levelsToLabelLayers = prepareLabelLayers(value)
            replaceMapLayers(selectedLevel, selectedLevel)
        }

    private var levelsToDrawableLayers = prepareDrawableLayers(indoorData)
    private var levelsToLabelLayers = prepareLabelLayers(indoorData)

    private fun prepareDrawableLayers(id: IndoorData): kotlin.collections.Map<Double, VectorLayer> {
        return id.levels.map {
            Pair(it, prepareDrawableLayer(id, it))
        }.toMap()
    }

    private fun prepareDrawableLayer(id: IndoorData, level: Double): VectorLayer {
        val nodeDrawables = id.getNodes(level).map {
            RectangleDrawable(it.toGeoPoint(), it.toGeoPoint())
        }


        val roomStyle = Style.builder()
            .fixed(true)
            .generalization(Style.GENERALIZATION_SMALL)
            .strokeColor(-0x66333333)
            .fillColor(-0x33333333)
            .strokeWidth(1 * density)
            .build()

        val wayDrawables = id
            .getWays(level)
            .filter { it.isRoom }
            .map { iw ->
                try {
                    val map1 = iw.nodeRefs.map { it.toGeoPoint() }
                    val geometry = PolygonDrawable(map1)
                    geometry.style = roomStyle

                    geometry
                } catch (e: Exception) {
                    val y = e
                    null
                }
            }.filterNotNull()

        val levelLayer = VectorLayer(map)

        val drawables = nodeDrawables.union(wayDrawables)
        drawables.map { levelLayer.add(it) }

        return levelLayer
    }

    private fun prepareLabelLayers(id: IndoorData): kotlin.collections.Map<Double, Layer> {
        return id.levels.map {
            Pair(it, prepareLabelLayer(id, it))
        }.toMap()
    }

    private fun prepareLabelLayer(id: IndoorData, level: Double): Layer {
        val labelLayer = ItemizedLayer<MarkerItem>(map, null as MarkerSymbol?)

        val canvas = CanvasAdapter.newCanvas()
        val paint = CanvasAdapter.newPaint()
        paint.setTypeface(Paint.FontFamily.DEFAULT, Paint.FontStyle.NORMAL)
        paint.setTextSize(12 * CanvasAdapter.getScale())
        paint.strokeWidth = 2 * CanvasAdapter.getScale()
        paint.color = Color.BLACK

        val nodeMarkers = id.getNodes(level).map {
            if(it.name.isNullOrEmpty()) return@map  null

            val bitmap = CanvasAdapter.newBitmap((42 * CanvasAdapter.getScale()).toInt(), (42 * CanvasAdapter.getScale()).toInt(), 0)
            canvas.setBitmap(bitmap)
            canvas.fillColor(Color.TRANSPARENT)

            canvas.drawText(it.name, 3 * CanvasAdapter.getScale(), 17 * CanvasAdapter.getScale(), paint)
//            canvas.drawText(java.lang.Double.toString(lon), 3 * CanvasAdapter.getScale(), 35 * CanvasAdapter.getScale(), paint)

            val markerItem = MarkerItem(it.name, "description test", GeoPoint(it.lat, it.lon))
            val markerSymbol = MarkerSymbol(bitmap, 0f, 1f)
            markerItem.marker = markerSymbol


            markerItem
        }

        val wayMarkers = id.getWays(level).map {
            if(it.name.isNullOrEmpty()) return@map  null

            val bitmap = CanvasAdapter.newBitmap((42 * CanvasAdapter.getScale()).toInt(), (20 * CanvasAdapter.getScale()).toInt(), 0)
            canvas.setBitmap(bitmap)
            canvas.fillColor(Color.GREEN)

            canvas.drawText(it.name, 3 * CanvasAdapter.getScale(), 17 * CanvasAdapter.getScale(), paint)

            val markerItem = MarkerItem(it.name, "", GeoPoint(it.centerLat, it.centerLon))
            val markerSymbol = MarkerSymbol(bitmap, 0.5f, 0f)
            markerItem.marker = markerSymbol

            markerItem
        }

        val markers = nodeMarkers.union(wayMarkers)
            .filterNotNull()


        labelLayer.addItems(markers)


        val listener = object : ItemizedLayer.OnItemGestureListener<MarkerItem> {
            override fun onItemLongPress(index: Int, item: MarkerItem?): Boolean {
                if(item == null) return true
                LoggerFactory.getLogger(IndoorLayers::class.simpleName).info("long press on ${item.title}")
                return true
            }

            override fun onItemSingleTapUp(index: Int, item: MarkerItem?): Boolean {
                if(item == null) return true
                LoggerFactory.getLogger(IndoorLayers::class.simpleName).info("single tap on ${item.title}")
                return true
            }

        }
        labelLayer.setOnItemGestureListener(listener)


        return labelLayer
    }

    var selectedLevel: Double = 0.0
        set(value) {
            if (value == field) return

            replaceMapLayers(field, value)

            field = value
        }

    private fun replaceMapLayers(oldLevel: Double, newLevel: Double) {
        val oldDrawableLayer = levelsToDrawableLayers[oldLevel]
        val newDrawableLayer = levelsToDrawableLayers[newLevel]

        val oldLabelLayer = levelsToLabelLayers[oldLevel]
        val newLabelLayer = levelsToLabelLayers[newLevel]

        map.layers().remove(oldDrawableLayer)
        map.layers().add(newDrawableLayer)

        map.layers().remove(oldLabelLayer)
        map.layers().add(newLabelLayer)
    }


}