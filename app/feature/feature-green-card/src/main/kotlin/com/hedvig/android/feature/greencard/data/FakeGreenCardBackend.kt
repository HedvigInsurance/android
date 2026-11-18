package com.hedvig.android.feature.greencard.data

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

// TODO: Delete once the green card schema is published on Octopus and the real use case implementations are enabled.
@SingleIn(AppScope::class)
@Inject
internal class FakeGreenCardBackend(
  private val clock: Clock,
) {
  private val today: LocalDate
    get() = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

  private val greenCards = mutableListOf(
    FakeGreenCard(
      id = "fake-green-card-1",
      registrationNumber = "ABC 123",
      validFrom = LocalDate(2025, 6, 20),
      validTo = LocalDate(2025, 7, 10),
    ),
  )

  suspend fun greenCards(): List<FakeGreenCard> {
    delay(FAKE_LATENCY)
    return greenCards.toList()
  }

  suspend fun specifications(): Pair<String?, List<GreenCardContractSpecification>> {
    delay(FAKE_LATENCY)
    val contracts = listOf("ABC 123", "XYZ 789").mapIndexed { index, registrationNumber ->
      GreenCardContractSpecification(
        contractId = "fake-car-contract-$index",
        registrationNumber = registrationNumber,
        minStartDate = today,
        maxEndDate = today.plus(DatePeriod(months = 3)),
      )
    }
    return "member@example.com" to contracts
  }

  suspend fun create(contractId: String, travelStartDate: LocalDate, travelEndDate: LocalDate): FakeGreenCard {
    delay(FAKE_LATENCY)
    val registrationNumber = specifications().second.first { it.contractId == contractId }.registrationNumber
    val greenCard = FakeGreenCard(
      id = "fake-green-card-${greenCards.size + 1}",
      registrationNumber = registrationNumber,
      validFrom = travelStartDate,
      validTo = travelEndDate,
    )
    greenCards.add(0, greenCard)
    return greenCard
  }

  data class FakeGreenCard(
    val id: String,
    val registrationNumber: String,
    val validFrom: LocalDate,
    val validTo: LocalDate,
    val signedUrl: String = FAKE_PDF_URL,
  )
}

private val FAKE_LATENCY = 600.milliseconds
private const val FAKE_PDF_URL = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
