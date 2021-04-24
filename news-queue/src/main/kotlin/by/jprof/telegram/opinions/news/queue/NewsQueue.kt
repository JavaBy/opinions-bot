package by.jprof.telegram.opinions.news.queue

import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.components.entity.DynamoAttrs
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

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
                    "#processed" to "processed"
                )
            )
            it.expressionAttributeValues(
                mapOf(
                    ":processed" to AttributeValue.builder().bool(true).build()
                )
            )
            it.key(item.key())
            it.updateExpression("SET #processed = :processed")
        }.await()
    }


    suspend fun news(kind: Kind): List<QueueItem<DynamoAttrs>> {
        return dynamoDb.query {
            it.tableName(table)
            it.expressionAttributeValues(
                mapOf(
                    ":kind" to kind.name.toAttributeValue(),
                    ":processed" to AttributeValue.builder().bool(false).build()
                )
            )
            it.expressionAttributeNames(
                mapOf(
                    "#processed" to "processed"
                )
            )
            it.keyConditionExpression("kind = :kind")
            it.filterExpression("#processed = :processed")
        }.await()?.items()?.map { QueueItem.deserialize(it) }
            ?: emptyList()
    }
}