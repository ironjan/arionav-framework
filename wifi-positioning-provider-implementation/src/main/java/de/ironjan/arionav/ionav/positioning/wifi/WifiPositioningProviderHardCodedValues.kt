package de.ironjan.arionav.ionav.positioning.wifi

import de.ironjan.graphhopper.extensions_core.Coordinate

class WifiPositioningProviderHardCodedValues {
    val macsToRooms: Map<String, String> = mapOf(
        "00:b7:71:35:92:80" to "FU-aussen",
        "00:b7:71:35:92:80" to "FU-aussen",
        "10:05:ca:56:9d:a0" to "FU.106-Flur",
        "10:05:ca:56:9d:a0" to "FU.106-Flur",
        "f8:c2:88:92:29:10" to "FU.201.2-Flur",
        "f8:c2:88:92:29:10" to "FU.201.2-Flur",
        "f8:c2:88:b0:53:70" to "FU.207",
        "f8:c2:88:b0:53:70" to "FU.207",
        "f8:c2:88:b1:c1:f0" to "FU.307-Flur",
        "f8:c2:88:b1:c1:f0" to "FU.307-Flur",
        "f8:c2:88:b1:c2:70" to "FU.317-Flur",
        "f8:c2:88:b1:c2:70" to "FU.317-Flur",
        "f8:c2:88:8f:fb:c0" to "FU.343-Flur",
        "f8:c2:88:8f:fb:c0" to "FU.343-Flur",
        "f8:c2:88:96:37:30" to "FU.501-Flur",
        "f8:c2:88:96:37:30" to "FU.501-Flur",
        "f8:c2:88:a4:5c:c0" to "FU.523",
        "f8:c2:88:a4:5c:c0" to "FU.523",

        "78:72:5d:93:e3:e0" to "F0.116-Flur",
        "78:72:5d:93:e3:e0" to "F0.116-Flur",
        "78:0c:f0:fd:68:60" to "F0.231-Flur",
        "78:0c:f0:fd:68:60" to "F0.231-Flur",
        "dc:8c:37:c7:af:20" to "F0.328-Flur",
        "dc:8c:37:c7:af:20" to "F0.328-Flur",
        "00:fe:c8:5e:ca:b0" to "F0.401-Flur",
        "00:fe:c8:5e:ca:b0" to "F0.401-Flur",
        "78:0c:f0:fd:59:20" to "F0.520",
        "78:0c:f0:fd:59:20" to "F0.520",
        "78:0c:f0:e8:7e:e0" to "F0.540",
        "78:0c:f0:e8:7e:e0" to "F0.540",
        "f8:4f:57:84:82:f0" to "F0.550-hinten",
        "f8:4f:57:84:82:f0" to "F0.550-hinten",
        "10:b3:d6:ed:ec:80" to "F0.550-hinten",
        "10:b3:d6:ed:ec:80" to "F0.550-hinten",
        "20:bb:c0:4b:f7:f0" to "F0.550-vorne",
        "20:bb:c0:4b:f7:f0" to "F0.550-vorne",
        "10:b3:d6:f0:d9:60" to "F0.550-vorne",
        "10:b3:d6:f0:d9:60" to "F0.550-vorne",

        "f8:c2:88:96:35:a0" to "F1.101",
        "f8:c2:88:96:35:a0" to "F1.101",
        "70:b3:17:7f:03:00" to "F1.110",
        "70:b3:17:7f:03:00" to "F1.110",
        "78:0c:f0:e8:7e:40" to "F1.201-Flur",
        "78:0c:f0:e8:7e:40" to "F1.201-Flur",
        "78:0c:f0:e8:81:40" to "F1.213-Flur",
        "78:0c:f0:e8:81:40" to "F1.213-Flur",
        "70:b3:17:e5:8b:20" to "F1.225-Flur",
        "70:b3:17:e5:8b:20" to "F1.225-Flur",
        "78:0c:f0:e8:80:e0" to "F1.404-Flur",
        "78:0c:f0:e8:80:e0" to "F1.404-Flur",
        "70:b3:17:7f:02:80" to "F1.422-Flur",
        "70:b3:17:7f:02:80" to "F1.422-Flur",
        "70:b3:17:e5:8a:a0" to "F1.541",
        "70:b3:17:e5:8a:a0" to "F1.541",
        "00:fe:c8:72:ec:90" to "F1.544",
        "00:fe:c8:72:ec:90" to "F1.544",

        "a0:93:51:06:bf:c0" to "F2.116-Flur",
        "a0:93:51:06:bf:c0" to "F2.116-Flur",
        "70:b3:17:56:24:c0" to "F2.124-Flur",
        "70:b3:17:56:24:c0" to "F2.124-Flur",
        "78:0c:f0:e8:7a:60" to "F2.211",
        "78:0c:f0:e8:7a:60" to "F2.211",
        "70:b3:17:d7:de:e0" to "F2.301-Flur",
        "70:b3:17:d7:de:e0" to "F2.301-Flur",
        "70:b3:17:7f:00:60" to "F2.311-Flur",
        "70:b3:17:7f:00:60" to "F2.311-Flur",
        "70:b3:17:e3:6a:60" to "F2.401-Flur",
        "70:b3:17:e3:6a:60" to "F2.401-Flur",
        "70:b3:17:a5:20:60" to "F2.425-Flur",
        "70:b3:17:a5:20:60" to "F2.425-Flur"
    )


