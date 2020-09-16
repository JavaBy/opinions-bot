package by.jprof.telegram.opinions.config

import by.jprof.telegram.opinions.dao.KeyboardsDAO
import by.jprof.telegram.opinions.dao.KotlinMentionsDAO
import by.jprof.telegram.opinions.dao.VotesDAO
import by.jprof.telegram.opinions.dao.YoutubeDAO
import org.koin.core.qualifier.named
import org.koin.dsl.module
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

val dynamoModule = module {
    single {
        DynamoDbAsyncClient.create()
    }

    single {
        VotesDAO(
                get(),
                get(named(TABLE_VOTES))
        )
    }

    single {
        YoutubeDAO(
                get(),
                get(named(TABLE_YOUTUBE_CHANNELS_WHITELIST))
        )
    }

    single {
        KotlinMentionsDAO(
                get(),
                get(named(TABLE_KOTLIN_MENTIONS))
        )
    }

    single {
        KeyboardsDAO(
                get(),
                get(named(TABLE_KEYBOARDS))
        )
    }
}
