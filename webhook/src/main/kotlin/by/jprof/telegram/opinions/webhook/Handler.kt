package by.jprof.telegram.opinions.webhook

import by.jprof.telegram.components.config.componentsDynamoModule
import by.jprof.telegram.components.config.componentsEnvModule
import by.jprof.telegram.components.config.componentsTelegramModule
import by.jprof.telegram.opinions.voting.config.votingBeans
import by.jprof.telegram.opinions.voting.config.votingEnvModule
import by.jprof.telegram.opinions.webhook.config.dynamoModule
import by.jprof.telegram.opinions.webhook.config.envModule
import by.jprof.telegram.opinions.webhook.config.jsonModule
import by.jprof.telegram.opinions.webhook.config.pipelineModule
import by.jprof.telegram.opinions.webhook.processors.UpdateProcessingPipeline
import by.jprof.telegram.opinions.youtube.config.youtubeDynamoModule
import by.jprof.telegram.opinions.youtube.config.youtubeEnvModule
import by.jprof.telegram.opinions.youtube.config.youtubeModule
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.UpdateDeserializationStrategy
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject

val OK = APIGatewayV2ProxyResponseEvent().apply {
    statusCode = 200
    body = "{}"
}

class Handler : RequestHandler<APIGatewayV2ProxyRequestEvent, APIGatewayV2ProxyResponseEvent>, KoinComponent {
    companion object {
        val logger = LogManager.getLogger(Handler::class.java)!!
    }

    init {
        startKoin {
            modules(
                envModule,
                componentsEnvModule,
                youtubeEnvModule,
                votingEnvModule,
                componentsDynamoModule,
                componentsTelegramModule,
                votingBeans,
                dynamoModule,
                jsonModule,
                youtubeDynamoModule,
                youtubeModule,
                pipelineModule
            )
        }
    }

    private val json: Json by inject()
    private val pipeline: UpdateProcessingPipeline by inject()

    override fun handleRequest(input: APIGatewayV2ProxyRequestEvent, context: Context): APIGatewayV2ProxyResponseEvent {
        logger.debug("Incoming request: {}", input)

        val update = json.decodeFromString(UpdateDeserializationStrategy, input.body ?: return OK)

        logger.debug("Parsed update: {}", update)

        pipeline.process(update)

        return OK
    }
}
