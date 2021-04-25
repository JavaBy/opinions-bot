package by.jprof.telegram.opinions.youtube.config

import by.jprof.telegram.opinions.youtube.YoutubeVoting
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import org.koin.core.qualifier.named
import org.koin.dsl.module

val youtubeBeansModule = module {
    single {
        YoutubeVoting(get(), get(), get(), get())
    }

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