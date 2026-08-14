package com.beader.core.common.di

import javax.inject.Qualifier

/**
 * Hilt qualifier for injecting a specific [kotlinx.coroutines.CoroutineDispatcher].
 * Never inject `Dispatchers.IO` directly — always go through this so tests
 * can substitute a [kotlinx.coroutines.test.TestDispatcher].
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val beaderDispatcher: BeaderDispatchers)

enum class BeaderDispatchers {
    Default,
    Io,
    Main,
}
