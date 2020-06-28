package by.jprof.telegram.opinions

import by.jprof.telegram.opinions.config.*
import by.jprof.telegram.opinions.processors.UpdateProcessingPipeline
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.UpdateDeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.apache.logging.log4j.LogManager

val OK = APIGatewayV2ProxyResponseEvent().apply {
    statusCode = 200
    body = "{}"
}

@ImplicitReflectionSerializer
class Handler : RequestHandler<APIGatewayV2ProxyRequestEvent, APIGatewayV2ProxyResponseEvent>, KoinComponent {
    companion object {
        val logger = LogManager.getLogger(Handler::class.java)!!
    }

    init {
        startKoin {
            modules(envModule, jsonModule, dynamoModule, youtubeModule, telegramModule, pipelineModule)
        }
    }

    private val json: Json by inject()
    private val pipeline: UpdateProcessingPipeline by inject()

    override fun handleRequest(input: APIGatewayV2ProxyRequestEvent, context: Context): APIGatewayV2ProxyResponseEvent {
        logger.debug("Incoming request: {}", input)

        val update = json.parse(UpdateDeserializationStrategy, input.body ?: return OK)
        logger.debug("Parsed update: {}", update)

        pipeline.process(update)

        return OK
    }
}
