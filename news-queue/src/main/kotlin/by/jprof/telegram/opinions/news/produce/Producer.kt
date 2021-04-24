package by.jprof.telegram.opinions.news.produce

interface Producer {
    suspend fun produce()
}