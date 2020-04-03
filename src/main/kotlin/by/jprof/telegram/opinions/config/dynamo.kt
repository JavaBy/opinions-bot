package by.jprof.telegram.opinions.config

import by.jprof.telegram.opinions.dao.VotesDAO
import org.koin.core.qualifier.named
import org.koin.dsl.module
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

val dynamoModule = module {
    single {
        DynamoDbAsyncClient.builder().build()
    }

    single {
        VotesDAO(
                get(),
                get(named(TABLE_VOTES))
        )
    }
}
