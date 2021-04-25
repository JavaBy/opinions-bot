package by.jprof.telegram.opinions.youtube.config

import by.jprof.telegram.opinions.youtube.YoutubeDAO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val youtubeDynamoModule = module {
    single {
        YoutubeDAO(get(), get(named(TABLE_YOUTUBE_CHANNELS_WHITELIST)))
    }
}