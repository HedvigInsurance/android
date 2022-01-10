package com.hedvig.app.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import java.io.Closeable

private class Wrapper<T : Closeable>(val closeable: T) : RememberObserver {
    override fun onAbandoned() {
        closeable.close()
    }

    override fun onForgotten() {
        closeable.close()
    }

    override fun onRemembered() {}
}

@Composable
fun <T : Closeable> rememberCloseable(calculation: @DisallowComposableCalls () -> T): T {
    val wrapper = remember { Wrapper(calculation()) }
    return wrapper.closeable
}

@Composable
fun <T : Closeable> rememberCloseable(
    key1: Any?,
    calculation: @DisallowComposableCalls () -> T,
): T {
    val wrapper = remember(key1) { Wrapper(calculation()) }
    return wrapper.closeable
}

@Composable
fun <T : Closeable> rememberCloseable(
    key1: Any?,
    key2: Any?,
    calculation: @DisallowComposableCalls () -> T,
): T {
    val wrapper = remember(key1, key2) { Wrapper(calculation()) }
    return wrapper.closeable
}
