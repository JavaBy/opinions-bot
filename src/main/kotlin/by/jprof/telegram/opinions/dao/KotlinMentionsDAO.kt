package by.jprof.telegram.opinions.dao

import by.jprof.telegram.opinions.entity.KotlinMention
import by.jprof.telegram.opinions.entity.toAttributeValues
import by.jprof.telegram.opinions.entity.toKotlinMention
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class KotlinMentionsDAO(
        private val dynamoDb: DynamoDbAsyncClient,
        private val table: String
) {
    suspend fun save(kotlinMention: KotlinMention) {
        dynamoDb.putItem {
            it.tableName(table)
            it.item(kotlinMention.toAttributeValues())
        }.await()
    }

    suspend fun get(id: String): KotlinMention? {
        return dynamoDb.getItem {
            it.tableName(table)
            it.key(mapOf("chatId" to id.toAttributeValue()))
        }.await()?.item()?.takeUnless { it.isEmpty() }?.toKotlinMention()
    }
}
