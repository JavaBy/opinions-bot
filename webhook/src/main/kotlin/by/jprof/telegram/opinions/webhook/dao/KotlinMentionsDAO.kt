package by.jprof.telegram.opinions.webhook.dao

import by.jprof.telegram.components.dao.toAttributeValue
import by.jprof.telegram.opinions.webhook.entity.CHAT_ID_ATTR
import by.jprof.telegram.opinions.webhook.entity.KotlinMention
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class KotlinMentionsDAO(
        private val dynamoDb: DynamoDbAsyncClient,
        private val table: String
) {
    suspend fun save(mentions: KotlinMention) {
        dynamoDb.putItem {
            it.tableName(table)
            it.item(mentions.toAttrs())
        }.await()
    }

    suspend fun find(chatId: Long): KotlinMention? {
        return dynamoDb.getItem {
            it.tableName(table)
            it.key(mapOf(CHAT_ID_ATTR to chatId.toString().toAttributeValue()))
        }.await()?.item()?.takeUnless { it.isEmpty() }
                ?.let { KotlinMention.fromAttrs(it) }
    }
}
