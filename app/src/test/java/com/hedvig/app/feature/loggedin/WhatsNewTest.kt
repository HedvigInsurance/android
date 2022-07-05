package com.hedvig.app.feature.loggedin

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import assertk.assertThat
import assertk.assertions.isNotNull
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.mockserver.enqueue
import com.hedvig.android.owldroid.graphql.WhatsNewQuery
import com.hedvig.android.owldroid.graphql.type.Locale
import com.hedvig.app.apollo.runApolloTest
import com.hedvig.app.apollo.toJsonStringWithData
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewViewModelImpl
import com.hedvig.app.testdata.feature.loggedin.WHATS_NEW
import com.hedvig.app.util.coroutines.MainCoroutineRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runCurrent
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class WhatsNewTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `fetching what's new with present news shows them`() = runApolloTest { mockServer, apolloClient ->
        mockServer.enqueue(WHATS_NEW.toJsonStringWithData())
        val viewModel: WhatsNewViewModel = WhatsNewViewModelImpl(
            WhatsNewRepository(
                apolloClient = apolloClient,
                context = mockk(),
                localeManager = mockk {
                    every { this@mockk.defaultLocale() } returns Locale.sv_SE
                },
            ),
        )

        var eventData: WhatsNewQuery.Data? = null
        val observer: (t: WhatsNewQuery.Data) -> Unit = {
            eventData = it
        }
        viewModel.news.observeForever(observer)

        viewModel.fetchNews("")
        runCurrent()

        assertThat(eventData).isNotNull()
        viewModel.news.removeObserver(observer)
    }
}
