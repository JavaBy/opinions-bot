package by.jprof.telegram.opinions.news.config

import by.jprof.telegram.opinions.news.queue.NewsQueue
import org.koin.core.qualifier.named
import org.koin.dsl.module

val newsQueueDynamoModule = module {
    single {
        NewsQueue(
            get(),
            get(named(TABLE_NEWS_QUEUE))
        )
    }
}
