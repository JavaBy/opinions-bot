package by.jprof.telegram.opinions.insidejava.config

import by.jprof.telegram.opinions.insidejava.podcast.PodcastCrawler
import by.jprof.telegram.opinions.news.produce.Producer
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dynamoModule = module {
    single<Producer> {
        PodcastCrawler(
            get(named(RSS_URL)),
            get(),
            get(),
        )
    }
}
