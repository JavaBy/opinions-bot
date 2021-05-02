package by.jprof.telegram.opinions.publication.config

import by.jprof.telegram.opinions.publication.ChatDao
import org.koin.core.qualifier.named
import org.koin.dsl.module

val publicationDynamoModule = module {
    single {
        ChatDao(
            get(),
            get(named(TABLE_CHATS)),
        )
    }
}