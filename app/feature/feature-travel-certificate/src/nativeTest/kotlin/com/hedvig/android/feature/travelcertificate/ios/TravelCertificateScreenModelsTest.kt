package com.hedvig.android.feature.travelcertificate.ios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.ui.choose.ChooseContractForCertificateViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewhen.TravelCertificateDateInputViewModelFactory
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputViewModel
import com.hedvig.android.feature.travelcertificate.ui.generatewho.TravelCertificateTravellersInputViewModelFactory
import com.hedvig.android.feature.travelcertificate.ui.history.CertificateHistoryViewModel
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.molecule.public.ScreenModel
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

/**
 * Runs on the test binary's main thread, which drains `Dispatchers.Main` only while a test pumps the main run loop.
 * The display link produces no frame in the test process, so a presenter composes once: it starts its loads and its
 * event collection and delivers only Loading. The mapping of every other state is covered by the screen state tests
 * and the presenter tests in this source set.
 *
 * An event is shown to reach its ViewModel as the right presenter event by filling the ViewModel's buffer of 20 with
 * it while nothing collects: the 21st is dropped, logged by name, and never consumed.
 */
class TravelCertificateScreenModelsTest {
  private val logs = CapturingLogger()
  private val collections = MainScope()
  private val history = FakeHistoryUseCase()
  private val availability = FakeAvailabilityUseCase()
  private val eligibleContracts = FakeEligibleContractsUseCase()
  private val addonBanner = FakeAddonBannerUseCase()
  private val downloadPdf = FakeDownloadPdfUseCase()
  private val specifications = FakeSpecificationsUseCase()
  private val coInsured = FakeCoInsuredUseCase()
  private val createTransport = FakeCreateTransport()
  private val step = TravelCertificateTravellersStep("member@hedvig.com", LocalDate(2026, 12, 1), "contract-1")

  @BeforeTest
  fun installLogger() {
    LogcatLogger.install(logs)
  }

  @AfterTest
  fun uninstallLogger() {
    LogcatLogger.uninstall()
    collections.cancel()
  }

  @Test
  fun `the four models start as Loading without loading`() {
    val models = listOf(historyModel(), chooseModel(), dateModel("contract-1"), travellersModel())

    val seeded = models.map { it.currentState }
    pumpMainLoop(1.seconds)
    models.forEach { it.close() }

    assertThat(seeded).containsExactly(
      TravelCertificateHistoryScreenState.Loading,
      TravelCertificateChooseContractScreenState.Loading,
      TravelCertificateDateInputScreenState.Loading,
      TravelCertificateTravellersScreenState.Loading,
    )
    assertNothingLoaded()
  }

  @Test
  fun `collecting the history model loads each part once and delivers Loading`() {
    val model = historyModel()
    val states = mutableListOf<TravelCertificateHistoryScreenState>()

    collections.launch { model.stateUpdates.collect { states += it } }
    assertThat(
      pumpMainLoopUntil {
        history.calls.value == 1 && availability.calls.value == 1 && eligibleContracts.calls.value == 1
      },
    ).isTrue()
    pumpMainLoop(1.seconds)
    model.close()

    assertThat(states).containsExactly(TravelCertificateHistoryScreenState.Loading)
    assertThat(addonBanner.calls.value).isEqualTo(1)
    assertThat(downloadPdf.calls.value).isEqualTo(0)
  }

  @Test
  fun `the date model passes its contract id and null for null and empty`() {
    assertThat(contractIdReachingSpecifications("contract-1")).isEqualTo("contract-1")
    assertThat(contractIdReachingSpecifications(null)).isEqualTo(null)
    assertThat(contractIdReachingSpecifications("")).isEqualTo(null)
  }

  @Test
  fun `the travellers model loads the co-insured of the step's contract`() {
    val model = travellersModel()
    val states = mutableListOf<TravelCertificateTravellersScreenState>()

    collections.launch { model.stateUpdates.collect { states += it } }
    assertThat(pumpMainLoopUntil { coInsured.contractIds().size == 1 }).isTrue()
    model.close()

    assertThat(coInsured.contractIds()).containsExactly("contract-1")
    assertThat(states).containsExactly(TravelCertificateTravellersScreenState.Loading)
    assertThat(createTransport.inputs()).isEmpty()
  }

