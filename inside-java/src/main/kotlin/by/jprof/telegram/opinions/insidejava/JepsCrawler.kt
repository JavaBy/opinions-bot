package by.jprof.telegram.opinions.insidejava

import by.jprof.telegram.opinions.news.entity.JepAttrs
import by.jprof.telegram.opinions.news.produce.Producer
import by.jprof.telegram.opinions.news.queue.Event
import by.jprof.telegram.opinions.news.queue.NewsQueue
import by.jprof.telegram.opinions.news.queue.QueueItem
import by.jprof.telegram.opinions.rssparser.FeedParser
import java.net.URL

class JepsCrawler(
    private val rssLink: URL,
    private val parser: FeedParser,
    private val queue: NewsQueue
) : Producer {
    companion object {
        private val jepTitleRegex = ".*?New\\s+candidate\\s+JEP.?\\s+(?<jep>\\d+).*?".toRegex(RegexOption.IGNORE_CASE)
    }

    override suspend fun produce() {
        val jeps = queue.findAll<JepAttrs>(Event.JEP).map { it.payload.jep }
        parser.parse(rssLink.readText())
            .first().entries.mapNotNull { e ->
                e.title?.let {
                    jepTitleRegex.find(it)
                }?.let {
                    e to it.groups["jep"]?.value
                }
            }.filter { (_, jep) ->
                jep != null
            }.filterNot { (_, jep) ->
                jeps.contains(jep)
            }.forEach { (entry, jep) ->
                queue.push(
                    QueueItem(
                        event = Event.JEP,
                        payload = JepAttrs(
                            id = entry.id!!,
                            jep = jep!!
                        ),
                        entry.published?.toInstant()
                    )
                )
            }
    }
}