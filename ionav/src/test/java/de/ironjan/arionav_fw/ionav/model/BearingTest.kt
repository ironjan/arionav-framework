package de.ironjan.arionav_fw.ionav.model

import de.ironjan.graphhopper.extensions_core.Coordinate
import org.junit.Assert
import org.junit.Test

class BearingTest {

    @Test
    fun kansasCity_StLouis_Test() {
        val kansasCity = Coordinate(39.099912, -94.581213, 0.0)
        val stLouis = Coordinate(38.627089, -90.200203, 0.0)

        val actual = Bearing.compute(kansasCity.lat, kansasCity.lon, stLouis.lat, stLouis.lon)

        Assert.assertEquals(96.51263f, actual, 0.00001f)
    }
}