  @Test
  fun `history and choose reloads reach their ViewModels as RetryLoadData`() {
    assertThat(droppedEvent(historyModel(), "CertificateHistoryViewModel") { it.reload() }).isEqualTo("RetryLoadData")
    assertThat(droppedEvent(chooseModel(), "ChooseContractForCertificateViewModel") { it.reload() })
      .isEqualTo("RetryLoadData")
    assertNothingLoaded()
  }

  @Test
  fun `each date model function reaches the ViewModel as its event`() {
    val name = "TravelCertificateDateInputViewModel"

    assertThat(droppedEvent(dateModel(null), name) { it.reload() }).isEqualTo("RetryLoadData")
    assertThat(droppedEvent(dateModel(null), name) { it.setEmail("member@hedvig.com") })
      .isEqualTo("ChangeEmailInput(redacted)")
    assertThat(droppedEvent(dateModel(null), name) { it.selectTravelDate("2026-12-24") })
      .isEqualTo("SelectTravelDate(date=2026-12-24)")
    assertThat(droppedEvent(dateModel(null), name) { it.submit() }).isEqualTo("Submit")
    assertNothingLoaded()
  }

  @Test
  fun `a date that is not yyyy-MM-dd is not sent`() {
    val model = dateModel(null)

    repeat(25) { model.selectTravelDate("2026-02-30") }
    model.selectTravelDate("24/12/2026")
    model.close()

    assertThat(logs.droppedEvents("TravelCertificateDateInputViewModel")).isEmpty()
    assertThat(
      logs.entries().count { (priority, message) ->
        priority == LogPriority.WARN &&
          message == "TravelCertificateDateInputScreenModel ignored a date that is not yyyy-MM-dd"
      },
    ).isEqualTo(26)
  }

  @Test
  fun `each travellers model function reaches the ViewModel as its event`() {
    val name = "TravelCertificateTravellersInputViewModel"

    assertThat(droppedEvent(travellersModel(), name) { it.reload() }).isEqualTo("RetryLoadData")
    assertThat(droppedEvent(travellersModel(), name) { it.setMemberIncluded(false) })
      .isEqualTo("SetMemberIncluded(included=false)")
    assertThat(droppedEvent(travellersModel(), name) { it.setCoInsuredIncluded("backend-anna", true) })
      .isEqualTo("SetCoInsuredIncluded(included=true)")
    assertThat(droppedEvent(travellersModel(), name) { it.create() }).isEqualTo("GenerateTravelCertificate")
    assertNothingLoaded()
  }

  /**
   * Pins what 25 email edits do to a model nobody collects: the first 20 wait in the buffer, the newest 5 are dropped
   * and logged without the text, and a collection that starts later drains the buffer, after which edits fit again.
   */
  @Test
  fun `25 email edits while not collected keep the oldest 20 and drop the newest 5`() {
    val model = dateModel("contract-1")
    val droppedAfterEachEdit = (1..25).map { edit ->
      model.setEmail("m".repeat(edit) + "@hedvig.com")
      logs.droppedEvents("TravelCertificateDateInputViewModel").size
    }

    assertThat(droppedAfterEachEdit).isEqualTo(List(20) { 0 } + listOf(1, 2, 3, 4, 5))
    assertThat(logs.droppedEvents("TravelCertificateDateInputViewModel").distinct())
      .containsExactly("ChangeEmailInput(redacted)")
    assertThat(logs.messages().none { "@hedvig.com" in it }).isTrue()

    collections.launch { model.stateUpdates.collect {} }
    assertThat(pumpMainLoopUntil { specifications.contractIds().size == 1 }).isTrue()
    pumpMainLoop(1.seconds)
    repeat(20) { model.setEmail("later@hedvig.com") }
    model.close()

    assertThat(logs.droppedEvents("TravelCertificateDateInputViewModel").size).isEqualTo(5)
  }

