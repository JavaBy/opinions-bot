package by.jprof.telegram.opinions.dao

import by.jprof.telegram.opinions.entity.Keyboard
import by.jprof.telegram.opinions.entity.toKeyboard
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
