package by.jprof.telegram.opinions.news.entity

import by.jprof.telegram.components.dao.require
import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.components.entity.DynamoAttrs
import by.jprof.telegram.components.entity.DynamoEntity
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

data class InsideJavaNewscastAttrs(
    val videoId: String
) : DynamoAttrs {
    override fun serialize(): Map<String, AttributeValue> = mapOf(
        "videoId" to this.videoId.toAttributeValue(),
    )

    override fun businessKey(): Map<String, AttributeValue> = mapOf(
        "videoId" to this.videoId.toAttributeValue()
    )

    companion object : DynamoEntity<InsideJavaNewscastAttrs> {
        override fun deserialize(attrs: Map<String, AttributeValue>) =
            InsideJavaNewscastAttrs(attrs.require("videoId").s())
    }
}