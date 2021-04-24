package by.jprof.telegram.opinions.news.entity

import by.jprof.telegram.components.dao.require
import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.components.entity.DynamoAttrs
import by.jprof.telegram.components.entity.DynamoEntity
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

data class InsideJavaAttrs(
    val chatId: String,
    val caption: String,
    val fileId: String? = null,
) : DynamoAttrs {
    override fun serialize(): Map<String, AttributeValue> {
        return mapOf(
            "chatId" to this.chatId.toAttributeValue(),
            "caption" to this.caption.toAttributeValue(),
            "fileId" to (this.fileId ?: "").toAttributeValue()
        )
    }

    companion object : DynamoEntity<InsideJavaAttrs> {
        override fun deserialize(attrs: Map<String, AttributeValue>): InsideJavaAttrs =
            InsideJavaAttrs(
                attrs.require("chatId").s(),
                attrs.require("caption").s(),
                attrs["fileId"]?.s()
            )
    }
}