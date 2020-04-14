package de.ironjan.arionav_fw.ionav.mapview

import com.vividsolutions.jts.geom.Envelope
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.LineString
import io.jeo.vector.VectorDataset
import io.jeo.map.CartoCSS
import io.jeo.map.RGB
import io.jeo.map.Rule
import io.jeo.map.Style
import io.jeo.vector.Feature
import org.oscim.backend.canvas.Color
import org.oscim.jeo.JeoUtils
import org.oscim.layers.JeoVectorLayer
import org.oscim.layers.vector.AbstractVectorLayer
import org.oscim.map.Map
import org.oscim.renderer.bucket.TextBucket
import org.oscim.renderer.bucket.TextItem
import org.oscim.theme.styles.AreaStyle
import org.oscim.theme.styles.LineStyle
import org.oscim.theme.styles.TextStyle
import java.util.*

/**
 * The same as {@see org.oscim.layers.OSMIndoorLayer} with an adapted (private) getLevel-method.
 *  - valid levels like "-1;0;0" caused a crash. They default to "0" now.
 *  - negative levels caused incorrect computations. All levels are shifted by one, i.e. level -1 is returned as 0.
 */
open class OSMIndoorLayerWithLevelMinusOneSupport(map: Map, data: VectorDataset, style: Style, private val textStyle: TextStyle) : JeoVectorLayer(map, data, style) {

    protected var mTextLayer: TextBucket = TextBucket()
    protected var mText: TextStyle = textStyle

    var activeLevels = BooleanArray(10)

    override fun processFeatures(t: AbstractVectorLayer.Task, b: Envelope) {
        mTextLayer = TextBucket()

        t.buckets.set(mTextLayer)

        super.processFeatures(t, b)

        //render TextItems to a bitmap and prepare vertex buffer data.
        mTextLayer.prepare()
    }

    override fun addLine(t: AbstractVectorLayer.Task, f: Feature, rule: Rule, g: Geometry) {

        if ((g as LineString).isClosed) {
            addPolygon(t, f, rule, g)
            return
        }

        val level = getLevel(f)

        val ll = t.buckets.getLineBucket(level * 3 + 2)
        if (ll.line == null) {
            val color = rule.color(f, CartoCSS.LINE_COLOR, RGB.black)
            val width = rule.number(f, CartoCSS.LINE_WIDTH, 1.2f)!!
            ll.line = LineStyle(0, JeoUtils.color(color), width)
            ll.heightOffset = (level * 4).toFloat()
            ll.setDropDistance(0f)
        }

        addLine(t, g, ll)
    }

    override fun addPolygon(t: AbstractVectorLayer.Task, f: Feature, rule: Rule, g: Geometry) {
        val level = getLevel(f)

        val ll = t.buckets.getLineBucket(level * 3 + 1)

        val active = activeLevels[level + 1]

        if (ll.line == null) {
            val width = rule.number(f, CartoCSS.LINE_WIDTH, 1.2f)!!
            //int color = Color.rainbow((level + 1) / 10f);
            var color = JeoUtils.color(rule.color(f, CartoCSS.LINE_COLOR, RGB.black))

            if (/*level > -2 && */!active)
                color = getInactiveColor(color)

            ll.line = LineStyle(0, color, width)
            ll.heightOffset = (level * 4).toFloat()
            ll.setDropDistance(0f)
        }

        val mesh = t.buckets.getMeshBucket(level * 3)
        if (mesh.area == null) {
            var color = JeoUtils.color(rule.color(f, CartoCSS.POLYGON_FILL, RGB.red))
            if (/*level > -2 && */!active)
                color = getInactiveColor(color)

            mesh.area = AreaStyle(color)
            //mesh.area = new Area(Color.fade(Color.DKGRAY, 0.1f));
            mesh.heightOffset = level * 4f
        }

        addPolygon(t, g, mesh, ll)

        if (active) {
            val o = f.get("name")
            if (o is String) {
                var x = 0f
                var y = 0f
                val n = mGeom.index[0]
                if (n > 0) {
                    var i = 0
                    while (i < n) {
                        x += mGeom.points[i++]
                        y += mGeom.points[i++]
                    }

                    val ti = TextItem.pool.get()
                    ti.set(x / (n / 2), y / (n / 2), o, mText)

                    mTextLayer.addText(ti)
                }
            }
        }
    }

    override fun addPoint(t: AbstractVectorLayer.Task?, f: Feature?, rule: Rule?, g: Geometry?) {

    }

    protected fun getInactiveColor(color: Int): Int {
        return Color.fade(color, 0.1)
    }

    private fun getLevel(f: Feature): Int {
        /* not sure if one could match these geojson properties with cartocss */
        var o = f.get("@relations")
        if (o is HashMap<*, *>) {
            val tags = o as HashMap<String, Any>
            val reltags = tags["reltags"] as HashMap<String, Any>

            if (reltags != null) {
                o = reltags["level"]
                if (o is String) {
                    //log.debug("got level {}", o);
                    return Integer.parseInt(o)
                }
            }
        }

        o = f.get("level")
        val rawLevel = if (o is String) {
            o.toDoubleOrNull()?.toInt() ?: 0
        } else 0
        return rawLevel - minLevel // level bug fix. minLevel is <0

    }


    //region my extensions
    private var _activeLevel = 0
    var activeLevel: Int
        get() {
            return _activeLevel
        }
        set(value) {
            // 8 max
            _activeLevel = value
            activeLevels = activeLevels.map { false }.toBooleanArray()
            val shift = 1 - minLevel
            activeLevels[value + shift] = true
        }
    val minLevel = -1
    val maxLevel = activeLevels.size - 2

    //endregion
}