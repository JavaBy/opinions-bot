package by.jprof.telegram.opinions.entity

import by.jprof.telegram.opinions.dao.toAttributeValue
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

data class KotlinMention(
        val chatId: String,
        val timestamp: Long
)

fun KotlinMention.toAttributeValues(): Map<String, AttributeValue> = mapOf(
        "chatId" to this.chatId.toAttributeValue(),
        "timestamp" to this.timestamp.toAttributeValue()
)

fun Map<String, AttributeValue>.toKotlinMention(): KotlinMention = KotlinMention(
        chatId = this["chatId"]?.s() ?: throw IllegalStateException("Missing id property"),
        timestamp = this["timestamp"]?.n()?.toLong() ?: throw IllegalStateException("Missing timestamp property")
)
