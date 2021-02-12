package by.jprof.telegram.opinions.webhook.dao

import by.jprof.telegram.opinions.webhook.entity.Votes
import by.jprof.telegram.opinions.webhook.entity.toAttributeValues
import by.jprof.telegram.opinions.webhook.entity.toVotes
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class VotesDAO(
        private val dynamoDb: DynamoDbAsyncClient,
        private val table: String
) {
    suspend fun save(votes: Votes) {
        dynamoDb.putItem {
            it.tableName(table)
            it.item(votes.toAttributeValues())
        }.await()
    }

    suspend fun get(id: String): Votes? {
        return dynamoDb.getItem {
            it.tableName(table)
            it.key(mapOf("id" to id.toAttributeValue()))
        }.await()?.item()?.takeUnless { it.isEmpty() }?.toVotes()
    }
}
