package by.jprof.telegram.opinions.youtube.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val YOUTUBE_API_TOKEN = "YOUTUBE_API_TOKEN"
const val TABLE_YOUTUBE_CHANNELS_WHITELIST = "TABLE_YOUTUBE_CHANNELS_WHITELIST"

val youtubeEnvModule = module {
    listOf(
        YOUTUBE_API_TOKEN,
        TABLE_YOUTUBE_CHANNELS_WHITELIST
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
