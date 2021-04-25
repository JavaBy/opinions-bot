package by.jprof.telegram.opinions.publication

import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.opinions.news.queue.Kind
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class ChatDao(
    private val dynamoDb: DynamoDbAsyncClient,
    private val table: String
) {
    suspend fun findAll(event: Kind): List<ChatAttrs> {
        return dynamoDb.query {
            it.tableName(table)
            it.expressionAttributeNames(
                mapOf(
                    "#event" to "event"
                )
            )
            it.expressionAttributeValues(
                mapOf(
                    ":event" to event.name.toAttributeValue()
                )
            )
            it.keyConditionExpression("#event = :event")
        }.await()?.items()?.map {
            ChatAttrs.deserialize(it)
        } ?: emptyList()
    }
}