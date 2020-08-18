package by.jprof.telegram.opinions.entity

import by.jprof.telegram.opinions.dao.require
import by.jprof.telegram.opinions.dao.toAttributeValue
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant

const val CHAT_ID_ATTR = "chatId"
const val TIMESTAMP_ATTR = "timestamp"
const val USERS_ATTR = "users"
const val COUNT_ATTR = "count"
const val LAST_UPDATED_AT_ATTR = "lastUpdatedAt"

data class KotlinMention(
        val chatId: Long,
        val timestamp: Instant,
        val users: Map<Long, MentionStats> = mapOf()
) {
    companion object {
        fun fromAttrs(data: Map<String, AttributeValue>): KotlinMention = KotlinMention(
                data.require(CHAT_ID_ATTR).s().toLong(),
                Instant.ofEpochMilli(data.require(TIMESTAMP_ATTR).n().toLong()),
                (data[USERS_ATTR]?.m() ?: emptyMap<String, AttributeValue>())
                        .mapKeys { (key, _) -> key.toLong() }
                        .mapValues { (_, value) ->
                            MentionStats(
                                    value.m().require(COUNT_ATTR).n().toLong(),
                                    Instant.ofEpochMilli(
                                            value.m().require(LAST_UPDATED_AT_ATTR).n().toLong()))
                        }
        )
    }

    fun updateUserStats(userId: Long): KotlinMention {
        val updatedUsers = users.toMutableMap()
        val stats = updatedUsers[userId] ?: MentionStats(0, Instant.now())
        updatedUsers[userId] = MentionStats(stats.count + 1, Instant.now())
        return copy(users = updatedUsers)
    }

    fun toAttrs(): Map<String, AttributeValue> = mapOf<String, AttributeValue>(
            CHAT_ID_ATTR to chatId.toString().toAttributeValue(),
            TIMESTAMP_ATTR to timestamp.toEpochMilli().toAttributeValue(),
            USERS_ATTR to users.mapValues { (_, stats) ->
                mapOf(
                        COUNT_ATTR to stats.count.toAttributeValue(),
                        LAST_UPDATED_AT_ATTR to stats.lastUpdatedAt.toEpochMilli().toAttributeValue()
                ).toAttributeValue()
            }.mapKeys { it.key.toString() }.toAttributeValue()
    )
}

data class MentionStats(
        val count: Long,
        val lastUpdatedAt: Instant
)