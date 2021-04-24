package by.jprof.telegram.opinions.voting.config

import by.jprof.telegram.opinions.voting.VotesDAO
import by.jprof.telegram.opinions.voting.YoutubeVoting
import org.koin.core.qualifier.named
import org.koin.dsl.module

val votingBeans = module {
    single {
        VotesDAO(
            get(),
            get(named(TABLE_VOTES))
        )
    }

    single {
        YoutubeVoting(get(), get(), get(), get())
    }
}