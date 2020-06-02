package de.ironjan.arionav_fw.samples.campus.data

import de.ironjan.graphhopper.extensions_core.Coordinate

object WifiPositioningProviderHardCodedValues {
    val deviceNameMap: Map<String, String> = mapOf(
        "00:b7:71:35:92:80" to "FU-aussen",
        "10:05:ca:56:9d:a0" to "FU.106-Flur",
        "f8:c2:88:92:29:10" to "FU.201.2-Flur",
        "f8:c2:88:b0:53:70" to "FU.207",
        "f8:c2:88:b1:c1:f0" to "FU.307-Flur",
        "f8:c2:88:b1:c2:70" to "FU.317-Flur",
        "f8:c2:88:8f:fb:c0" to "FU.343-Flur",
        "f8:c2:88:96:37:30" to "FU.501-Flur",
        "f8:c2:88:a4:5c:c0" to "FU.523",

        "78:72:5d:93:e3:e0" to "F0.116-Flur",
        "78:0c:f0:fd:68:60" to "F0.231-Flur",
        "dc:8c:37:c7:af:20" to "F0.328-Flur",
        "00:fe:c8:5e:ca:b0" to "F0.401-Flur",
        "78:0c:f0:fd:59:20" to "F0.520",
        "78:0c:f0:e8:7e:e0" to "F0.540",
        "f8:4f:57:84:82:f0" to "F0.550-hinten",
        "10:b3:d6:ed:ec:80" to "F0.550-hinten",
        "20:bb:c0:4b:f7:f0" to "F0.550-vorne",
        "10:b3:d6:f0:d9:60" to "F0.550-vorne",

        "f8:c2:88:96:35:a0" to "F1.101",
        "70:b3:17:7f:03:00" to "F1.110",
        "78:0c:f0:e8:7e:40" to "F1.201-Flur",
        "78:0c:f0:e8:81:40" to "F1.213-Flur",
        "70:b3:17:e5:8b:20" to "F1.225-Flur",
        "78:0c:f0:e8:80:e0" to "F1.404-Flur",
        "70:b3:17:7f:02:80" to "F1.422-Flur",
        "70:b3:17:e5:8a:a0" to "F1.541",
        "00:fe:c8:72:ec:90" to "F1.544",

        "a0:93:51:06:bf:c0" to "F2.116-Flur",
        "70:b3:17:56:24:c0" to "F2.124-Flur",
        "78:0c:f0:e8:7a:60" to "F2.211",
        "70:b3:17:d7:de:e0" to "F2.301-Flur",
        "70:b3:17:7f:00:60" to "F2.311-Flur",
        "70:b3:17:e3:6a:60" to "F2.401-Flur",
        "70:b3:17:a5:20:60" to "F2.425-Flur"
    )


    val nameToCoordinatesMap = mapOf(
        "FU-aussen" to Coordinate(51.7319664,8.7346338,-1.0), //FIXME double-check
        "FU.106-Flur" to Coordinate(51.7319838,8.7342958,-1.0), //FIXME double-check
        "FU.201.2-Flur" to Coordinate(51.7317068,8.735199,-1.0),
        "FU.207" to Coordinate(51.7317043,8.735022,-1.0),
        "FU.307-Flur" to Coordinate(51.7314692,8.7349121,-1.0),
        "FU.317-Flur" to Coordinate(51.735735,8.7347116,-1.0), //FIXME double-check
        "FU.343-Flur" to Coordinate(51.7317807,8.7342516,-1.0), //FIXME double-check
        "FU.501-Flur" to Coordinate(51.7316981,8.7346291,-1.0), //FIXME double-check
        "FU.523" to Coordinate(51.731696,8.7348276,-1.0), //FIXME double-check


        "F0.116-Flur" to Coordinate(51.7316852,8.7353311,0.0),
        "F0.231-Flur" to Coordinate(51.7314543,8.734896,0.0),
        "F0.328-Flur" to Coordinate(51.7317174,8.7343454,0.0),
        "F0.401-Flur" to Coordinate(51.7318995,8.7340906,0.0),
        "F0.520" to Coordinate(51.7317055,8.7348376,0.0),
        "F0.540" to Coordinate(51.7320444,8.734385,0.0),
        "F0.550-hinten" to Coordinate(51.731847,8.7346157,0.0),
        "F0.550-vorne" to Coordinate(51.7318393,8.7345495,0.0),

        "F1.101" to Coordinate(51.7220884,8.7347102,1.0),
        "F1.110" to Coordinate(51.731949,8.7348852,1.0),
        "F1.201-Flur" to Coordinate(51.7318454,8.7351286,1.0),
        "F1.213-Flur" to Coordinate(51.7316873,8.7349536,1.0),
        "F1.225-Flur" to Coordinate(51.7315726,8.7347987,1.0),
        "F1.404-Flur" to Coordinate(51.7317914,8.7343354,1.0),
        "F1.422-Flur" to Coordinate(51.7320174,8.7345754,1.0),
        "F1.541" to Coordinate(51.7318501,8.7345265,1.0),
        "F1.544" to Coordinate(51.7319277,8.7346432,1.0),

        "F2.116-Flur" to Coordinate(51.7319165,8.7349845,2.0),
        "F2.124-Flur" to Coordinate(51.7318443,8.7351333,2.0),
        "F2.211" to Coordinate(51.731708,8.7349711,2.0),
        "F2.301-Flur" to Coordinate(51.7315751,8.7347981,2.0),
        "F2.311-Flur" to Coordinate(51.7316673,8.7345962,2.0),
        "F2.401-Flur" to Coordinate(51.7318019,8.7343173,2.0),
        "F2.425-Flur" to Coordinate(51.732071,8.7346519,2.0)
    )

    val deviceMap: Map<String, Coordinate> =
        deviceNameMap
            .map { it.key to nameToCoordinatesMap[it.value] }
            .filterNot { it.second == null }
            .map { it.first to it.second!! }
            .toMap()


}