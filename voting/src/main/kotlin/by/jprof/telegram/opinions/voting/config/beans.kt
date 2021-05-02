package by.jprof.telegram.opinions.voting.config

import by.jprof.telegram.opinions.voting.JEPLinksVoting
import by.jprof.telegram.opinions.voting.VotesDAO
import by.jprof.telegram.opinions.voting.YoutubeVoting
import org.koin.core.qualifier.named
import org.koin.dsl.module

val votingBeans = module {
    single {
        YoutubeVoting(get(), get(), get(), get())
    }
    single {
        JEPLinksVoting(get(), get())
    }
    single {
        VotesDAO(
            get(),
            get(named(TABLE_VOTES))
        )
    }
}