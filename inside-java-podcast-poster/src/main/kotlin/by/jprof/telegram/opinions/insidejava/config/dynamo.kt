package by.jprof.telegram.opinions.insidejava.config

import by.jprof.telegram.opinions.insidejava.dao.InsideJavaDAO
import org.koin.core.qualifier.named
import org.koin.dsl.module
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

val dynamoModule = module {
    single {
        DynamoDbAsyncClient.create()
    }

    single {
        InsideJavaDAO(
                get(),
                get(named(TABLE_INSIDE_JAVA_PODCAST))
        )
    }
}
