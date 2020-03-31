package by.jprof.telegram.opinions

import by.dev.madhead.telek.model.Update
import by.jprof.telegram.opinions.config.engineModule
import by.jprof.telegram.opinions.config.envModule
import by.jprof.telegram.opinions.config.jsonModule
import by.jprof.telegram.opinions.config.telegramModule
import by.jprof.telegram.opinions.processors.UpdateProcessingEngine
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent
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
        val logger = LogManager.getLogger(Handler::class.java)
    }

    init {
        startKoin {
            modules(envModule, jsonModule, engineModule, telegramModule)
        }
    }

    private val json: Json by inject()
    private val engine: UpdateProcessingEngine by inject()

    override fun handleRequest(input: APIGatewayV2ProxyRequestEvent, context: Context): APIGatewayV2ProxyResponseEvent {
        logger.debug("Incoming request: {}", input)

        val update = json.parse(Update.serializer(), input.body ?: return OK)

        logger.debug("Parsed update: {}", update)

        engine.process(update)

        return OK
    }
}
