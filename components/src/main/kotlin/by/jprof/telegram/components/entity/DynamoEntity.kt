package by.jprof.telegram.components.entity

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

interface DynamoEntity<T : DynamoAttrs> {
    fun deserialize(attrs: Map<String, AttributeValue>): T
}