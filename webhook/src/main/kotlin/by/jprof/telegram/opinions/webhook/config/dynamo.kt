package by.jprof.telegram.opinions.webhook.config

import by.jprof.telegram.opinions.webhook.dao.KeyboardsDAO
import by.jprof.telegram.opinions.webhook.dao.KotlinMentionsDAO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dynamoModule = module {
    single {
        KotlinMentionsDAO(
                get(),
                get(named(TABLE_KOTLIN_MENTIONS))
        )
    }

    single {
        KeyboardsDAO(
                get(),
                get(named(TABLE_KEYBOARDS))
        )
    }
}
