package by.jprof.telegram.opinions.insidejava.config

import by.jprof.telegram.opinions.insidejava.RssCrawler
import by.jprof.telegram.opinions.insidejava.dao.InsideJavaDAO
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dynamoModule = module {
    single {
        RssCrawler(
            get(named(RSS_URL)),
            get(),
            get(),
            get()
        )
    }

    single {
        InsideJavaDAO(
            get(),
            get(named(TABLE_INSIDE_JAVA_PODCAST))
        )
    }
}
