package by.jprof.telegram.opinions.dao

import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest
import java.time.Instant

class KotlinMentionsDAO(
        private val dynamoDb: DynamoDbAsyncClient,
        private val table: String
) {
    companion object {
        private const val ID_ATTR = "chatId"
    }

    @Volatile
    private var initialized = false

    suspend fun updateKotlinMentionInfo(id: String, userId: String) {
        val builder = UpdateItemRequest.builder()
                .tableName(table)
                .key(mapOf(ID_ATTR to AttributeValue.builder().s(id).build()))

        if (!initialized) {
            // small optimization: create "users" map attr only once, because it's top level attribute
            dynamoDb.updateItem(builder.applyMutation {
                it.updateExpression("SET #u = if_not_exists(#u, :empty)")
                it.expressionAttributeNames(mapOf("#u" to "users"))
                it.expressionAttributeValues(mapOf(":empty" to AttributeValue.builder().m(mapOf()).build()))
            }.build()).await()
            initialized = true
        }

        dynamoDb.updateItem(builder.applyMutation {
            it.updateExpression("SET #u.#user_id = if_not_exists(#u.#user_id, :empty)")
            it.expressionAttributeNames(mapOf("#u" to "users", "#user_id" to userId))
            it.expressionAttributeValues(mapOf(":empty" to AttributeValue.builder().m(mapOf(
                    "count" to AttributeValue.builder().n("0").build(),
                    "last_time" to AttributeValue.builder().n("0").build()
            )).build()))
        }.build()).await()

        dynamoDb.updateItem(builder.applyMutation {
            it.updateExpression("""
                SET #u.#user_id.#c = #u.#user_id.#c + :inc,
                    #u.#user_id.last_time = :last_time,
                    #t = :last_time
            """.trimIndent())
            it.expressionAttributeNames(mapOf(
                    "#t" to "timestamp",
                    "#c" to "count",
                    "#u" to "users",
                    "#user_id" to userId))
            it.expressionAttributeValues(mapOf(
                    ":last_time" to AttributeValue.builder().n(Instant.now().toEpochMilli().toString()).build(),
                    ":inc" to AttributeValue.builder().n("1").build()))
        }.build())
    }

    suspend fun getKotlinLastMentionAt(id: String): Instant? {
        return dynamoDb.getItem {
            it.tableName(table)
            it.key(mapOf(ID_ATTR to AttributeValue.builder().s(id).build()))
            it.projectionExpression("#t")
            it.expressionAttributeNames(mapOf("#t" to "timestamp"))
        }.await()?.item()?.takeUnless { it.isEmpty() }
                ?.let {
                    val epoch = it["timestamp"] ?: throw IllegalStateException("Missing 'timestamp' property")
                    Instant.ofEpochMilli(epoch.n().toLong())
                }
    }
}
