package by.vsdev.posterminal.demo.domain.util

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Injectable coroutine dispatchers. Production binds these to [kotlinx.coroutines.Dispatchers];
 * tests swap in a single test dispatcher so `runTest` stays deterministic.
 */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
