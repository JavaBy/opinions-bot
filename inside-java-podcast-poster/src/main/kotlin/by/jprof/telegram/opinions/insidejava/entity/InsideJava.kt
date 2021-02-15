package by.jprof.telegram.opinions.insidejava.entity

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

data class InsideJava(
        val chatId: Long,
        val guids: Set<String>,
)

fun InsideJava.toAttributeValues(): Map<String, AttributeValue> = mapOf(
        "chatId" to AttributeValue.builder().s(this.chatId.toString()).build(),
        "guids" to AttributeValue.builder().ss(this.guids).build(),
)

fun Map<String, AttributeValue>.toInsideJava(): InsideJava = InsideJava(
        chatId = this["chatId"]?.s()?.toLong() ?: throw IllegalStateException("Missing chatId property"),
        guids = this["guids"]?.ss()?.filterNotNull()?.toSet() ?: emptySet()
)
