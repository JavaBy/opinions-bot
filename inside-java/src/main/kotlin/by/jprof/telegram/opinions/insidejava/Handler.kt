package by.jprof.telegram.opinions.insidejava

import by.jprof.telegram.components.config.componentsDynamoModule
import by.jprof.telegram.components.config.componentsEnvModule
import by.jprof.telegram.components.config.componentsTelegramModule
import by.jprof.telegram.opinions.insidejava.config.dynamoModule
import by.jprof.telegram.opinions.insidejava.config.envModule
import by.jprof.telegram.opinions.insidejava.config.rssModule
import by.jprof.telegram.opinions.news.config.newsQueueDynamoModule
import by.jprof.telegram.opinions.news.config.newsQueueEnvModule
import by.jprof.telegram.opinions.news.produce.Producer
import by.jprof.telegram.opinions.publication.Publisher
import by.jprof.telegram.opinions.publication.config.publicationBeansModule
import by.jprof.telegram.opinions.publication.config.publicationDynamoModule
import by.jprof.telegram.opinions.publication.config.publicationEnvModule
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin

class Handler : RequestHandler<ScheduledEvent, Unit>, KoinComponent {
    init {
        startKoin {
            modules(
                componentsEnvModule,
                newsQueueEnvModule,
                publicationEnvModule,
                componentsTelegramModule,
                componentsDynamoModule,
                newsQueueDynamoModule,
                publicationDynamoModule,
                publicationBeansModule,
                envModule,
                dynamoModule,
                rssModule
            )
        }
    }

    private val producers: List<Producer> = getKoin().getAll()
    private val publishers: List<Publisher> = getKoin().getAll()

    override fun handleRequest(event: ScheduledEvent, context: Context?) =
        runBlocking {
            producers.forEach { it.produce() }
            publishers.forEach { it.publish() }
        }
}

fun main() {
    val handler = Handler()

    handler.handleRequest(ScheduledEvent(), null)
}