    val roomsToCoordinates = mapOf(

//        "FU-aussen" to Coordinate(0.0,0.0,0.0),
//        "FU-aussen" to Coordinate(0.0,0.0,0.0),
        "FU.106-Flur" to Coordinate(51.732023766666664, 8.734476666666666, -1.0),
        "FU.201.2-Flur" to Coordinate(51.732,8.7352,-1.0), // FIXME
        "FU.207" to Coordinate(51.7317124875, 8.7350351375, -1.0),
        "FU.307-Flur" to Coordinate(51.73141902, 8.73485396, -1.0),
        "FU.317-Flur" to Coordinate(51.7315226589,8.73464794416,-1.0),

        "FU.343-Flur" to Coordinate(51.73174265, 8.73414325, -1.0),
        "FU.501-Flur" to Coordinate(0.0,0.0,0.0),
        "FU.523" to Coordinate(0.0,0.0,0.0),

        "FU.201.1" to Coordinate(51.73172192, 8.7351320, -1.0),
        "FU.201.1" to Coordinate(51.73172192, 8.7351320, -1.0),
        "FU.201.3" to Coordinate(51.73165206, 8.7352365, -1.0),
        "FU.307" to Coordinate(51.73141902, 8.73485396, -1.0),
        "FU.316" to Coordinate(51.73151744999999, 8.7346588, -1.0),
        "FU.343" to Coordinate(51.73174265, 8.73414325, -1.0),


        "F0.116-Flur" to Coordinate(0.0,0.0,0.0),
        "F0.116-Flur" to Coordinate(0.0,0.0,0.0),
        "F0.231-Flur" to Coordinate(0.0,0.0,0.0),
        "F0.231-Flur" to Coordinate(0.0,0.0,0.0),
        "F0.328-Flur" to Coordinate(0.0,0.0,0.0),
        "F0.328-Flur" to Coordinate(0.0,0.0,0.0),
        "F0.401-Flur" to Coordinate(0.0,0.0,0.0),
        "F0.401-Flur" to Coordinate(0.0,0.0,0.0),
        "F0.520" to Coordinate(0.0,0.0,0.0),
        "F0.520" to Coordinate(0.0,0.0,0.0),
        "F0.540" to Coordinate(0.0,0.0,0.0),
        "F0.540" to Coordinate(0.0,0.0,0.0),
        "F0.550-hinten" to Coordinate(0.0,0.0,0.0),
        "F0.550-hinten" to Coordinate(0.0,0.0,0.0),
        "F0.550-hinten" to Coordinate(0.0,0.0,0.0),
        "F0.550-hinten" to Coordinate(0.0,0.0,0.0),
        "F0.550-vorne" to Coordinate(0.0,0.0,0.0),
        "F0.550-vorne" to Coordinate(0.0,0.0,0.0),
        "F0.550-vorne" to Coordinate(0.0,0.0,0.0),
        "F0.550-vorne" to Coordinate(0.0,0.0,0.0),

        "F1.101" to Coordinate(0.0,0.0,0.0),
        "F1.101" to Coordinate(0.0,0.0,0.0),
        "F1.110" to Coordinate(0.0,0.0,0.0),
        "F1.110" to Coordinate(0.0,0.0,0.0),
        "F1.201-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.201-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.213-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.213-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.225-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.225-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.404-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.404-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.422-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.422-Flur" to Coordinate(0.0,0.0,0.0),
        "F1.541" to Coordinate(0.0,0.0,0.0),
        "F1.541" to Coordinate(0.0,0.0,0.0),
        "F1.544" to Coordinate(0.0,0.0,0.0),
        "F1.544" to Coordinate(0.0,0.0,0.0),

        "F2.116-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.116-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.124-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.124-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.211" to Coordinate(0.0,0.0,0.0),
        "F2.211" to Coordinate(0.0,0.0,0.0),
        "F2.301-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.301-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.311-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.311-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.401-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.401-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.425-Flur" to Coordinate(0.0,0.0,0.0),
        "F2.425-Flur" to Coordinate(0.0,0.0,0.0)
    )

    val macsToCoordinates =
        macsToRooms
            .map { it.key to roomsToCoordinates[it.value] }
            .filterNot { it.second == null }
            .map { it.first to it.second!! }
            .toMap()


}
