package by.jprof.telegram.opinions.insidejava.podcast

import by.jprof.telegram.components.config.componentsDynamoModule
import by.jprof.telegram.components.config.componentsEnvModule
import by.jprof.telegram.components.config.componentsTelegramModule
import by.jprof.telegram.opinions.insidejava.config.dynamoModule
import by.jprof.telegram.opinions.insidejava.config.envModule
import by.jprof.telegram.opinions.insidejava.config.rssModule
import by.jprof.telegram.opinions.news.config.newsQueueDynamoModule
import by.jprof.telegram.opinions.news.config.newsQueueEnvModule
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject

class Handler : RequestHandler<ScheduledEvent, Unit>, KoinComponent {
    init {
        startKoin {
            modules(
                componentsEnvModule,
                componentsTelegramModule,
                componentsDynamoModule,
                newsQueueEnvModule,
                newsQueueDynamoModule,
                envModule,
                dynamoModule,
                rssModule
            )
        }
    }

    private val producer by inject<RssCrawler>()

    override fun handleRequest(event: ScheduledEvent, context: Context?) =
        runBlocking { producer.produce() }
}

fun main() {
    val handler = Handler()

    handler.handleRequest(ScheduledEvent(), null)
}
