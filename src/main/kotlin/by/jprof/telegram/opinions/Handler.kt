package by.jprof.telegram.opinions

import by.dev.madhead.telek.model.Update as OldUpdate
import by.jprof.telegram.opinions.config.*
import by.jprof.telegram.opinions.model.Update
import by.jprof.telegram.opinions.processors.UpdateProcessingPipeline
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent
import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.UpdateDeserializationStrategy
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject

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
            modules(envModule, jsonModule, dynamoModule, pipelineModule, youtubeModule, telegramModule)
        }
    }

    private val json: Json by inject()
    private val pipeline: UpdateProcessingPipeline by inject()

    override fun handleRequest(input: APIGatewayV2ProxyRequestEvent, context: Context): APIGatewayV2ProxyResponseEvent {
        logger.debug("Incoming request: {}", input)

        val oldUpdate = json.parse(OldUpdate.serializer(), input.body ?: return OK)
        val newUpdate = json.parse(UpdateDeserializationStrategy, input.body ?: return OK)
        val update  = Update(oldUpdate, newUpdate) //TODO: move jep processor to new updates after youtube part
        logger.debug("Parsed update: {}", oldUpdate)

        pipeline.process(update)

        return OK
    }
}
