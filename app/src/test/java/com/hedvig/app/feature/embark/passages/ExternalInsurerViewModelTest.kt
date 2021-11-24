package com.hedvig.app.feature.embark.passages

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.app.feature.embark.passages.externalinsurer.ExternalInsurerViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.GetInsuranceProvidersUseCase
import com.hedvig.app.feature.embark.passages.externalinsurer.InsuranceProvider
import com.hedvig.app.feature.embark.passages.externalinsurer.InsuranceProvidersResult
import com.hedvig.app.util.coroutines.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

class ExternalInsurerViewModelTest {

    private val insuranceProviders = listOf(
        InsuranceProvider("1", "Test1"),
        InsuranceProvider("2", "Test2"),
        InsuranceProvider("3", "Test3")
    )

    private var insuranceProvidersResult: InsuranceProvidersResult =
        InsuranceProvidersResult.Success(insuranceProviders)

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val getInsuranceProvidersUseCase = object : GetInsuranceProvidersUseCase {
        override suspend fun getInsuranceProviders(): InsuranceProvidersResult {
            delay(100)
            return insuranceProvidersResult
        }
    }

    @Test
    fun testLoadingInsurers() = mainCoroutineRule.dispatcher.runBlockingTest {
        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
    }

    @Test
    fun testShowInsuranceProviders() = mainCoroutineRule.dispatcher.runBlockingTest {
        insuranceProvidersResult = InsuranceProvidersResult.Success(insuranceProviders)
        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        viewModel.showInsuranceProviders()
        assertThat(viewModel.viewState.value.showInsuranceProviders).isEqualTo(listOf())
    }

    @Test
    fun testErrorState() = mainCoroutineRule.dispatcher.runBlockingTest {
        insuranceProvidersResult = InsuranceProvidersResult.Error.NetworkError
        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
        assertThat(viewModel.viewState.value.error).isEqualTo(InsuranceProvidersResult.Error.NetworkError)
    }

    @Test
    fun testSelectProvider() = mainCoroutineRule.dispatcher.runBlockingTest {
        insuranceProvidersResult = InsuranceProvidersResult.Success(insuranceProviders)
        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)
        viewModel.selectInsuranceProvider(InsuranceProvider("1", "Test1"))

        assertThat(viewModel.viewState.value.selectedProvider).isEqualTo(InsuranceProvider("1", "Test1"))
        assertThat(viewModel.viewState.value.showInsuranceProviders).isEqualTo(null)
    }

    @Test
    fun testContinueIfNoProviderSelected() = mainCoroutineRule.dispatcher.runBlockingTest {
        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)
        advanceUntilIdle()

        viewModel.onContinue()

        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
        assertThat(viewModel.viewState.value.continueEvent).isEqualTo(null)
    }

    @Test
    fun testContinueIfProviderSelected() = mainCoroutineRule.dispatcher.runBlockingTest {
        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)
        advanceUntilIdle()

        viewModel.selectInsuranceProvider(InsuranceProvider("1", "Test1"))
        viewModel.onContinue()

        assertThat(viewModel.viewState.value.continueEvent).isEqualTo(
            ExternalInsurerViewModel.ViewState.ContinueEvent(
                providerId = "1",
                providerName = "Test1"
            )
        )
    }
}
