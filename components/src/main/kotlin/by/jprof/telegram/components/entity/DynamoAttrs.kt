package by.jprof.telegram.components.entity

import software.amazon.awssdk.services.dynamodb.model.AttributeValue

interface DynamoAttrs {
    fun serialize(): Map<String, AttributeValue>
}