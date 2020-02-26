package de.ironjan.arionav.ionav.positioning.wifi

import de.ironjan.graphhopper.extensions_core.Coordinate

class WifiPositioningProviderHardCodedValues {
    val macsToRooms: Map<String, String> = mapOf(
        "70:b3:17:e5:8a:a0" to "F1.541",
        "78:0c:f0:fd:59:20" to "F0.520",
        "00:fe:c8:72:ec:90" to "F1.544",
        "78:0c:f0:e8:7e:e0" to "F0.540",
        "f8:c2:88:a4:5c:c0" to "FU.523",
        "70:b3:17:7f:03:00" to "F1.110",
        "f8:c2:88:b0:53:70" to "FU.207",
        "f8:c2:88:96:35:a0" to "F1.101",
        "78:0c:f0:e8:7a:60" to "F2.211"
    )


    val roomsToCoordinates = mapOf(
        "FU.128" to Coordinate(51.731835700000005, 8.7348406375, -1.0),
        "FU.124" to Coordinate(51.731878971428564, 8.734778657142856, -1.0),
        "FU.126" to Coordinate(51.73186586666667, 8.734723466666667, -1.0),
        "FU.519" to Coordinate(51.73182024999999, 8.734654875, -1.0),
        "FU.511" to Coordinate(51.7318412, 8.734502575, -1.0),
        "FU.136" to Coordinate(51.73175776666667, 8.734918522222223, -1.0),
        "FU.507" to Coordinate(51.7317764, 8.73461432, -1.0),
        "Treppenhaus Nord" to Coordinate(51.73186321666666, 8.735057016666667, -1.0),
        "FU.201.3" to Coordinate(51.73165206, 8.7352365, -1.0),
        "FU201.2" to Coordinate(51.73169200000001, 8.73518996, -1.0),
        "FU.201.1" to Coordinate(51.731721916666665, 8.735131966666666, -1.0),
        "FU.207" to Coordinate(51.7317124875, 8.7350351375, -1.0),
        "FU.211" to Coordinate(51.73164355, 8.734935833333335, -1.0),
        "FU.214" to Coordinate(51.73159834285714, 8.734861214285715, -1.0),
        "FU.225" to Coordinate(51.73155701666666, 8.734784666666666, -1.0),
        "FU.227" to Coordinate(51.73152248571429, 8.734837214285713, -1.0),
        "FU.231" to Coordinate(51.73149006666666, 8.734918716666668, -1.0),
        "FU.234" to Coordinate(51.731457683333325, 8.734987216666667, -1.0),
        "FU.237" to Coordinate(51.73143408333333, 8.735037249999998, -1.0),
        "WC Herren" to Coordinate(51.73161816, 8.73478098, -1.0),
        "WC Damen" to Coordinate(51.73163982, 8.734735879999999, -1.0),
        "Teeküche" to Coordinate(51.73166036, 8.734693060000001, -1.0),
        "Bernsteinzimmer" to Coordinate(51.7316802, 8.734661166666665, -1.0),
        "WC Damen" to Coordinate(51.73202148333334, 8.7347525, -1.0),
        "WC Herren" to Coordinate(51.732047171428576, 8.734702042857142, -1.0),
        "Fahrstühle" to Coordinate(51.73206038571429, 8.734639242857144, -1.0),
        "FU.106" to Coordinate(51.732023766666664, 8.734476666666666, -1.0),
        "FU.105" to Coordinate(51.731982262500004, 8.734410875, -1.0),
        "FU.301" to Coordinate(51.73136911666666, 8.734969466666668, -1.0),
        "FU.304" to Coordinate(51.731392316666664, 8.73492055, -1.0),
        "FU.307" to Coordinate(51.73141902, 8.73485396, -1.0),
        "FU.310" to Coordinate(51.7314427, 8.7347884, -1.0),
        "FU.313" to Coordinate(51.731483433333345, 8.734728466666667, -1.0),
        "FU.316" to Coordinate(51.73151744999999, 8.7346588, -1.0),
        "FU.318" to Coordinate(51.731536157142855, 8.734604514285714, -1.0),
        "FU.219" to Coordinate(51.73158141428571, 8.734634400000001, -1.0),
        "FU.323" to Coordinate(51.731595171428566, 8.7345833, -1.0),
        "FU.326" to Coordinate(51.73163121666666, 8.734514266666665, -1.0),
        "FU.329" to Coordinate(51.73165803333333, 8.734456566666665, -1.0),
        "FU.332" to Coordinate(51.73168486666666, 8.734375033333334, -1.0),
        "FU.343" to Coordinate(51.73174265, 8.73414325, -1.0),
        "FU.346" to Coordinate(51.731780766666674, 8.73408695, -1.0),
        "FU.350" to Coordinate(51.73183124285714, 8.733986771428572, -1.0),
        "FU.358" to Coordinate(51.73188651666667, 8.733989816666666, -1.0),
        "FU.362" to Coordinate(51.7319418, 8.734097042857142, -1.0),
        "FU.401" to Coordinate(51.73201244999999, 8.734128516666667, -1.0),
        "FU.404" to Coordinate(51.73205707142857, 8.734191157142856, -1.0),
        "FU.407" to Coordinate(51.73209821666668, 8.7342181, -1.0),
        "FU.370" to Coordinate(51.73185081428572, 8.734262957142857, -1.0),
        "FU.367" to Coordinate(51.73190791999999, 8.73421236, -1.0),
        "FU.368" to Coordinate(51.73186396666666, 8.734148766666666, -1.0),
        "FU.338" to Coordinate(51.73170074444445, 8.734274444444445, -1.0),
        "FU.335" to Coordinate(51.73175346000001, 8.73429774, -1.0),
        "Treppenhaus Süd" to Coordinate(51.73180577142856, 8.734352814285716, -1.0),
        "FU.505" to Coordinate(51.7317584, 8.73444875, -1.0),
        "WC Herren" to Coordinate(51.73204714285715, 8.734702028571428, 0.0),
        "Fahrstühle" to Coordinate(51.7320603, 8.734639357142857, 0.0),
        "WC Damen" to Coordinate(51.732021350000004, 8.734752333333333, 0.0),
        "WC" to Coordinate(51.720410516666654, 8.751796533333334, 0.0),
        "WC" to Coordinate(51.7219485, 8.749251928571429, 0.0),
        "Treppenhaus Süd" to Coordinate(51.73182115714285, 8.734382442857143, 0.0)
    )

    val macsToCoordinates =
        macsToRooms
            .map { it.key to roomsToCoordinates[it.value] }
            .filterNot { it.second == null }
            .map { it.first to it.second!! }
            .toMap()


}
