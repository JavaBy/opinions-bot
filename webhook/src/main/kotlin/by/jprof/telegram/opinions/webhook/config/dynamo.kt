package by.jprof.telegram.opinions.webhook.config

import by.jprof.telegram.opinions.webhook.dao.KeyboardsDAO
import by.jprof.telegram.opinions.webhook.dao.KotlinMentionsDAO
import by.jprof.telegram.opinions.webhook.dao.VotesDAO
import by.jprof.telegram.opinions.webhook.dao.YoutubeDAO
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
