package by.jprof.telegram.opinions.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"
const val TABLE_VOTES = "TABLE_VOTES"
const val YOUTUBE_CHANNELS_WHITELIST_TABLE = "YOUTUBE_CHANNELS_WHITELIST_TABLE"
const val YOUTUBE_API_TOKEN = "YOUTUBE_API_TOKEN"

val envModule = module {
    listOf(
            TELEGRAM_BOT_TOKEN,
            TABLE_VOTES,
            YOUTUBE_API_TOKEN,
            YOUTUBE_CHANNELS_WHITELIST_TABLE
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
