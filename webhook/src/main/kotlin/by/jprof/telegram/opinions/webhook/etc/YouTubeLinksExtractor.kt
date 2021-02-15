package by.jprof.telegram.opinions.webhook.etc

import by.jprof.telegram.opinions.webhook.processors.YoutubeLinksProcessor.Companion.youTubeVideoId
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import java.io.File

fun main(args: Array<String>) {
    val youTube = YouTube
            .Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null
            )
            .setApplicationName("opinions-bot")
            .setYouTubeRequestInitializer(YouTubeRequestInitializer(args[0]))
            .build()!!

    File(args[1])
            .readLines()
            .mapNotNull { it.youTubeVideoId }
            .map { it.replace("\"", "") }
            .distinct()
            .mapNotNull {
                println("Fetching channel for $it…")

                try {
                    val response = youTube
                            .videos()!!
                            .list("snippet,statistics")!!
                            .setId(it)!!
                            .execute()!!

                    println(response)

                    val items = response.items

                    println(items)

                    val video = items.first()

                    println(video)

                    val snippet = video.snippet

                    println(snippet)

                    val channel = snippet.channelId

                    println(channel)

                    channel
                } catch (e: Exception) {
                    System.err.println("Failed to fetch channel for $it: $e")

                    null
                }
            }
            .groupBy { it }
            .entries
            .sortedByDescending { (_, list) -> list.size }
            .forEach { (channelId, list) ->
                try {
                    val response = youTube
                            .channels()
                            .list("snippet")!!
                            .setId(channelId)!!
                            .execute()!!
                    val items = response.items
                    val channel = items.first()
                    val snippet = channel.snippet

                    println("| $channelId | ${snippet.title} | https://www.youtube.com/channel/$channelId | ${list.size} |")
                } catch (_: Exception) {
                }
            }
}
