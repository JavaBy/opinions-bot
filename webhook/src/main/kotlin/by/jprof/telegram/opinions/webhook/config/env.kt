package by.jprof.telegram.opinions.webhook.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TABLE_YOUTUBE_CHANNELS_WHITELIST = "TABLE_YOUTUBE_CHANNELS_WHITELIST"
const val TABLE_KOTLIN_MENTIONS = "TABLE_KOTLIN_MENTIONS"
const val TABLE_KEYBOARDS = "TABLE_KEYBOARDS"
const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"

val envModule = module {
    listOf(
            TABLE_YOUTUBE_CHANNELS_WHITELIST,
            TABLE_KOTLIN_MENTIONS,
            TABLE_KEYBOARDS,
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
