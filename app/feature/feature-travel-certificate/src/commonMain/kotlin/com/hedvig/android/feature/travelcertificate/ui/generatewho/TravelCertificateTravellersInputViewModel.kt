package com.hedvig.android.feature.travelcertificate.ui.generatewho

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetCoInsuredForContractUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.molecule.public.AttemptStatus
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.molecule.public.rememberAttempts
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlin.jvm.JvmInline
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class TravelCertificateTravellersInputViewModel(
  @Assisted primaryInput: TravelCertificateTravellersStep,
  createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  getCoInsuredForContractUseCase: GetCoInsuredForContractUseCase,
) : MoleculeViewModel<TravelCertificateTravellersInputEvent, TravelCertificateTravellersInputUiState>(
    initialState = TravelCertificateTravellersInputUiState.Loading,
    presenter = TravelCertificateTravellersInputPresenter(
      primaryInput,
      createTravelCertificateUseCase,
      getCoInsuredForContractUseCase,
    ),
  )

internal class TravelCertificateTravellersInputPresenter(
  private val primaryInput: TravelCertificateTravellersStep,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  private val getCoInsuredForContractUseCase: GetCoInsuredForContractUseCase,
) : MoleculePresenter<TravelCertificateTravellersInputEvent, TravelCertificateTravellersInputUiState> {
  @Composable
  override fun MoleculePresenterScope<TravelCertificateTravellersInputEvent>.present(
    lastState: TravelCertificateTravellersInputUiState,
  ): TravelCertificateTravellersInputUiState {
    val lastContent = lastState as? TravelCertificateTravellersInputUiState.Success
    var loadIteration by remember { mutableIntStateOf(0) }
    var travellers by remember {
      mutableStateOf(
        lastContent?.let { content ->
          Travellers(
            memberFullName = content.memberFullName,
            coInsured = content.coInsuredList.map { it.copy(isIncluded = false) },
            coInsuredHasMissingInfo = content.coInsuredHasMissingInfo,
          )
        },
      )
    }
    val generatedIds = remember { GeneratedCoInsuredIds(lastContent?.coInsuredList.orEmpty()) }
    var loadFailed by remember { mutableStateOf(false) }
    var isMemberIncluded by remember { mutableStateOf(lastContent?.isMemberIncluded ?: true) }
    var includedCoInsuredIds by remember {
      mutableStateOf(lastContent?.coInsuredList.orEmpty().filter { it.isIncluded }.map { it.id }.toSet())
    }
    var createFailed by remember { mutableStateOf(lastContent?.createFailed ?: false) }
    var created by remember { mutableStateOf(lastState as? TravelCertificateTravellersInputUiState.Created) }
    val create = rememberAttempts<CreateRequest>(
      restoredWhileRunning = lastContent?.isCreating == true,
      terminal = true,
    ) { request ->
      createTravelCertificateUseCase.invoke(
        contractId = primaryInput.contractId,
        startDate = primaryInput.travelDate,
        isMemberIncluded = request.isMemberIncluded,
        coInsured = request.coInsured,
        email = primaryInput.email,
      ).fold(
        ifLeft = {
          createFailed = true
          false
        },
        ifRight = { url ->
          created = TravelCertificateTravellersInputUiState.Created(url)
          true
        },
      )
    }

    CollectEvents { event ->
      if (created != null) return@CollectEvents
      val loaded = travellers
      when (event) {
        TravelCertificateTravellersInputEvent.RetryLoadData -> {
          loadIteration++
        }

        is TravelCertificateTravellersInputEvent.SetMemberIncluded -> {
          if (create.status == AttemptStatus.Running) return@CollectEvents
          isMemberIncluded = event.included
        }

        is TravelCertificateTravellersInputEvent.SetCoInsuredIncluded -> {
          val isCreating = create.status == AttemptStatus.Running
          if (loaded == null || isCreating || loaded.coInsured.none { it.id == event.id }) return@CollectEvents
          includedCoInsuredIds = if (event.included) {
            includedCoInsuredIds + event.id
          } else {
            includedCoInsuredIds - event.id
          }
        }

        TravelCertificateTravellersInputEvent.GenerateTravelCertificate -> {
          if (loaded == null || create.status == AttemptStatus.Running) return@CollectEvents
          val includedCoInsured = loaded.coInsured
            .filter { it.id in includedCoInsuredIds }
            .map { it.copy(isIncluded = true) }
          if (!isMemberIncluded && includedCoInsured.isEmpty()) return@CollectEvents
          if (create.start(CreateRequest(isMemberIncluded = isMemberIncluded, coInsured = includedCoInsured))) {
            createFailed = false
          }
        }
      }
    }

    if (created == null) {
      LaunchedEffect(loadIteration) {
        getCoInsuredForContractUseCase.invoke(primaryInput.contractId).collectLatest { data ->
          data.fold(
            ifLeft = {
              if (travellers == null) {
                loadFailed = true
              }
            },
            ifRight = { data ->
              val coInsuredList = data.coInsuredList.filterNot {
                it.hasMissingInfo
              }.map { coInsuredData ->
                val name = "${coInsuredData.firstName} ${coInsuredData.lastName}"
                CoInsured(
                  id = coInsuredData.id?.takeIf { it.isNotBlank() }?.let(::CoInsuredId)
                    ?: generatedIds.idFor(name, coInsuredData.ssn, coInsuredData.dateOfBirth),
                  name = name,
                  ssn = coInsuredData.ssn,
                  dateOfBirth = coInsuredData.dateOfBirth,
                  isIncluded = false,
                )
              }.sortedBy { it.name }
              Snapshot.withMutableSnapshot {
                loadFailed = false
                travellers = Travellers(
                  memberFullName = data.memberFullName,
                  coInsured = coInsuredList,
                  coInsuredHasMissingInfo = data.coInsuredList.any { it.hasMissingInfo },
                )
              }
            },
          )
        }
      }
    }

    val createdState = created
    val loaded = travellers
    return when {
      createdState != null -> createdState

      loaded != null -> TravelCertificateTravellersInputUiState.Success(
        memberFullName = loaded.memberFullName,
        isMemberIncluded = isMemberIncluded,
        coInsuredList = loaded.coInsured.map { it.copy(isIncluded = it.id in includedCoInsuredIds) },
        coInsuredHasMissingInfo = loaded.coInsuredHasMissingInfo,
        isCreating = create.status == AttemptStatus.Running,
        createFailed = when (create.status) {
          // The create stopped with the last presenter run and may have reached the server, so it is shown as failed
          // rather than as a form nobody submitted.
          AttemptStatus.Interrupted -> true

          AttemptStatus.Running, AttemptStatus.Idle -> createFailed
        },
      )

      loadFailed -> TravelCertificateTravellersInputUiState.Failure

      else -> TravelCertificateTravellersInputUiState.Loading
    }
  }
}

