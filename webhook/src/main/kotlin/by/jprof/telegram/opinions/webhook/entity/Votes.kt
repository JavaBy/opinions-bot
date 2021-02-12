package by.jprof.telegram.opinions.webhook.entity

import by.jprof.telegram.opinions.webhook.dao.toAttributeValue
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant

const val DOWN_VOTE = "\uD83D\uDC4E"
const val UP_VOTE = "\uD83D\uDC4D"

data class Votes(
        val id: String,
        val votes: Map<String, String> = emptyMap(),
        val lastRepostedAt: Instant? = null
)

fun Votes.toAttributeValues(): Map<String, AttributeValue> = mapOf(
        "id" to this.id.toAttributeValue(),
        "votes" to AttributeValue.builder().m(
                this
                        .votes
                        .mapValues { (_, value) -> value.toAttributeValue() }
        ).build()
)

fun Map<String, AttributeValue>.toVotes(): Votes = Votes(
        id = this["id"]?.s() ?: throw IllegalStateException("Missing id property"),
        votes = this["votes"]?.m()
                ?.mapValues { (_, value) -> value.s() ?: "" }
                ?: emptyMap()
)

val Votes.upVotes: Int
    get() = this.votes.count { (_, vote) -> vote == "+" }

val Votes.downVotes: Int
    get() = this.votes.count { (_, vote) -> vote == "-" }