package by.jprof.telegram.opinions.news.queue

import by.jprof.telegram.components.dao.require
import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.components.entity.DynamoAttrs
import by.jprof.telegram.components.entity.DynamoEntity
import by.jprof.telegram.opinions.news.entity.InsideJavaNewscastAttrs
import by.jprof.telegram.opinions.news.entity.InsideJavaPodcastAttrs
import by.jprof.telegram.opinions.news.entity.JepAttrs
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant

data class QueueItem<T : DynamoAttrs>(
    val event: Event,
    val payload: T,
    val createdAt: Instant?,
    val processedAt: Instant? = null,
    val queuedAt: Instant = Instant.now(),
    val processed: Boolean = false
) : DynamoAttrs {
    override fun serialize(): Map<String, AttributeValue> = mapOf(
        "event" to this.event.name.toAttributeValue(),
        "payload" to this.payload.serialize().toAttributeValue(),
        "createdAt" to (this.createdAt ?: "").toString().toAttributeValue(),
        "processedAt" to (this.processedAt ?: "").toString().toAttributeValue(),
        "queuedAt" to queuedAt.toString().toAttributeValue(),
        "processed" to AttributeValue.builder().bool(this.processed).build(),
        "businessKey" to this.payload.businessKey().toAttributeValue()
    )

    override fun businessKey(): Map<String, AttributeValue> = this.payload.businessKey()

    fun key(): Map<String, AttributeValue> = mapOf(
        "event" to this.event.name.toAttributeValue(),
        "queuedAt" to queuedAt.toString().toAttributeValue()
    )

    companion object : DynamoEntity<QueueItem<DynamoAttrs>> {
        override fun deserialize(attrs: Map<String, AttributeValue>): QueueItem<DynamoAttrs> =
            Event.valueOf(attrs.require("event").s()).let { event ->
                val payload = attrs.require("payload").m()
                QueueItem(
                    event,
                    when (event) {
                        Event.INSIDE_JAVA_PODCAST -> InsideJavaPodcastAttrs.deserialize(payload)
                        Event.INSIDE_JAVA_NEWSCAST -> InsideJavaNewscastAttrs.deserialize(payload)
                        Event.JEP -> JepAttrs.deserialize(payload)
                    },
                    (attrs.require("createdAt").s().ifEmpty { null })?.let { Instant.parse(it) },
                    (attrs.require("processedAt").s().ifEmpty { null })?.let { Instant.parse(it) },
                    Instant.parse(attrs.require("queuedAt").s()),
                    attrs.require("processed").bool()
                )
            }
    }
}

enum class Event {
    INSIDE_JAVA_PODCAST,
    INSIDE_JAVA_NEWSCAST,
    JEP
}