package by.jprof.telegram.opinions.publication

import by.jprof.telegram.components.dao.require
import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.components.entity.DynamoAttrs
import by.jprof.telegram.components.entity.DynamoEntity
import by.jprof.telegram.opinions.news.queue.Event
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class ChatAttrs(
    val event: Event,
    val chatId: String,
) : DynamoAttrs {
    override fun serialize(): Map<String, AttributeValue> = mapOf(
        "event" to this.event.name.toAttributeValue(),
        "chatId" to this.chatId.toAttributeValue()
    )

    override fun businessKey(): Map<String, AttributeValue> = mapOf(
        "chatId" to this.chatId.toAttributeValue()
    )

    companion object : DynamoEntity<ChatAttrs> {
        override fun deserialize(attrs: Map<String, AttributeValue>): ChatAttrs =
            ChatAttrs(
                event = Event.valueOf(attrs.require("event").s()),
                chatId = attrs.require("chatId").s()
            )
    }
}