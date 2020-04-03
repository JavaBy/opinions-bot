package by.jprof.telegram.opinions.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TELEGRAM_BOT_TOKEN = "TELEGRAM_BOT_TOKEN"
const val TABLE_VOTES = "TABLE_VOTES"

val envModule = module {
    listOf(
            TELEGRAM_BOT_TOKEN,
            TABLE_VOTES
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
