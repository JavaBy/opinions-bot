package by.jprof.telegram.opinions.insidejava.config

import by.jprof.telegram.opinions.insidejava.podcast.PodcastCrawler
import by.jprof.telegram.opinions.insidejava.dao.InsideJavaDAO
import by.jprof.telegram.opinions.news.produce.Producer
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val dynamoModule = module {
    single<Producer> {
        PodcastCrawler(
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
