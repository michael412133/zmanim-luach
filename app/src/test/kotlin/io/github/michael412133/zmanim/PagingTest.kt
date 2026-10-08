package io.github.michael412133.zmanim

import org.junit.Assert.assertEquals
import org.junit.Test

class PagingTest {

    @Test
    fun wholeItemsToAPage() {
        // The day's details (130) then times of 52, on a page of 330: 130 + 3 x 52 fit, then 6 to a page.
        val heights = listOf(130f) + List(16) { 52f }
        assertEquals(listOf(0..3, 4..9, 10..15, 16..16), Paging.pages(heights, 330f))
    }

    @Test
    fun anItemTallerThanAPageGetsItsOwn() {
        assertEquals(listOf(0..0, 1..1, 2..3), Paging.pages(listOf(10f, 500f, 10f, 10f), 100f))
    }

    @Test
    fun anEmptyList() {
        assertEquals(1, Paging.pages(emptyList(), 100f).size)
        assertEquals(0, Paging.pageOf(Paging.pages(emptyList(), 100f), 3))
    }

    @Test
    fun theLastPixelStillFits() {
        assertEquals(listOf(0..1), Paging.pages(listOf(50f, 50f), 100f))
        assertEquals(listOf(0..1, 2..2), Paging.pages(listOf(50f, 50f, 1f), 100f))
        assertEquals(1, Paging.pageOf(Paging.pages(listOf(50f, 50f, 1f), 100f), 2))
    }
}
