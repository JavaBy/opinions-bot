package by.jprof.telegram.opinions.dao

import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant

class KotlinMentionsDAO(
        private val dynamoDb: DynamoDbAsyncClient,
        private val table: String
) {
    companion object {
        private const val ID_ATTR = "chatId"
        private const val VALUE_ATTR = "timestamp"
    }

    suspend fun updateKotlinLastMentionAt(id: String, instant: Instant) {
        dynamoDb.putItem {
            it.tableName(table)
            it.item(
                    mapOf(
                            ID_ATTR to AttributeValue.builder().s(id).build(),
                            VALUE_ATTR to AttributeValue.builder().n(instant.toEpochMilli().toString()).build()
                    )
            )
        }.await()
    }

    suspend fun getKotlinLastMentionAt(id: String): Instant? {
        return dynamoDb.getItem {
            it.tableName(table)
            it.key(mapOf(ID_ATTR to AttributeValue.builder().s(id).build()))
        }.await()?.item()?.takeUnless { it.isEmpty() }
                ?.let {
                    val epoch = it[VALUE_ATTR] ?: throw IllegalStateException("Missing $VALUE_ATTR property")
                    Instant.ofEpochMilli(epoch.n().toLong())
                }
    }
}
