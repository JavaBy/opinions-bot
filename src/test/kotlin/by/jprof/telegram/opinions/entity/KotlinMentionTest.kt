package by.jprof.telegram.opinions.entity

import by.jprof.telegram.opinions.dao.toAttributeValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant
import java.util.concurrent.TimeUnit

internal class KotlinMentionTest {
    @Test
    fun `test update user stats`() {
        val now = Instant.now()
        val mention = KotlinMention(1, now, mapOf(111L to MentionStats(1, Instant.now())))
        TimeUnit.MILLISECONDS.sleep(1) // to ensure lastUpdatedAt > now
        val updatedMention = mention.update(111L, now)
        assertEquals(1, updatedMention.users.size)
        val (count, lastUpdatedAt) = updatedMention.users[111L] ?: fail("user")
        assertEquals(2, count)
        assertTrue(lastUpdatedAt.isAfter(now))
        assertEquals(now, updatedMention.timestamp)
    }

    @Test
    fun `test kotlin stats mapping with missing attr then should throw`() {
        val chatIdError = assertThrows<IllegalStateException> { KotlinMention.fromAttrs(mapOf()) }
        assertEquals(chatIdError.localizedMessage, "Missing '$CHAT_ID_ATTR' attribute!")

        val timestampError = assertThrows<IllegalStateException> {
            KotlinMention.fromAttrs(mapOf(CHAT_ID_ATTR to "1".toAttributeValue()))
        }
        assertEquals(timestampError.localizedMessage, "Missing '$TIMESTAMP_ATTR' attribute!")

        val countError = assertThrows<IllegalStateException> {
            KotlinMention.fromAttrs(mapOf(
                    CHAT_ID_ATTR to "1".toAttributeValue(),
                    TIMESTAMP_ATTR to 123456.toAttributeValue(),
                    USERS_ATTR to mapOf<String, AttributeValue>(
                            "111" to mapOf<String, AttributeValue>().toAttributeValue()
                    ).toAttributeValue()
            ))
        }
        assertEquals(countError.localizedMessage, "Missing '$COUNT_ATTR' attribute!")

        val lastUpdatedAtError = assertThrows<IllegalStateException> {
            KotlinMention.fromAttrs(mapOf(
                    CHAT_ID_ATTR to "1".toAttributeValue(),
                    TIMESTAMP_ATTR to 123456.toAttributeValue(),
                    USERS_ATTR to mapOf<String, AttributeValue>(
                            "111" to mapOf(
                                    COUNT_ATTR to 123.toAttributeValue()
                            ).toAttributeValue()
                    ).toAttributeValue()
            ))
        }
        assertEquals(lastUpdatedAtError.localizedMessage, "Missing '$LAST_UPDATED_AT_ATTR' attribute!")
    }

    @Test
    fun `test mapping kotlin stats to attributes`() {
        val now = Instant.now()
        assertEquals(
                KotlinMention(1, now, mapOf(1L to MentionStats(1, now))).toAttrs(),
                mapOf<String, AttributeValue>(
                        CHAT_ID_ATTR to "1".toAttributeValue(),
                        TIMESTAMP_ATTR to now.toEpochMilli().toAttributeValue(),
                        USERS_ATTR to mapOf(
                                "1" to mapOf(
                                        COUNT_ATTR to 1.toAttributeValue(),
                                        LAST_UPDATED_AT_ATTR to now.toEpochMilli().toAttributeValue()
                                ).toAttributeValue()
                        ).toAttributeValue()
                )
        )
    }
}