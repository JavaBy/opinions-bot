package by.jprof.telegram.opinions.news.entity

import by.jprof.telegram.components.dao.require
import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.components.entity.DynamoAttrs
import by.jprof.telegram.components.entity.DynamoEntity
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

data class JepAttrs(
    val id: String,
    val jep: String
) : DynamoAttrs {
    override fun serialize() = mapOf(
        "id" to id.toAttributeValue(),
        "jep" to jep.toAttributeValue()
    )

    override fun businessKey() = mapOf(
        "id" to id.toAttributeValue()
    )

    companion object : DynamoEntity<JepAttrs> {
        override fun deserialize(attrs: Map<String, AttributeValue>) =
            JepAttrs(
                attrs.require("id").s(),
                attrs.require("jep").s()
            )
    }
}