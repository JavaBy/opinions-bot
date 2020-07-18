package by.jprof.telegram.opinions.dao

import by.jprof.telegram.opinions.extension.toAttributeValue
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class YoutubeDAO(
        private val dynamoDB: DynamoDbAsyncClient,
        private val whiteListTable: String
) {

    suspend fun isInWhiteList(channelId: String): Boolean {
        val item = dynamoDB.getItem {
            it.tableName(whiteListTable)
            it.key(channelId.toYoutubeWhiteListItem())
        }.await().item()

        return !item.isNullOrEmpty()
    }

}

private fun String.toYoutubeWhiteListItem(): Map<String, AttributeValue> =
        mapOf("channelId" to this.toAttributeValue())
