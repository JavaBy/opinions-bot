package by.jprof.telegram.opinions.publication.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TABLE_CHATS = "TABLE_CHATS"

val publicationEnvModule = module {
    single(named(TABLE_CHATS)) {
        System.getenv(TABLE_CHATS)
    }
}