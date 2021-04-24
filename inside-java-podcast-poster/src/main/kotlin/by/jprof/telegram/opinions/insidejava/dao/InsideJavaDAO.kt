package by.jprof.telegram.opinions.insidejava.dao

import by.jprof.telegram.opinions.insidejava.entity.InsideJava
import by.jprof.telegram.opinions.insidejava.entity.toAttributeValues
import by.jprof.telegram.opinions.insidejava.entity.toInsideJava
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class InsideJavaDAO(
    private val dynamoDb: DynamoDbAsyncClient,
    private val table: String
) {
    suspend fun list(): List<InsideJava> {
        return dynamoDb.scan {
            it.tableName(table)
        }.await()?.items()?.map { it.toInsideJava() } ?: emptyList()
    }

    suspend fun save(value: InsideJava) {
        dynamoDb.putItem {
            it.tableName(table)
            it.item(value.toAttributeValues())
        }.await()
    }
}
