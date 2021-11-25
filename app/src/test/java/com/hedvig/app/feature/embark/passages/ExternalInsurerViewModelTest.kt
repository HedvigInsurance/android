package com.hedvig.app.feature.embark.passages

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.app.feature.embark.passages.externalinsurer.ExternalInsurerViewModel
import com.hedvig.app.feature.embark.passages.externalinsurer.GetInsuranceProvidersUseCase
import com.hedvig.app.feature.embark.passages.externalinsurer.InsuranceProvider
import com.hedvig.app.feature.embark.passages.externalinsurer.InsuranceProvidersResult
import com.hedvig.app.util.coroutines.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test

class ExternalInsurerViewModelTest {

    private val insuranceProviders = listOf(
        InsuranceProvider("1", "Test1"),
        InsuranceProvider("2", "Test2"),
        InsuranceProvider("3", "Test3")
    )

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val getInsuranceProvidersUseCase = mockk<GetInsuranceProvidersUseCase>()

    @Test
    fun testLoadingInsurers() = mainCoroutineRule.dispatcher.runBlockingTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } coAnswers {
            delay(100)
            InsuranceProvidersResult.Success(insuranceProviders)
        }

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
    }

    @Test
    fun testLoadInsuranceProviders() = mainCoroutineRule.dispatcher.runBlockingTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } returns
            InsuranceProvidersResult.Success(insuranceProviders)

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        assertThat(viewModel.viewState.value.insuranceProviders).isEqualTo(insuranceProviders)
    }

    @Test
    fun testErrorState() = mainCoroutineRule.dispatcher.runBlockingTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } coAnswers {
            delay(100)
            InsuranceProvidersResult.Error.NetworkError
        }

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        val events = mutableListOf<ExternalInsurerViewModel.Event>()
        val job = launch {
            viewModel.events.toList(events)
        }

        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
        assertThat(events.first())
            .isEqualTo(ExternalInsurerViewModel.Event.Error(InsuranceProvidersResult.Error.NetworkError))

        job.cancel()
    }

    @Test
    fun testNoErrorState() = mainCoroutineRule.dispatcher.runBlockingTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } coAnswers {
            delay(100)
            InsuranceProvidersResult.Success(insuranceProviders)
        }

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)

        val events = mutableListOf<ExternalInsurerViewModel.Event>()
        val job = launch {
            viewModel.events.toList(events)
        }

        advanceTimeBy(1)
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(true)

        advanceUntilIdle()
        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
        assertThat(events.isEmpty()).isEqualTo(true)

        job.cancel()
    }

    @Test
    fun testSelectProvider() = mainCoroutineRule.dispatcher.runBlockingTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } returns
            InsuranceProvidersResult.Success(insuranceProviders)

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)
        viewModel.selectInsuranceProvider(InsuranceProvider("1", "Test1"))

        assertThat(viewModel.viewState.value.selectedProvider).isEqualTo(InsuranceProvider("1", "Test1"))
    }

    @Test
    fun testContinueIfNoProviderSelected() = mainCoroutineRule.dispatcher.runBlockingTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } returns
            InsuranceProvidersResult.Success(insuranceProviders)

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.isLoading).isEqualTo(false)
        assertThat(viewModel.viewState.value.canContinue()).isEqualTo(false)
    }

    @Test
    fun testContinueIfProviderSelected() = mainCoroutineRule.dispatcher.runBlockingTest {
        coEvery { getInsuranceProvidersUseCase.getInsuranceProviders() } returns
            InsuranceProvidersResult.Success(insuranceProviders)

        val viewModel = ExternalInsurerViewModel(getInsuranceProvidersUseCase)
        advanceUntilIdle()

        viewModel.selectInsuranceProvider(InsuranceProvider("1", "Test1"))
        assertThat(viewModel.viewState.value.canContinue()).isEqualTo(true)
    }
}
