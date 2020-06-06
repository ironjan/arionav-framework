package de.ironjan.arionav_fw.ionav.model.indoor_map

import de.ironjan.graphhopper.extensions_core.Coordinate

import org.junit.Test
import org.junit.Assert.*

class IndoorWayTest {
  @Test
  fun indoorWayCreatedWithNoNodesHasNoNodeRefs(){
    assertTrue(IndoorWay(-1, 0.0 , emptyList(), emptyMap()).nodeRefs.isEmpty())
  }

  @Test
  fun indoorWayCreatedWithNodesAsOpenWayHasNodeRefs(){
    val expected = arrayListOf(-1L, -2L, -3L)
    val nodes = listOf(IndoorNode(-1, 0.0, 0.0), IndoorNode(-2, 1.0, 1.0), IndoorNode(-3, 2.0, 2.0))
    val actual = IndoorWay(-1, 0.0 , nodes, emptyMap()).nodeRefs

    assertEquals(expected, actual)
  }

  @Test
  fun indoorWayCreatedWithNodesAsClosedWayHasNodeRefs(){
    val expected = arrayListOf(-1L, -2L, -3L, -1L)
    val nodes = listOf(IndoorNode(-1, 0.0, 0.0), IndoorNode(-2, 1.0, 1.0), IndoorNode(-3, 2.0, 2.0), IndoorNode(-1, 0.0, 0.0))
    val actual = IndoorWay(-1, 0.0 , nodes, emptyMap()).nodeRefs

    assertEquals(expected, actual)
  }

  @Test
  fun quadraticIndoorWayHasCorrectCenterInMiddle(){
    val nodes = listOf(IndoorNode(-1, 0.0, 0.0), IndoorNode(-2, 0.0, 1.0), IndoorNode(-3, 1.0, 1.0), IndoorNode(-4, 1.0, 0.0), IndoorNode(-1, 0.0, 0.0))
    val actual = IndoorWay(-1, 0.0 , nodes, mapOf("indoor" to "room")).center

    assertEquals(Coordinate(0.5, 0.5, 0.0), actual)
  }

  @Test
  fun indoorWayWithTagPairIndoorRoomIsRoom(){
    val nodes = listOf(IndoorNode(-1, 0.0, 0.0), IndoorNode(-2, 1.0, 1.0), IndoorNode(-3, 2.0, 2.0), IndoorNode(-1, 0.0, 0.0))
    val actual = IndoorWay(-1, 0.0 , nodes, mapOf("indoor" to "room")).isRoom
    assertTrue(actual)
  }

  @Test
  fun indoorWayWithTagPairIndoorAreaIsArea(){
    val nodes = listOf(IndoorNode(-1, 0.0, 0.0), IndoorNode(-2, 1.0, 1.0), IndoorNode(-3, 2.0, 2.0), IndoorNode(-1, 0.0, 0.0))
    val actual = IndoorWay(-1, 0.0 , nodes, mapOf("indoor" to "area")).isArea
    assertTrue(actual)
  }

  @Test
  fun indoorWayWithTagPairIndoorCorridorIsCorridor(){
    val nodes = listOf(IndoorNode(-1, 0.0, 0.0), IndoorNode(-2, 1.0, 1.0), IndoorNode(-3, 2.0, 2.0), IndoorNode(-1, 0.0, 0.0))
    val actual = IndoorWay(-1, 0.0 , nodes, mapOf("indoor" to "corridor")).isCorridor
    assertTrue(actual)
  }

}