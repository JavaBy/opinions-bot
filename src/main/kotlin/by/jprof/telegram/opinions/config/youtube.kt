package by.jprof.telegram.opinions.config

import by.jprof.telegram.opinions.dao.YoutubeDAO
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import org.koin.core.qualifier.named
import org.koin.dsl.module


val youtubeModule = module {

    single {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val theKey = get<String>(named(YOUTUBE_API_TOKEN))
        val initializer = YouTubeRequestInitializer(theKey)
        YouTube.Builder(httpTransport, jsonFactory, null)
                .setYouTubeRequestInitializer(initializer)
                .build()
    }

    single {
        YoutubeDAO(get(), get(named(YOUTUBE_CHANNELS_WHITELIST_TABLE)))
    }


}
