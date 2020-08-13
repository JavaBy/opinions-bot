package by.jprof.telegram.opinions.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.TimeUnit

internal class KotlinMentionTest {
    @Test
    fun testUpdateUserStats() {
        val now = Instant.now()
        val mention = KotlinMention(1, now, mapOf(111L to MentionStats(1, Instant.now())))
        TimeUnit.MILLISECONDS.sleep(1) // to ensure lastUpdatedAt > now
        val updatedMention = mention.updateUserStats(111L)
        assertEquals(1, updatedMention.users.size)
        val (count, lastUpdatedAt) = updatedMention.users[111L] ?: fail("user")
        assertEquals(2, count)
        assertTrue(lastUpdatedAt.isAfter(now))
    }
}