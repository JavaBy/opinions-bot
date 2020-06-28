package by.jprof.telegram.opinions.processors

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class YouTubeRegexTest {
     companion object {
        val siteRegex = """
            http(?:s?):\/\/(?:www\.)?youtu(?:be\.com\/watch\?v=|\.be\/)([\w\-\_]*)(&(amp;)?‌​[\w\?‌​=]*)?
        """.trimIndent().toRegex()
     }

    @Test
    fun `id is at index 1 on full link`() {
        val link = "https://www.youtube.com/watch?v=8zpJO7co1C0"
        val matches = siteRegex.matches(link)

        assertTrue(matches)

        val match = siteRegex.matchEntire(link)
        val groupValues = match?.groupValues
        val id = groupValues?.get(1)

        assertFalse(id.isNullOrBlank())
    }

    @Test
    fun `id is at index 1 on shorten link`() {
        val shortenLink = "https://youtu.be/8zpJO7co1C0"
        val matches = siteRegex.matches(shortenLink)

        assertTrue(matches)

        val match = siteRegex.matchEntire(shortenLink)
        val groupValues = match?.groupValues
        val id = groupValues?.get(1)

        assertFalse(id.isNullOrBlank())
    }

}