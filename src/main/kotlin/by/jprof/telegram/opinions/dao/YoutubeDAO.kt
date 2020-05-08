package by.jprof.telegram.opinions.dao

import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse

class YoutubeDAO(
        private val dynamoDB: DynamoDbAsyncClient,
        private val whiteListTable: String
) {

    suspend fun isInWhiteList(channelId: String) : Boolean {
        val item = getItemQuery(channelId).item()
        return !item.isNullOrEmpty()
    }

    private suspend fun getItemQuery(channelId: String): GetItemResponse {
        return dynamoDB.getItem {
            it.tableName(whiteListTable)
            it.key(mapOf("channelId" to AttributeValue.builder().s(channelId).build()))
        }.await()
    }

}