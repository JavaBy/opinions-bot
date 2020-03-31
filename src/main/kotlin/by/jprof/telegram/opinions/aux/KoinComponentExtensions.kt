package by.jprof.telegram.opinions.aux

import org.koin.core.KoinComponent

inline fun <reified T : Any> KoinComponent.injectAll(): Lazy<List<T>> {
    return lazy(LazyThreadSafetyMode.NONE) {
        getKoin().getAll<T>()
    }
}
