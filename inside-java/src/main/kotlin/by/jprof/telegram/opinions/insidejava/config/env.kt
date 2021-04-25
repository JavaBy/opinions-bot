package by.jprof.telegram.opinions.insidejava.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val RSS_URL = "RSS_URL"

val envModule = module {
    single(named(RSS_URL)) {
        System.getenv(RSS_URL) ?: "http://insidejava.libsyn.com/rss"
    }
}
