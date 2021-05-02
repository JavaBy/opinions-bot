package by.jprof.telegram.components.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"

val componentsEnvModule = module {
    listOf(
        TELEGRAM_BOT_TOKEN
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
