package com.bazhicc.angelscript.formatter

/** Compose authorized structural edits with the subsequent whitespace-formatting pass. */
object ControlFlowBraces {
    /** Compose insertions with clang's subsequent whitespace edits in original document coordinates. */
    fun compose(text: String, insertions: List<Replacement>, edits: List<Replacement>): List<Replacement> {
        fun originalOffset(offset: Int, end: Boolean): Int {
            var delta = 0
            for (insertion in insertions) {
                if (offset < insertion.start + delta) break
                if (offset <= insertion.start + delta + insertion.text.length)
                    return if (end) insertion.end else insertion.start
                delta += insertion.text.length - (insertion.end - insertion.start)
            }
            return offset - delta
        }
        val regions = (insertions.map { FormatRange(it.start, it.end) } +
            edits.map { FormatRange(originalOffset(it.start, false), originalOffset(it.end, true)) }).sortedBy { it.start }
        val merged = mutableListOf<FormatRange>()
        for (region in regions) {
            val previous = merged.lastOrNull()
            if (previous != null && region.start <= previous.end)
                merged[merged.lastIndex] = FormatRange(previous.start, maxOf(previous.end, region.end))
            else merged += region
        }
        val intermediate = StringBuilder(text).apply {
            insertions.asReversed().forEach { replace(it.start, it.end, it.text) }
        }.toString()
        return merged.map { region ->
            val start = region.start + insertions.filter { it.end <= region.start && it.start < region.start }
                .sumOf { it.text.length - (it.end - it.start) }
            val end = region.end + insertions.filter { it.start <= region.end }
                .sumOf { it.text.length - (it.end - it.start) }
            val value = StringBuilder(intermediate.substring(start, end))
            edits.filter { it.start >= start && it.end <= end }.asReversed().forEach {
                value.replace(it.start - start, it.end - start, it.text)
            }
            Replacement(region.start, region.end, value.toString())
        }
    }

    fun shiftedRanges(ranges: List<FormatRange>, insertions: List<Replacement>): List<FormatRange> = ranges.map { range ->
        FormatRange(range.start + insertions.filter { it.end <= range.start && it.start < range.start }
            .sumOf { it.text.length - (it.end - it.start) },
            range.end + insertions.filter { it.start <= range.end }.sumOf { it.text.length - (it.end - it.start) })
    }
}
