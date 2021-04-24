package by.jprof.telegram.opinions.webhook.config

import by.jprof.telegram.opinions.youtube.config.YOUTUBE_API_TOKEN
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import org.koin.core.qualifier.named
import org.koin.dsl.module


val youtubeModule = module {
    single {
        YouTube
            .Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                null
            )
            .setApplicationName("opinions-bot")
            .setYouTubeRequestInitializer(YouTubeRequestInitializer(get<String>(named(YOUTUBE_API_TOKEN))))
            .build()
    }
}