private data class Travellers(
  val memberFullName: String,
  val coInsured: List<CoInsured>,
  val coInsuredHasMissingInfo: Boolean,
)

/**
 * Ids for co-insured the backend returns without one. The same name, SSN and birth date get the same id on every poll
 * and after a restart from [restored], so a selection keyed by id survives both, and the id carries none of that data.
 */
private class GeneratedCoInsuredIds(restored: List<CoInsured>) {
  private val ids: MutableMap<Identity, CoInsuredId> =
    restored.associateTo(mutableMapOf()) { Identity(it.name, it.ssn, it.dateOfBirth) to it.id }
  private var lastNumber = 0

  fun idFor(name: String, ssn: String?, dateOfBirth: LocalDate?): CoInsuredId =
    ids.getOrPut(Identity(name, ssn, dateOfBirth)) {
      generateSequence { CoInsuredId("generated-${++lastNumber}") }.first { it !in ids.values }
    }

  private data class Identity(val name: String, val ssn: String?, val dateOfBirth: LocalDate?)
}

private data class CreateRequest(
  val isMemberIncluded: Boolean,
  val coInsured: List<CoInsured>,
)

internal sealed interface TravelCertificateTravellersInputUiState {
  data object Loading : TravelCertificateTravellersInputUiState

  data object Failure : TravelCertificateTravellersInputUiState

  data class Success(
    val memberFullName: String,
    val isMemberIncluded: Boolean,
    val coInsuredList: List<CoInsured>,
    val coInsuredHasMissingInfo: Boolean,
    val isCreating: Boolean,
    val createFailed: Boolean,
  ) : TravelCertificateTravellersInputUiState {
    /** A create is only sent with at least one traveller. */
    val canCreate: Boolean = isMemberIncluded || coInsuredList.any { it.isIncluded }

    /** Leaves out [memberFullName]. */
    override fun toString(): String = "Success(isMemberIncluded=$isMemberIncluded, coInsuredList=$coInsuredList, " +
      "coInsuredHasMissingInfo=$coInsuredHasMissingInfo, isCreating=$isCreating, createFailed=$createFailed)"
  }

  /** The certificate exists; nothing follows on this screen. */
  data class Created(val url: TravelCertificateUrl) : TravelCertificateTravellersInputUiState {
    /**
     * Leaving for the certificate. It is not part of equality, so the state flow keeps the first of equal states, and a
     * restart that produces this state again does not offer a second request.
     */
    val leave: NavigationRequest<TravelCertificateUrl> = NavigationRequest.of(url)
  }
}

internal sealed interface TravelCertificateTravellersInputEvent {
  data object RetryLoadData : TravelCertificateTravellersInputEvent

  /** Ignored while a create is in flight and when nobody is included. */
  data object GenerateTravelCertificate : TravelCertificateTravellersInputEvent

  /** Ignored while a create is in flight. */
  data class SetMemberIncluded(val included: Boolean) : TravelCertificateTravellersInputEvent

  /** Ignored while a create is in flight and when no loaded co-insured has [id]. */
  data class SetCoInsuredIncluded(val id: CoInsuredId, val included: Boolean) : TravelCertificateTravellersInputEvent {
    override fun toString(): String = "SetCoInsuredIncluded(included=$included)"
  }
}

@Serializable
internal data class CoInsured(
  val id: CoInsuredId,
  val name: String,
  val ssn: String?,
  val dateOfBirth: LocalDate?,
  val isIncluded: Boolean,
) {
  /** Leaves out the person's id, name, SSN and birth date. */
  override fun toString(): String = "CoInsured(isIncluded=$isIncluded)"

  @Serializable
  @JvmInline
  value class CoInsuredId(val value: String) {
    override fun toString(): String = "CoInsuredId(redacted)"
  }
}
