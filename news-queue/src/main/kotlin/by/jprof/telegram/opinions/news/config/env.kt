package by.jprof.telegram.opinions.news.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TABLE_NEWS_QUEUE = "TABLE_NEWS_QUEUE"

val newsQueueEnvModule = module {
    listOf(
        TABLE_NEWS_QUEUE
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}