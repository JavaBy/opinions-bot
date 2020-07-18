package by.jprof.telegram.opinions.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TABLE_VOTES = "TABLE_VOTES"
const val TABLE_YOUTUBE_CHANNELS_WHITELIST = "TABLE_YOUTUBE_CHANNELS_WHITELIST"
const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"
const val YOUTUBE_API_TOKEN = "YOUTUBE_API_TOKEN"

val envModule = module {
    listOf(
            TABLE_VOTES,
            TABLE_YOUTUBE_CHANNELS_WHITELIST,
            TELEGRAM_BOT_TOKEN,
            YOUTUBE_API_TOKEN
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
