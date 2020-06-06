package de.ironjan.arionav_fw.ionav.model.indoor_map


import org.junit.Test

import org.junit.Assert.*

class IndoorNodeTest {

    @Test
    fun indoorNodeHasEmptyTagsOnDefault() {
      assertTrue(IndoorNode(-1, 0.0, 0.0).tags.isEmpty())
    }

    @Test
    fun indoorNodeWithEmptyTagsHasDoubleLevelZero() {
        assertEquals(0.0, IndoorNode(-1, 0.0, 0.0).lvl, 0.0)
    }

    @Test
    fun indoorNodeWithLevelTagOneHasDoubleLevelOne() {
        assertEquals(1.0, IndoorNode(-1, 0.0, 0.0, mapOf("level" to "1")).lvl, 0.0)
        assertEquals(1.0, IndoorNode(-1, 0.0, 0.0, mapOf("level" to "1.0")).lvl, 0.0)
    }

    @Test
    fun indoorNodeWithLevelTagMinusOneHasDoubleLevelMinusOne() {
        assertEquals(-1.0, IndoorNode(-1, 0.0, 0.0, mapOf("level" to "-1")).lvl, 0.0)
        assertEquals(-1.0, IndoorNode(-1, 0.0, 0.0, mapOf("level" to "-1.0")).lvl, 0.0)
    }

    @Test
    fun indoorNodeWithNonEmptyTagsButNoLevelTagHasDoubleLevelZero() {
        assertEquals(0.0, IndoorNode(-1, 0.0, 0.0, mapOf("name" to "some name")).lvl, 0.0)
    }

}