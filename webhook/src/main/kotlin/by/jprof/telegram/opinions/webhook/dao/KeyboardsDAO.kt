package by.jprof.telegram.opinions.webhook.dao

import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.opinions.webhook.entity.Keyboard
import by.jprof.telegram.opinions.webhook.entity.toKeyboard
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class KeyboardsDAO(
        private val dynamoDb: DynamoDbAsyncClient,
        private val table: String
) {
    suspend fun get(id: String): Keyboard? {
        return dynamoDb.getItem {
            it.tableName(table)
            it.key(mapOf("id" to id.toAttributeValue()))
        }.await()?.item()?.takeUnless { it.isEmpty() }?.toKeyboard()
    }
}
