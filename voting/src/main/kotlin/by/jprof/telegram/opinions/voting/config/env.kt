package by.jprof.telegram.opinions.voting.config

import org.koin.core.qualifier.named
import org.koin.dsl.module

const val TABLE_VOTES = "TABLE_VOTES"

val votingEnvModule = module {
    listOf(
        TABLE_VOTES
    ).forEach { variable ->
        single(named(variable)) {
            System.getenv(variable)!!
        }
    }
}
