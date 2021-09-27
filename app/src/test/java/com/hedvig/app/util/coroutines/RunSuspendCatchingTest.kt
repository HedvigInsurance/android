package com.hedvig.app.util.coroutines

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isSuccess
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class RunSuspendCatchingTest {
    @Test
    fun `when CancellationException is thrown, should rethrow`() {
        val ex = CancellationException()
        assertThat {
            runSuspendCatching {
                throw ex
            }
        }.isFailure().isEqualTo(ex)
    }

    @Test
    fun `when non-CancellationException is thrown, should return failure`() {
        val error = Error()
        assertThat {
            runSuspendCatching {
                throw error
            }
        }.isSuccess().isEqualTo(Result.failure(error))
    }

    @Test
    fun `when no exception is thrown, should return success`() {
        assertThat {
            runSuspendCatching {}
        }.isSuccess().isEqualTo(Result.success(Unit))
    }
}
