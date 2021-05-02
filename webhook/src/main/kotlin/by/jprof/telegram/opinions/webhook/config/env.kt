package by.jprof.telegram.opinions.webhook.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TABLE_KOTLIN_MENTIONS = "TABLE_KOTLIN_MENTIONS"
const val TABLE_KEYBOARDS = "TABLE_KEYBOARDS"

val envModule = module {
    listOf(
            TABLE_KOTLIN_MENTIONS,
            TABLE_KEYBOARDS,
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
