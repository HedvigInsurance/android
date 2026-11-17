package com.hedvig.android.feature.payments.ios

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentHistoryItem
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryUiState
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class PaymentHistoryScreenStateTest {
  @Test
  fun `Loading maps to Loading`() {
    assertThat(PaymentHistoryUiState.Loading.toScreenState()).isEqualTo(PaymentHistoryScreenState.Loading)
  }

  @Test
  fun `Failure maps to Failed`() {
    assertThat(PaymentHistoryUiState.Failure.toScreenState()).isEqualTo(PaymentHistoryScreenState.Failed)
  }

  @Test
  fun `a success without charges maps to Empty`() {
    assertThat(PaymentHistoryUiState.Success(emptyList()).toScreenState()).isEqualTo(PaymentHistoryScreenState.Empty)
  }

  @Test
  fun `a success with charges maps each charge in order`() {
    val state = PaymentHistoryUiState.Success(
      listOf(
        historyItem("charge-2", LocalDate(2026, 2, 27), 449.5, MemberCharge.MemberChargeStatus.FAILED),
        historyItem("charge-1", LocalDate(2025, 12, 1), 0.0, MemberCharge.MemberChargeStatus.SUCCESS),
      ),
    )

    assertThat(state.toScreenState()).isEqualTo(
      PaymentHistoryScreenState.Content(
        charges = listOf(
          PaymentHistoryCharge("charge-2", "2026-02-27", PaymentMoney(449.5, "SEK"), PaymentChargeStatus.FAILED),
          PaymentHistoryCharge("charge-1", "2025-12-01", PaymentMoney(0.0, "SEK"), PaymentChargeStatus.SUCCESS),
        ),
        showsOlderChargesNotice = false,
      ),
    )
  }

  @Test
  fun `a charge the data layer stored with an empty id has a null id`() {
    val state = PaymentHistoryUiState.Success(listOf(historyItem(id = "")))

    val content = state.toScreenState() as PaymentHistoryScreenState.Content

    assertThat(content.charges.map { it.id }).containsExactly(null)
  }

  @Test
  fun `the older charges notice shows from twelve charges on`() {
    val eleven = PaymentHistoryUiState.Success(List(11) { historyItem(id = "charge-$it") }).toScreenState()
    val twelve = PaymentHistoryUiState.Success(List(12) { historyItem(id = "charge-$it") }).toScreenState()

    assertThat((eleven as PaymentHistoryScreenState.Content).showsOlderChargesNotice).isFalse()
    assertThat((twelve as PaymentHistoryScreenState.Content).showsOlderChargesNotice).isTrue()
  }

  @Test
  fun `handle calls the closure of each state`() {
    val states = listOf(
      PaymentHistoryScreenState.Loading,
      PaymentHistoryScreenState.Empty,
      PaymentHistoryScreenState.Failed,
      PaymentHistoryScreenState.Content(emptyList(), showsOlderChargesNotice = false),
    )

    val handled = states.map { state ->
      val calls = mutableListOf<String>()
      state.handle(
        onLoading = { calls += "loading" },
        onEmpty = { calls += "empty" },
        onFailed = { calls += "failed" },
        onContent = { calls += "content:${it.charges.size}" },
      )
      calls
    }

    assertThat(handled).containsExactly(listOf("loading"), listOf("empty"), listOf("failed"), listOf("content:0"))
  }
}

private fun historyItem(
  id: String,
  dueDate: LocalDate = LocalDate(2026, 1, 27),
  amount: Double = 100.0,
  status: MemberCharge.MemberChargeStatus = MemberCharge.MemberChargeStatus.SUCCESS,
) = PaymentHistoryItem(
  netAmount = UiMoney(amount, UiCurrencyCode.SEK),
  id = id,
  status = status,
  dueDate = dueDate,
)
