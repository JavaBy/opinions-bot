package by.jprof.telegram.opinions.webhook.processors

import by.jprof.telegram.opinions.processors.YoutubeLinksProcessor.Companion.youTubeVideoId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class YoutubeLinksProcessorRegexTest {
    @Test
    fun parseLink() {
        assertEquals("8zpJO7co1C0", "https://www.youtube.com/watch?v=8zpJO7co1C0".youTubeVideoId)
    }

    @Test
    fun parseShortLink() {
        assertEquals("8zpJO7co1C0", "https://youtu.be/8zpJO7co1C0".youTubeVideoId)
    }

    @Test
    fun parseShortNonYouTubeLink() {
        assertNull("https://goo.gl/8zpJO7co1C0".youTubeVideoId)
    }
}
