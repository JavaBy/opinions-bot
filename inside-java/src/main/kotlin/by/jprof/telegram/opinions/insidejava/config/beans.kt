package by.jprof.telegram.opinions.insidejava.config

import by.jprof.telegram.opinions.insidejava.newscast.NewscastCrawler
import by.jprof.telegram.opinions.insidejava.podcast.PodcastCrawler
import by.jprof.telegram.opinions.news.produce.Producer
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val dynamoModule = module {
    single<Producer>(named("PodcastCrawler")) {
        PodcastCrawler(
            get(named(RSS_URL)),
            get(),
            get(),
        )
    } bind Producer::class

    single<Producer>(named("NewscastCrawler")) {
        NewscastCrawler(
            get(),
            get(),
        )
    } bind Producer::class
}
