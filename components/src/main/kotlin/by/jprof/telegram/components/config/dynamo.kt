package by.jprof.telegram.components.config

import org.koin.core.qualifier.named
import org.koin.dsl.module
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient


val componentsDynamoModule = module {
    single {
        DynamoDbAsyncClient.create()
    }
}
