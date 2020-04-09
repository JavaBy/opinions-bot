package by.jprof.telegram.opinions.config

import by.jprof.telegram.opinions.dao.VotesDAO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import org.koin.dsl.module
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

val dynamoModule = module {
    single {
        val client = DynamoDbAsyncClient.builder().build()

        GlobalScope.launch {
            client.describeEndpoints().await()
        }

        client
    }

    single {
        VotesDAO(
                get(),
                get(named(TABLE_VOTES))
        )
    }
}
