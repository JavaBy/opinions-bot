package by.jprof.telegram.opinions.dao

import by.jprof.telegram.opinions.entity.Votes
import by.jprof.telegram.opinions.entity.toAttributeValues
import by.jprof.telegram.opinions.entity.toVotes
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

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
            it.key(mapOf("id" to AttributeValue.builder().s(id).build()))
        }.await()?.item()?.takeUnless { it.isEmpty() }?.toVotes()
    }
}
