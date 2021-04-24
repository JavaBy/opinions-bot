package by.jprof.telegram.opinions.youtube.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TABLE_VOTES = "TABLE_VOTES"
const val YOUTUBE_API_TOKEN = "YOUTUBE_API_TOKEN"

val youtubeEnvModule = module {
    listOf(
        TABLE_VOTES,
        YOUTUBE_API_TOKEN
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
