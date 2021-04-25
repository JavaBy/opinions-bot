package by.jprof.telegram.opinions.news.entity

import by.jprof.telegram.components.dao.require
import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.components.entity.DynamoAttrs
import by.jprof.telegram.components.entity.DynamoEntity
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

data class InsideJavaPodcastAttrs(
    val caption: String,
    val guid: String,
    val fileId: String? = null,
) : DynamoAttrs {
    override fun serialize(): Map<String, AttributeValue> {
        return mapOf(
            "caption" to this.caption.toAttributeValue(),
            "guid" to this.guid.toAttributeValue(),
            "fileId" to (this.fileId ?: "").toAttributeValue()
        )
    }

    override fun businessKey(): Map<String, AttributeValue> = mapOf(
        "guid" to this.guid.toAttributeValue()
    )

    companion object : DynamoEntity<InsideJavaPodcastAttrs> {
        override fun deserialize(attrs: Map<String, AttributeValue>): InsideJavaPodcastAttrs =
            InsideJavaPodcastAttrs(
                attrs.require("caption").s(),
                attrs.require("guid").s(),
                attrs["fileId"]?.s()
            )
    }
}