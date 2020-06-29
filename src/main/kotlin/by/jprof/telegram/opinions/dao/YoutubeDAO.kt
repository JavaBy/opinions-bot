package by.jprof.telegram.opinions.dao

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

    suspend fun addToWhiteList(channelId: String) {
        dynamoDB.putItem {
            it.tableName(whiteListTable)
            it.item(channelId.toYoutubeWhiteListItem())
        }.await()
    }

    fun removeFromWhiteList(channelId: String) {
        dynamoDB.deleteItem {
            it.tableName(whiteListTable)
            it.key(channelId.toYoutubeWhiteListItem())
        }
    }

}

private fun String.toAttributeValue(): AttributeValue = AttributeValue.builder().s(this).build()
private fun String.toYoutubeWhiteListItem(): Map<String, AttributeValue> =
        mapOf("channelId" to this.toAttributeValue())
