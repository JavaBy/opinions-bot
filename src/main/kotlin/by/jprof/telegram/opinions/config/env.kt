package by.jprof.telegram.opinions.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TABLE_VOTES = "TABLE_VOTES"
const val TABLE_YOUTUBE_CHANNELS_WHITELIST = "TABLE_YOUTUBE_CHANNELS_WHITELIST"
const val TABLE_KOTLIN_MENTIONS = "TABLE_KOTLIN_MENTIONS"
const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"
const val YOUTUBE_API_TOKEN = "YOUTUBE_API_TOKEN"
const val KOTLIN_MENTIONS_COOLDOWN_MS = "KOTLIN_MENTIONS_COOLDOWN_MS"

val envModule = module {
    listOf(
            TABLE_VOTES,
            TABLE_YOUTUBE_CHANNELS_WHITELIST,
            TABLE_KOTLIN_MENTIONS,
            TELEGRAM_BOT_TOKEN,
            YOUTUBE_API_TOKEN,
            KOTLIN_MENTIONS_COOLDOWN_MS
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
