package by.jprof.telegram.opinions.news.queue

import by.jprof.telegram.components.dao.require
import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.components.entity.DynamoAttrs
import by.jprof.telegram.components.entity.DynamoEntity
import by.jprof.telegram.opinions.news.entity.InsideJavaAttrs
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant

data class QueueItem<T : DynamoAttrs>(
    val kind: Kind,
    val payload: T,
    val createdAt: Instant?,
    val queuedAt: Instant = Instant.now(),
    val processed: Boolean = false
) : DynamoAttrs {
    override fun serialize(): Map<String, AttributeValue> = mapOf(
        "kind" to this.kind.name.toAttributeValue(),
        "payload" to this.payload.serialize().toAttributeValue(),
        "createdAt" to (this.createdAt ?: "").toString().toAttributeValue(),
        "queuedAt" to queuedAt.toString().toAttributeValue(),
        "processed" to AttributeValue.builder().bool(this.processed).build()
    )

    fun key(): Map<String, AttributeValue> = mapOf(
        "kind" to this.kind.name.toAttributeValue(),
        "queuedAt" to queuedAt.toString().toAttributeValue()
    )

    companion object : DynamoEntity<QueueItem<DynamoAttrs>> {
        override fun deserialize(attrs: Map<String, AttributeValue>): QueueItem<DynamoAttrs> {
            val kind = Kind.valueOf(attrs.require("kind").s())
            if (kind == Kind.INSIDE_JAVA_PODCAST) {
                return QueueItem(
                    kind,
                    InsideJavaAttrs.deserialize(attrs.require("payload").m()),
                    (attrs.require("createdAt").s().ifEmpty { null })?.let { Instant.parse(it) },
                    Instant.parse(attrs.require("queuedAt").s()),
                    attrs.require("processed").bool()
                )
            }
            throw IllegalStateException("Unexpected queue item kind $kind. Attrs: $attrs")
        }
    }
}

enum class Kind {
    INSIDE_JAVA_PODCAST
}