  @Test
  fun `closing each model clears its ViewModel once and drops later events`() {
    val historyModel = historyModel()
    val chooseModel = chooseModel()
    val dateModel = dateModel("contract-1")
    val travellersModel = travellersModel()

    listOf(historyModel, chooseModel, dateModel, travellersModel).forEach {
      it.close()
      it.close()
    }
    repeat(25) {
      historyModel.reload()
      chooseModel.reload()
      dateModel.setEmail("member@hedvig.com")
      dateModel.submit()
      travellersModel.setCoInsuredIncluded("backend-anna", true)
      travellersModel.create()
    }
    pumpMainLoop(1.seconds)

    assertThat(logs.messages().filter { it.endsWith("ViewModel cleared") }.sorted()).containsExactly(
      "CertificateHistoryViewModel cleared",
      "ChooseContractForCertificateViewModel cleared",
      "TravelCertificateDateInputViewModel cleared",
      "TravelCertificateTravellersInputViewModel cleared",
    )
    assertThat(logs.entries().filter { (priority, _) -> priority == LogPriority.WARN }).isEmpty()
    assertNothingLoaded()
  }

  private fun assertNothingLoaded() {
    assertThat(history.calls.value).isEqualTo(0)
    assertThat(availability.calls.value).isEqualTo(0)
    assertThat(eligibleContracts.calls.value).isEqualTo(0)
    assertThat(addonBanner.calls.value).isEqualTo(0)
    assertThat(specifications.contractIds()).isEmpty()
    assertThat(coInsured.contractIds()).isEmpty()
    assertThat(createTransport.inputs()).isEmpty()
  }

  private fun contractIdReachingSpecifications(contractId: String?): String? {
    val specifications = FakeSpecificationsUseCase()
    val model = createTravelCertificateDateInputScreenModel(dateFactory(specifications), contractId)
    val states = mutableListOf<TravelCertificateDateInputScreenState>()

    collections.launch { model.stateUpdates.collect { states += it } }
    assertThat(pumpMainLoopUntil { specifications.contractIds().size == 1 }).isTrue()
    pumpMainLoop(1.seconds)
    model.close()

    assertThat(states).containsExactly(TravelCertificateDateInputScreenState.Loading)
    assertThat(createTransport.inputs()).isEmpty()
    return specifications.contractIds().single()
  }

  /** Fills the buffer with 20 calls of [send], then makes one more, which is dropped; returns its event's text. */
  private fun <M : ScreenModel<*>> droppedEvent(model: M, viewModelName: String, send: (M) -> Unit): String {
    val before = logs.droppedEvents(viewModelName).size
    repeat(21) { send(model) }
    model.close()
    val dropped = logs.droppedEvents(viewModelName)
    assertThat(dropped.size - before).isEqualTo(1)
    return dropped.last()
  }

  private fun historyModel() = createTravelCertificateHistoryScreenModel(
    factory {
      CertificateHistoryViewModel(history, downloadPdf, availability, eligibleContracts, addonBanner)
    },
  )

  private fun chooseModel() = createTravelCertificateChooseContractScreenModel(
    factory {
      ChooseContractForCertificateViewModel(eligibleContracts)
    },
  )

  private fun dateModel(contractId: String?) =
    createTravelCertificateDateInputScreenModel(dateFactory(specifications), contractId)

  private fun travellersModel() = createTravelCertificateTravellersScreenModel(travellersFactory(), step)

  private fun dateFactory(specifications: FakeSpecificationsUseCase) = assistedFactory(
    TravelCertificateDateInputViewModelFactory::class to {
      TravelCertificateDateInputViewModelFactory { contractId ->
        TravelCertificateDateInputViewModel(contractId, specifications, createTransport.createUseCase(), FixedClock())
      }
    },
  )

  private fun travellersFactory() = assistedFactory(
    TravelCertificateTravellersInputViewModelFactory::class to {
      TravelCertificateTravellersInputViewModelFactory { step ->
        TravelCertificateTravellersInputViewModel(step, createTransport.createUseCase(), coInsured)
      }
    },
  )
}

private fun factory(create: () -> ViewModel): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
    @Suppress("UNCHECKED_CAST")
    return create() as T
  }
}

/** Holds only [entry], as the graph's factory holds it among its others. */
private fun assistedFactory(
  entry: Pair<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory>,
): MetroViewModelFactory = object : MetroViewModelFactory() {
  override val manualAssistedFactoryProviders:
    Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory> = mapOf(entry)
}
