package de.ironjan.arionav_fw.framework.arionav

import com.graphhopper.util.Instruction
import com.graphhopper.util.PointList

data class SimplifiedRoute(val waypoints: PointList,
                           val instructionList: List<Instruction>){
  val zipped = waypoints.zip(instructionList)
}