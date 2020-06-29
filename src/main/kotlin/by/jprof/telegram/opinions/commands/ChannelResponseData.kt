package by.jprof.telegram.opinions.commands

import com.google.api.services.youtube.model.ChannelListResponse

internal data class ChannelResponseData(val resp: ChannelListResponse) {
    val total: Int by lazy {
        resp.pageInfo.totalResults
    }

    val channelId: String? by lazy {
        resp.items?.first()?.id
    }

    val channelTitle: String? by lazy {
        resp.items?.first()?.snippet?.title
    }
}
