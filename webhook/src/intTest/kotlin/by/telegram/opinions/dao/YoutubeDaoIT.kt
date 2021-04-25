package by.telegram.opinions.dao

import by.dev.madhead.aws_junit5.common.AWSClient
import by.dev.madhead.aws_junit5.common.AWSEndpoint
import by.dev.madhead.aws_junit5.dynamo.v2.DynamoDB
import by.jprof.telegram.opinions.youtube.YoutubeDAO
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

@ExtendWith(DynamoDB::class)
class YoutubeDaoIT {
    @AWSClient(endpoint = Endpoint::class)
    lateinit var dynamoDB: DynamoDbAsyncClient

    @Test
    fun daoReturnTrueIfKeyIsPresentInAWhiteList() = runBlocking {
        assertNotNull(dynamoDB)
        val youtubeDAO = YoutubeDAO(dynamoDB, "youtube-whitelist")
        assertTrue(youtubeDAO.isInWhiteList("TEST-125"))
    }

    class Endpoint : AWSEndpoint {
        private val dummy = "local"
        override fun region(): String = dummy
        override fun accessKey(): String = dummy
        override fun secretKey(): String = dummy
        override fun url(): String = "http://localhost:4569" //default for localstack
    }
}
