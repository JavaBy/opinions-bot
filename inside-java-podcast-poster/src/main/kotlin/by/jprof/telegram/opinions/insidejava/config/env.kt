package by.jprof.telegram.opinions.insidejava.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val RSS_URL = "RSS_URL"
const val TABLE_INSIDE_JAVA_PODCAST = "TABLE_INSIDE_JAVA_PODCAST"
const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"

val envModule = module {
    single(named(RSS_URL)) {
        System.getenv(RSS_URL) ?: "http://insidejava.libsyn.com/rss"
    }

    listOf(
            TABLE_INSIDE_JAVA_PODCAST,
            TELEGRAM_BOT_TOKEN,
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
