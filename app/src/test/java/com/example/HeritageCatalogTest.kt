package com.example

import com.example.data.datasource.HeritageCatalog
import com.example.data.model.HeritageCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HeritageCatalogTest {

    @Test
    fun `catalog contains prominent Indian heritage sites`() {
        val konark = HeritageCatalog.findById("konark-sun-temple")
        assertNotNull(konark)
        assertEquals("Konark Sun Temple", konark?.name)
        assertTrue(konark?.unescoWorldHeritage == true)

        val hampi = HeritageCatalog.findById("hampi-vijayanagara")
        assertNotNull(hampi)

        val brihadisvara = HeritageCatalog.findById("brihadisvara-temple")
        assertNotNull(brihadisvara)
    }

    @Test
    fun `search by query returns matching monuments`() {
        val results = HeritageCatalog.search("odisha")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.id == "konark-sun-temple" })
    }

    @Test
    fun `filter by category works correctly`() {
        val temples = HeritageCatalog.search("", HeritageCategory.TEMPLES)
        assertTrue(temples.isNotEmpty())
        assertTrue(temples.all { it.category == HeritageCategory.TEMPLES })
    }
}
