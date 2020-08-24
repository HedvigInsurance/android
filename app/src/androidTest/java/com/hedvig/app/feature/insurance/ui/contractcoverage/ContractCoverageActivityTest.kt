package com.hedvig.app.feature.insurance.ui.contractcoverage

import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.test.espresso.ApolloIdlingResource
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.inject

@RunWith(AndroidJUnit4::class)
class ContractCoverageActivityTest : KoinComponent {

    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = ActivityTestRule(ContractCoverageActivity::class.java)

    private val idlingResource = ApolloIdlingResource.create("ApolloIdlingResource", apolloClientWrapper.apolloClient)

    @Before
    fun setup() {
        IdlingRegistry
            .getInstance()
            .register(idlingResource)
    }

    @Test
    fun showsSpinnerWhileLoading() {
        onScreen<ContractCoverageScreen> {
            spinner {
                isVisible()
            }
            scrollView {
                isGone()
            }
        }
    }

    @Test
    fun showsPerilsWhenLoadingIsComplete() {
        onScreen<ContractCoverageScreen> {

        }
    }

    class ContractCoverageScreen : Screen<ContractCoverageScreen>() {
        val spinner = KView { withId(R.id.loadingSpinner) }
        val scrollView = KView { withId(R.id.scrollView) }
    }
}
