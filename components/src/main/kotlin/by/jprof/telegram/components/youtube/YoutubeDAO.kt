package by.jprof.telegram.components.youtube

import by.jprof.telegram.components.dao.toAttributeValue
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class YoutubeDAO(
    private val dynamoDB: DynamoDbAsyncClient,
    private val whiteListTable: String
) {
    suspend fun isInWhiteList(channelId: String): Boolean {
        val item = dynamoDB.getItem {
            it.tableName(whiteListTable)
            it.key(mapOf("channelId" to channelId.toAttributeValue()))
        }.await().item()

        return !item.isNullOrEmpty()
    }
}
