package de.ironjan.arionav_fw.ionav.model.osm


import org.junit.Test

import org.junit.Assert.*

class ElementTest {

    @Test
    fun tagsShouldBeEmptyOnDefault() {
        assertTrue(Element(-1).tags.isEmpty())
    }

    @Test
    fun elementWithEmptyTagsShouldHaveNullName() {
        assertEquals(null, Element(-1).name)    
    }

    @Test
    fun elementWithNameInTagsShouldHaveCorrectName() {
        val element = Element(-1, mapOf("name" to "the name"))
        assertEquals("the name", element.name)
    }

    @Test
    fun elementWithoutNameInTagsShouldHaveNullName() {
        val element = Element(-1, mapOf("key" to "value"))
        assertEquals(null, element.name)
    }

}
