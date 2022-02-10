package com.hedvig.app.feature.addressautocompletion.data

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetFinalDanishAddressSelectionUseCaseTest {

    private fun getTestUseCase(
        fetchNewResultsReturnValue: List<DanishAddress>,
    ) = GetFinalDanishAddressSelectionUseCase(
        mockk<GetDanishAddressAutoCompletionUseCase>().apply {
            coEvery { this@apply.invoke(any(), any()) }.coAnswers {
                AddressAutoCompleteResults(fetchNewResultsReturnValue).right()
            }
        }
    )

    @Test
    fun `return null when only providing an address`() = runBlockingTest {
        val selectedAddress = asavej
        val fetchResult = asavej_results

        val result = getTestUseCase(fetchResult).invoke(
            selectedAddress = selectedAddress,
            lastSelection = null,
        )

        assertThat(result).isNull()
    }

    @Test
    fun `return null when after querying again it turns out to not be the only possible result`() = runBlockingTest {
        val selectedAddress = asavej_1_9330_dronninglund
        val fetchResult = asavej_1_9330_dronninglund_results // Contains 2 results

        val result = getTestUseCase(fetchResult).invoke(
            selectedAddress = selectedAddress,
            lastSelection = null,
        )

        assertThat(result).isNull()
    }

    @Test
    fun `return the selected address when it is not the only result but it was selected again in the previous query`() =
        runBlockingTest {
            val selectedAddress = asavej_1_9330_dronninglund
            val fetchResult = asavej_1_9330_dronninglund_results

            val result = getTestUseCase(fetchResult).invoke(
                selectedAddress = selectedAddress,
                lastSelection = asavej_1_9330_dronninglund,
            )

            assertThat(result).isNotNull()
            assertThat(result).isEqualTo(selectedAddress)
        }

    @Test
    fun `return the selected address when after querying again it turns out to be the only possible result`() =
        runBlockingTest {
            val selectedAddress = asavej_1_9330_dronninglund
            val fetchResult = asavej_1_9330_dronninglund_results.take(1)

            val result = getTestUseCase(fetchResult).invoke(
                selectedAddress = selectedAddress,
                lastSelection = null,
            )

            assertThat(result).isNotNull()
            assertThat(result).isEqualTo(selectedAddress)
        }

    @Test
    fun `return null when after querying again and despite being a single result, it's not the same item`() =
        runBlockingTest {
            val selectedAddress = asavej_1_9330_dronninglund
            val fetchResult = asavej_1_9330_dronninglund_results.takeLast(1)

            val result = getTestUseCase(fetchResult).invoke(
                selectedAddress = selectedAddress,
                lastSelection = null,
            )

            assertThat(result).isNull()
        }

    companion object {
        private val asavej = DanishAddress("asavej")
        private val asavej_1_9330_dronninglund = DanishAddress(
            id = "0a3f50c7-03e1-32b8-e044-0003ba298018",
            address = "Asåvej 1, 9330 Dronninglund",
            streetName = "Asåvej",
            streetNumber = "1",
            floor = null,
            apartment = null,
            postalCode = "9330",
            city = "Dronninglund",
        )

        private val asavej_results: List<DanishAddress> = listOf(
            DanishAddress(
                id = "0a3f5099-6015-32b8-e044-0003ba298018",
                address = "Asåvej 1, 9330 Dronninglund",
                streetName = "Asåvej",
                streetNumber = "1",
                floor = null,
                apartment = null,
                postalCode = "9330",
                city = "Dronninglund",
            ),
            DanishAddress(
                id = "0a3f5099-6016-32b8-e044-0003ba298018",
                address = "Asåvej 1A, 9330 Dronninglund",
                streetName = "Asåvej",
                streetNumber = "1A",
                floor = null,
                apartment = null,
                postalCode = "9330",
                city = "Dronninglund",
            ),
            DanishAddress(
                id = "d63c47da-35ab-4c29-af9f-547bdea90c95",
                address = "Asåvej 1B, 9330 Dronninglund",
                streetName = "Asåvej",
                streetNumber = "1B",
                floor = null,
                apartment = null,
                postalCode = "9330",
                city = "Dronninglund",
            ),
        )

        // Tests rely on this being of size 2, do not change.
        private val asavej_1_9330_dronninglund_results: List<DanishAddress> = listOf(
            DanishAddress(
                id = "0a3f50c7-03e1-32b8-e044-0003ba298018",
                address = "Asåvej 1, 9330 Dronninglund",
                streetName = "Asåvej",
                streetNumber = "1",
                floor = null,
                apartment = null,
                postalCode = "9330",
                city = "Dronninglund",
            ),
            DanishAddress(
                id = "e24567bc-412b-438a-864b-555e2d20686a",
                address = "Asåvej 2, 1., 9330 Dronninglund",
                streetName = "Asåvej",
                streetNumber = "2",
                floor = "1",
                apartment = null,
                postalCode = "9330",
                city = "Dronninglund",
            )
        )
    }
}
