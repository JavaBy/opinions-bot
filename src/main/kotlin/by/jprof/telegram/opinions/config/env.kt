package by.jprof.telegram.opinions.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"
const val TABLE_VOTES = "TABLE_VOTES"
const val TABLE_YOUTUBE_CHANNELS_WHITELIST = "TABLE_YOUTUBE_CHANNELS_WHITELIST"
const val YOUTUBE_API_TOKEN = "YOUTUBE_API_TOKEN"

val envModule = module {
    listOf(
            TELEGRAM_BOT_TOKEN,
            TABLE_VOTES,
            YOUTUBE_API_TOKEN,
            TABLE_YOUTUBE_CHANNELS_WHITELIST
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
