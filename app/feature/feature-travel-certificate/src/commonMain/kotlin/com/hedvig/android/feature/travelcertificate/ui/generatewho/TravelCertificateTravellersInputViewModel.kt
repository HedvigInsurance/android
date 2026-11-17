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
import com.hedvig.android.feature.travelcertificate.data.CoInsuredData
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetCoInsuredForContractUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.ui.generatewho.CoInsured.CoInsuredId
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
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
    var loadFailed by remember { mutableStateOf(false) }
    var isMemberIncluded by remember { mutableStateOf(lastContent?.isMemberIncluded ?: true) }
    var includedCoInsuredIds by remember {
      mutableStateOf(lastContent?.coInsuredList.orEmpty().filter { it.isIncluded }.map { it.id }.toSet())
    }
    // A create in flight belongs to a stopped run, so it is not restored.
    var isCreating by remember { mutableStateOf(false) }
    var createFailed by remember { mutableStateOf(lastContent?.createFailed ?: false) }
    var requestToCreate by remember { mutableStateOf<CreateRequest?>(null) }
    var createdUrl by remember {
      mutableStateOf((lastState as? TravelCertificateTravellersInputUiState.Created)?.url)
    }

    CollectEvents { event ->
      if (createdUrl != null) return@CollectEvents
      val loaded = travellers
      when (event) {
        TravelCertificateTravellersInputEvent.RetryLoadData -> {
          loadIteration++
        }

        is TravelCertificateTravellersInputEvent.SetMemberIncluded -> {
          isMemberIncluded = event.included
        }

        is TravelCertificateTravellersInputEvent.SetCoInsuredIncluded -> {
          if (loaded == null || loaded.coInsured.none { it.id == event.id }) return@CollectEvents
          includedCoInsuredIds = if (event.included) {
            includedCoInsuredIds + event.id
          } else {
            includedCoInsuredIds - event.id
          }
        }

        TravelCertificateTravellersInputEvent.GenerateTravelCertificate -> {
          if (loaded == null || isCreating) return@CollectEvents
          val includedCoInsured = loaded.coInsured
            .filter { it.id in includedCoInsuredIds }
            .map { it.copy(isIncluded = true) }
          if (!isMemberIncluded && includedCoInsured.isEmpty()) return@CollectEvents
          Snapshot.withMutableSnapshot {
            isCreating = true
            createFailed = false
            requestToCreate = CreateRequest(isMemberIncluded, includedCoInsured)
          }
        }
      }
    }

    if (createdUrl == null) {
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
                CoInsured(
                  id = CoInsuredId(coInsuredData),
                  name = "${coInsuredData.firstName} ${coInsuredData.lastName}",
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

    LaunchedEffect(requestToCreate) {
      val request = requestToCreate ?: return@LaunchedEffect
      createTravelCertificateUseCase.invoke(
        contractId = primaryInput.contractId,
        startDate = primaryInput.travelDate,
        isMemberIncluded = request.isMemberIncluded,
        coInsured = request.coInsured,
        email = primaryInput.email,
      ).fold(
        ifLeft = {
          Snapshot.withMutableSnapshot {
            isCreating = false
            createFailed = true
          }
        },
        ifRight = { url ->
          createdUrl = url
        },
      )
      requestToCreate = null
    }

    val url = createdUrl
    val loaded = travellers
    return when {
      url != null -> TravelCertificateTravellersInputUiState.Created(url)

      loaded != null -> TravelCertificateTravellersInputUiState.Success(
        memberFullName = loaded.memberFullName,
        isMemberIncluded = isMemberIncluded,
        coInsuredList = loaded.coInsured.map { it.copy(isIncluded = it.id in includedCoInsuredIds) },
        coInsuredHasMissingInfo = loaded.coInsuredHasMissingInfo,
        isCreating = isCreating,
        createFailed = createFailed,
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
  }

  /** The certificate exists; nothing follows on this screen. */
  data class Created(val url: TravelCertificateUrl) : TravelCertificateTravellersInputUiState
}

internal sealed interface TravelCertificateTravellersInputEvent {
  data object RetryLoadData : TravelCertificateTravellersInputEvent

  /** Ignored while a create is in flight and when nobody is included. */
  data object GenerateTravelCertificate : TravelCertificateTravellersInputEvent

  data class SetMemberIncluded(val included: Boolean) : TravelCertificateTravellersInputEvent

  /** Ignored when no loaded co-insured has [id]. */
  data class SetCoInsuredIncluded(val id: CoInsuredId, val included: Boolean) : TravelCertificateTravellersInputEvent
}

@Serializable
internal data class CoInsured(
  val id: CoInsuredId,
  val name: String,
  val ssn: String?,
  val dateOfBirth: LocalDate?,
  val isIncluded: Boolean,
) {
  @Serializable
  @JvmInline
  value class CoInsuredId(val value: String) {
    companion object {
      operator fun invoke(coInsuredData: CoInsuredData) = CoInsuredId(
        // The backend may return no id. The one built from the person's data is the same on every poll, so a
        // selection keyed by it survives a refetch.
        coInsuredData.id ?: listOf(
          coInsuredData.firstName,
          coInsuredData.lastName,
          coInsuredData.ssn,
          coInsuredData.dateOfBirth?.toString(),
        ).joinToString(separator = "|") { it.orEmpty() },
      )
    }
  }
}
