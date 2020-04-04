package de.ironjan.arionav_fw.ionav

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.FileInputStream
import java.io.IOException

object OsmBoundsExtractor {

    data class Bounds(val minLat: Double, val minLon: Double, val maxLat: Double, val maxLon: Double)

    @Throws(XmlPullParserException::class, IOException::class)
    fun extractBoundsFromOsm(osmFilePath: String): Bounds? {
        var xxxx: Bounds? = null
        FileInputStream(osmFilePath).use { fileInputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(fileInputStream, null)
            parser.nextTag()
            xxxx = extractBounds(parser)
        }
        return xxxx
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun extractBounds(parser: XmlPullParser): Bounds? {
        parser.require(XmlPullParser.START_TAG, null, "osm")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag
            if (parser.name == "bounds") {
                val minLat = parser.getAttributeValue(null, "minlat")?.toDoubleOrNull()
                val minLon = parser.getAttributeValue(null, "minlon")?.toDoubleOrNull()
                val maxLat = parser.getAttributeValue(null, "maxlat")?.toDoubleOrNull()
                val maxLon = parser.getAttributeValue(null, "maxlon")?.toDoubleOrNull()

                if (arrayOf(minLat, minLon, maxLat, maxLon).any { it == null }) {
                    // FIXME throw invalid file exception
                    return null
                }
                return Bounds(minLat!!, minLon!!, maxLat!!, maxLon!!)
            } else {
                skip(parser)
            }
        }
        // FIXME throw invalid file exception
        return null
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}