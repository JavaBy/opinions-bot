package by.jprof.telegram.opinions.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"

val envModule = module {
    listOf(
            TELEGRAM_BOT_TOKEN
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
