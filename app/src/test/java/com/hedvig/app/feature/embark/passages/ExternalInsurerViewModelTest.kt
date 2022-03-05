package com.hedvig.app.feature.embark.passages

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.app.feature.embark.passages.externalinsurer.ExternalInsurerViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.GetInsuranceProvidersUseCase
import com.hedvig.app.feature.embark.passages.externalinsurer.InsuranceProvider
import com.hedvig.app.feature.embark.passages.externalinsurer.InsuranceProvidersResult
import com.hedvig.app.util.coroutines.StandardTestDispatcherAsMainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ExternalInsurerViewModelTest {

    private val insuranceProviders = listOf(
        InsuranceProvider("1", "Test1"),
        InsuranceProvider("2", "Test2"),
        InsuranceProvider("3", "Test3")
    )

    @get:Rule
    val standardTestDispatcherAsMainDispatcherRule = StandardTestDispatcherAsMainDispatcherRule()

    private val getInsuranceProvidersUseCase = mockk<GetInsuranceProvidersUseCase>()

    @Test
    fun testLoadingInsurers() = runTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } coAnswers {
            delay(100)
            InsuranceProvidersResult.Success(insuranceProviders)
        }

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
    }

    @Test
    fun testErrorState() = runTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } coAnswers {
            delay(100)
            InsuranceProvidersResult.Error.NetworkError
        }

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        val events = mutableListOf<ExternalInsurerViewModel.Event>()
        val eventCollectingJob = launch {
            viewModel.events.collect { events.add(it) }
        }

        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
        assertThat(events.size).isEqualTo(1)
        assertThat(events.first())
            .isEqualTo(ExternalInsurerViewModel.Event.Error(InsuranceProvidersResult.Error.NetworkError))
        eventCollectingJob.cancel()
    }

    @Test
    fun testNoErrorState() = runTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } coAnswers {
            delay(100)
            InsuranceProvidersResult.Success(insuranceProviders)
        }

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        val events = mutableListOf<ExternalInsurerViewModel.Event>()
        val eventCollectingJob = launch {
            viewModel.events.collect { events.add(it) }
        }

        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
        assertThat(events.isEmpty()).isEqualTo(true)
        eventCollectingJob.cancel()
    }

    @Test
    fun testSelectProvider() = runTest {
        coEvery {
            getInsuranceProvidersUseCase.getInsuranceProviders()
        } returns InsuranceProvidersResult.Success(insuranceProviders)
        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        viewModel.selectInsuranceProvider(InsuranceProvider("1", "Test1"))

        assertThat(viewModel.viewState.value.selectedProvider).isEqualTo(InsuranceProvider("1", "Test1"))
    }

    @Test
    fun testCantContinueIfNoProviderSelected() = runTest {
        coEvery {
            getInsuranceProvidersUseCase.getInsuranceProviders()
        } returns InsuranceProvidersResult.Success(insuranceProviders)

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
        assertThat(viewModel.viewState.value.canContinue()).isEqualTo(false)
    }

    @Test
    fun testCanContinueIfProviderSelected() = runTest {
        coEvery {
            getInsuranceProvidersUseCase.getInsuranceProviders()
        } returns InsuranceProvidersResult.Success(insuranceProviders)

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)
        advanceUntilIdle()

        viewModel.selectInsuranceProvider(InsuranceProvider("1", "Test1"))
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
        assertThat(viewModel.viewState.value.canContinue()).isEqualTo(true)
    }
}
