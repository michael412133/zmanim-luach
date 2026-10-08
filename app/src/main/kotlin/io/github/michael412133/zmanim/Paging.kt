package io.github.michael412133.zmanim

/** Dividing a list into pages, for an e-ink screen that turns a page instead of scrolling. */
object Paging {

    /**
     * Splits items of the given heights into pages of [pageHeight], in order, as many whole items
     * to a page as fit. An item taller than a page gets a page of its own. There is always at
     * least one page, empty when there are no items.
     */
    fun pages(heights: List<Float>, pageHeight: Float): List<IntRange> {
        if (heights.isEmpty()) return listOf(IntRange.EMPTY)
        val pages = mutableListOf<IntRange>()
        var start = 0
        var used = 0f
        heights.forEachIndexed { index, height ->
            if (index > start && used + height > pageHeight + 0.5f) {
                pages += start until index
                start = index
                used = 0f
            }
            used += height
        }
        pages += start until heights.size
        return pages
    }

    /** The page that holds an item, or the first page. */
    fun pageOf(pages: List<IntRange>, item: Int): Int = pages.indexOfFirst { item in it }.coerceAtLeast(0)
}
