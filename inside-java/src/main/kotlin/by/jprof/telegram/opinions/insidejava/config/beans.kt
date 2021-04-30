package by.jprof.telegram.opinions.insidejava.config

import by.jprof.telegram.opinions.insidejava.jeps.JepsCrawler
import by.jprof.telegram.opinions.insidejava.newscast.NewscastCrawler
import by.jprof.telegram.opinions.insidejava.podcast.PodcastCrawler
import by.jprof.telegram.opinions.news.produce.Producer
import by.jprof.telegram.opinions.rssparser.FeedParser
import by.jprof.telegram.opinions.rssparser.FeedType
import by.jprof.telegram.opinions.rssparser.FeedXmlPathConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.net.URL

val dynamoModule = module {
    single<Producer>(named("PodcastCrawler")) {
        PodcastCrawler(
            URL("http://insidejava.libsyn.com/rss"),
            get(),
            get(),
        )
    }

    single<Producer>(named("JepsCrawler")) {
        JepsCrawler(
            URL("https://inside.java/feed.xml"),
            FeedParser(FeedXmlPathConfig.atomDefault, FeedType.ATOM),
            get(),
        )
    }

    single<Producer>(named("NewscastCrawler")) {
        NewscastCrawler(
            get(),
            get(),
        )
    }
}
