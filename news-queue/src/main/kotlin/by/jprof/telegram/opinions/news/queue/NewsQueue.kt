package by.jprof.telegram.opinions.news.queue

import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.components.entity.DynamoAttrs
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant

class NewsQueue(
    private val dynamoDb: DynamoDbAsyncClient,
    private val table: String
) {
    suspend fun push(item: QueueItem<*>) {
        dynamoDb.putItem {
            it.tableName(table)
            it.item(item.serialize())
        }.await()
    }

    suspend fun markProcessed(item: QueueItem<*>) {
        dynamoDb.updateItem {
            it.tableName(table)
            it.expressionAttributeNames(
                mapOf(
                    "#processed" to "processed",
                    "#processedAt" to "processedAt",
                    "#businessKey" to "businessKey"
                )
            )
            it.expressionAttributeValues(
                mapOf(
                    ":processed" to AttributeValue.builder().bool(true).build(),
                    ":processedAt" to AttributeValue.builder().s(Instant.now().toString()).build(),
                    ":businessKey" to item.businessKey().toAttributeValue()
                )
            )
            it.key(item.key())
            it.conditionExpression("#businessKey = :businessKey")
            it.updateExpression("SET #processed = :processed, #processedAt = :processedAt")
        }.await()
    }

    suspend fun isProcessed(item: QueueItem<*>): Boolean? {
        return dynamoDb.query {
            it.tableName(table)
            it.expressionAttributeNames(
                mapOf(
                    "#event" to "event",
                    "#processed" to "processed",
                    "#businessKey" to "businessKey"
                )
            )
            it.expressionAttributeValues(
                mapOf(
                    ":event" to item.event.name.toAttributeValue(),
                    ":processed" to AttributeValue.builder().bool(false).build(),
                    ":businessKey" to item.businessKey().toAttributeValue()
                )
            )
            it.keyConditionExpression("#event = :event")
            it.filterExpression("#businessKey = :businessKey")
        }?.await()?.hasItems()
    }


    suspend fun <T : DynamoAttrs> news(event: Event): List<QueueItem<T>> {
        return dynamoDb.query {
            it.tableName(table)
            it.expressionAttributeValues(
                mapOf(
                    ":event" to event.name.toAttributeValue(),
                    ":processed" to AttributeValue.builder().bool(false).build()
                )
            )
            it.expressionAttributeNames(
                mapOf(
                    "#processed" to "processed"
                )
            )
            it.keyConditionExpression("event = :event")
            it.filterExpression("#processed = :processed")
        }.await()?.items()?.map {
            QueueItem.deserialize(it) as QueueItem<T>
        } ?: emptyList()
    }

    suspend fun <T : DynamoAttrs> findAll(event: Event): List<QueueItem<T>> {
        return dynamoDb.query {
            it.tableName(table)
            it.expressionAttributeNames(mapOf(
                "#event" to "event"
            ))
            it.expressionAttributeValues(
                mapOf(
                    ":event" to event.name.toAttributeValue(),
                )
            )
            it.keyConditionExpression("#event = :event")
        }.await()?.items()?.map {
            QueueItem.deserialize(it) as QueueItem<T>
        } ?: emptyList()
    }
}