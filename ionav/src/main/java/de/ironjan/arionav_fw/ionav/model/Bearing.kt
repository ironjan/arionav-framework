package de.ironjan.arionav_fw.ionav.model

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val Double.asRadian: Double
    get() = this * PI / 180

private val Double.asDegree: Double
    get() = this * 180 / PI

object Bearing {
    fun compute(firstLat: Double, firstLon: Double, secondLat: Double, secondLon: Double): Float {
        // See https://www.igismap.com/formula-to-find-bearing-or-heading-angle-between-two-points-latitude-longitude/

        val delta_L = secondLon - firstLon

        // convert all to Radian first
        val X = cos(secondLat.asRadian) * sin(delta_L.asRadian)
        val Y = cos(firstLat.asRadian) * sin(secondLat.asRadian) - sin(firstLat.asRadian) * cos(secondLat.asRadian) * cos(delta_L.asRadian)



        val betaRad = atan2(X, Y)
        val beta = betaRad.asDegree




        return beta.toFloat()
    }

}
