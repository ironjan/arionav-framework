package de.ironjan.arionav_fw.ionav.positioning

object IonavLocationDistanceCalculator {
    /**
     * Computes the distance in meters between the two given locations as if they were
     * on one level, i.e. only takes lat/lon into
     */
    fun distanceBetween(a: IonavLocation, b: IonavLocation): Double {
        val R = 6371000 // earth radius in m

        val theta1 = a.lat * Math.PI / 180
        val theta2 = b.lat * Math.PI / 180

        val deltaTheta = (b.lat - a.lat) * Math.PI / 180
        val deltaLambda = (b.lon - a.lon) * Math.PI / 180

        val a = Math.sin(deltaTheta / 2) * Math.sin(deltaTheta / 2) +
                Math.cos(theta1) * Math.cos(theta2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        val d = R * c
        return d
    }
}