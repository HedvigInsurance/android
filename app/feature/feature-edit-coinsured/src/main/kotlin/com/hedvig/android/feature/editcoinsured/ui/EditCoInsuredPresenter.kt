package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import arrow.core.raise.either
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.CoInsuredError
import com.hedvig.android.feature.editcoinsured.data.CoInsuredPersonalInformation
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.Member
import com.hedvig.android.feature.editcoinsured.data.MonthlyCost
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.OnAddCoInsuredClicked
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.OnDismissError
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.OnRemoveCoInsuredClicked
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.RemoveCoInsured
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.ResetAddBottomSheetState
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.ResetRemoveBottomSheetState
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Error
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.InfoFromSsn
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.ManualInfo
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loading
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class EditCoInsuredPresenter(
  private val contractId: String,
  private val getCoInsuredUseCase: GetCoInsuredUseCase,
  private val fetchCoInsuredPersonalInformationUseCase: FetchCoInsuredPersonalInformationUseCase,
  private val createMidtermChangeUseCase: CreateMidtermChangeUseCase,
  private val commitMidtermChangeUseCase: CommitMidtermChangeUseCase,
) : MoleculePresenter<EditCoInsuredEvent, EditCoInsuredState> {
  @Composable
  override fun MoleculePresenterScope<EditCoInsuredEvent>.present(lastState: EditCoInsuredState): EditCoInsuredState {
    var listState by remember {
      val lastListState = lastState.safeCast<Loaded>()?.listState
      mutableStateOf(lastListState ?: Loaded.CoInsuredListState())
    }
    var addBottomSheetContentState by remember {
      val lastBottomSheetState = lastState.safeCast<Loaded>()?.addBottomSheetContentState
      mutableStateOf(
        lastBottomSheetState ?: Loaded.AddBottomSheetContentState(
          manualInfo = ManualInfo(),
          infoFromSsn = InfoFromSsn(),
        ),
      )
    }
    var removeBottomSheetContentState by remember {
      val lastBottomSheetState = lastState.safeCast<Loaded>()?.removeBottomSheetContentState
      mutableStateOf(lastBottomSheetState ?: Loaded.RemoveBottomSheetContentState())
    }

    var finishedAdding by remember { mutableStateOf(false) }
    var finishedRemoving by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var fetchInfoFromSsn by remember { mutableIntStateOf(0) }
    var intentId by remember { mutableStateOf<String?>(null) }
    var selectedCoInsuredId by remember { mutableStateOf<String?>(null) }
    var commit by remember { mutableStateOf(false) }
    var contractUpdateDate by remember { mutableStateOf<LocalDate?>(null) }
    var editedCoInsuredList by remember { mutableStateOf<List<CoInsured>?>(null) }

    LaunchedEffect(Unit) {
      if (listState.member != null) {
        return@LaunchedEffect
      }
      isLoading = true
      either {
        val result = getCoInsuredUseCase.invoke(contractId).bind()
        Snapshot.withMutableSnapshot {
          errorMessage = null
          isLoading = false
          listState = Loaded.CoInsuredListState(
            originalCoInsured = result.coInsuredOnContract,
            allCoInsured = result.allCoInsured,
            member = result.member,
          )
        }
      }.onLeft {
        Snapshot.withMutableSnapshot {
          isLoading = false
          errorMessage = when (it) {
            CoInsuredError.ContractNotFound -> "Could not find contract"
            is CoInsuredError.GenericError -> it.message
          }
        }
      }
    }

    CollectEvents { event ->
      when (event) {
        EditCoInsuredEvent.OnBottomSheetContinue -> {
          val selectedCoInsured = addBottomSheetContentState.selectedCoInsured
          if (selectedCoInsured != null) {
            editedCoInsuredList = addCoInsured(selectedCoInsuredId, selectedCoInsured, listState)
          } else if (addBottomSheetContentState.shouldFetchInfo()) {
            fetchInfoFromSsn++
          } else {
            editedCoInsuredList =
              addCoInsuredFromBottomSheet(selectedCoInsuredId, addBottomSheetContentState, listState)
          }
        }

        is EditCoInsuredEvent.OnCoInsuredSelected ->
          addBottomSheetContentState = addBottomSheetContentState.copy(
            selectedCoInsured = event.coInsured,
            errorMessage = null,
          )

        is RemoveCoInsured -> {
          editedCoInsuredList = listState.coInsured.filterNot { it == event.coInsured }
        }

        is EditCoInsuredEvent.OnSsnChanged ->
          if (event.ssn.length <= 12) {
            addBottomSheetContentState = addBottomSheetContentState.copy(
              manualInfo = ManualInfo(),
              infoFromSsn = InfoFromSsn(
                ssn = event.ssn,
                firstName = null,
                lastName = null,
              ),
              errorMessage = null,
            )
          }

        is EditCoInsuredEvent.OnBirthDateChanged ->
          addBottomSheetContentState =
            addBottomSheetContentState.copy(
              manualInfo = addBottomSheetContentState.manualInfo.copy(birthDate = event.birthDate),
              errorMessage = null,
            )

        is EditCoInsuredEvent.OnFirstNameChanged ->
          addBottomSheetContentState =
            addBottomSheetContentState.copy(
              manualInfo = addBottomSheetContentState.manualInfo.copy(firstName = event.firstName),
              errorMessage = null,
            )

        is EditCoInsuredEvent.OnLastNameChanged ->
          addBottomSheetContentState =
            addBottomSheetContentState.copy(
              manualInfo = addBottomSheetContentState.manualInfo.copy(lastName = event.lastName),
              errorMessage = null,
            )

        is EditCoInsuredEvent.OnManualInputSwitchChanged -> {
          addBottomSheetContentState = addBottomSheetContentState.copy(
            showManualInput = event.show,
            errorMessage = null,
            showUnderAgedInfo = if (!event.show) false else addBottomSheetContentState.showUnderAgedInfo,
          )
        }

        is EditCoInsuredEvent.OnEditCoInsuredClicked -> {
          finishedAdding = false
          selectedCoInsuredId = event.coInsured.internalId
          addBottomSheetContentState = Loaded.AddBottomSheetContentState(
            manualInfo = ManualInfo(
              firstName = event.coInsured.firstName,
              lastName = event.coInsured.lastName,
              birthDate = event.coInsured.birthDate,
            ),
            infoFromSsn = InfoFromSsn(
              firstName = event.coInsured.firstName,
              lastName = event.coInsured.lastName,
              ssn = event.coInsured.ssn,
            ),
            selectableCoInsured = listState.allCoInsured,
          )
        }

        is OnRemoveCoInsuredClicked -> {
          finishedRemoving = false
          removeBottomSheetContentState = Loaded.RemoveBottomSheetContentState(
            coInsured = event.coInsured,
          )
        }

        OnAddCoInsuredClicked -> {
          finishedAdding = false
          addBottomSheetContentState = Loaded.AddBottomSheetContentState(
            selectableCoInsured = listState.allCoInsured,
            manualInfo = ManualInfo(),
            infoFromSsn = InfoFromSsn(),
          )
        }

        EditCoInsuredEvent.OnAddNewCoInsured ->
          addBottomSheetContentState =
            addBottomSheetContentState.copy(
              selectableCoInsured = null,
              selectedCoInsured = null,
              errorMessage = null,
            )

        ResetAddBottomSheetState -> {
          addBottomSheetContentState = Loaded.AddBottomSheetContentState(
            infoFromSsn = InfoFromSsn(),
            manualInfo = ManualInfo(),
          )
        }

        ResetRemoveBottomSheetState -> removeBottomSheetContentState = Loaded.RemoveBottomSheetContentState()
        OnDismissError -> errorMessage = null
        EditCoInsuredEvent.OnCommitChanges -> commit = true
      }
    }

    LaunchedEffect(fetchInfoFromSsn) {
      addBottomSheetContentState = addBottomSheetContentState.copy(errorMessage = null)
      val ssn = addBottomSheetContentState.infoFromSsn.ssn
      if (ssn != null && !addBottomSheetContentState.showManualInput) {
        either {
          val result = fetchCoInsuredPersonalInformationUseCase.invoke(ssn).bind()
          when (result) {
            is CoInsuredPersonalInformation.FullInfo -> {
              addBottomSheetContentState = addBottomSheetContentState.copy(
                infoFromSsn = InfoFromSsn(
                  firstName = result.firstName,
                  lastName = result.lastName,
                  ssn = ssn,
                ),
                errorMessage = null,
              )
            }

            is CoInsuredPersonalInformation.EmptyInfo -> {
              addBottomSheetContentState =
                Loaded.AddBottomSheetContentState(
                  showManualInput = true,
                  manualInfo = ManualInfo(null, null, birthDate = result.dateOfBirth),
                  infoFromSsn = InfoFromSsn(),
                  showUnderAgedInfo = true,
                )
            }
          }
        }.onLeft {
          addBottomSheetContentState = addBottomSheetContentState.copy(errorMessage = it.message)
        }
      }
    }

    LaunchedEffect(editedCoInsuredList) {
      editedCoInsuredList?.let { list ->
        Snapshot.withMutableSnapshot {
          addBottomSheetContentState = addBottomSheetContentState.copy(isLoading = true)
          removeBottomSheetContentState = removeBottomSheetContentState.copy(isLoading = true)
        }

        createMidtermChangeUseCase
          .invoke(contractId, list)
          .fold(
            ifLeft = {
              Snapshot.withMutableSnapshot {
                removeBottomSheetContentState = removeBottomSheetContentState.copy(
                  errorMessage = it.message,
                  isLoading = false,
                )
                addBottomSheetContentState = addBottomSheetContentState.copy(
                  errorMessage = it.message,
                  isLoading = false,
                )
              }
            },
            ifRight = {
              Snapshot.withMutableSnapshot {
                val originalCoInsuredIds = listState.originalCoInsured?.map { originalCoInsured ->
                  originalCoInsured.id
                } ?: emptyList()
                val updatedCoinsuredList = it.coInsured
                val updatedCoinsuredListWithDate = updateCoInsuredWithActivationDates(
                  coInsured = updatedCoinsuredList,
                  originalIds = originalCoInsuredIds,
                  activationDate = it.activatedDate,
                ) // todo: check here
                intentId = it.id
                listState = listState.copy(
                  updatedCoInsured = updatedCoinsuredListWithDate,
                  priceInfo = Loaded.PriceInfo(
                    currentCost = it.currentCost,
                    newCost = it.newCost,
                    validFrom = it.activatedDate,
                    newCostBreakDown = it.newCostBreakDown,
                  ),
                )
                selectedCoInsuredId = null
                addBottomSheetContentState = Loaded.AddBottomSheetContentState(
                  manualInfo = ManualInfo(),
                  infoFromSsn = InfoFromSsn(),
                )
                removeBottomSheetContentState = Loaded.RemoveBottomSheetContentState()
                editedCoInsuredList = null
                finishedAdding = true
                finishedRemoving = true
              }
            },
          )
      }
    }

    if (commit) {
      LaunchedEffect(Unit) {
        intentId?.let {
          listState = listState.copy(isCommittingUpdate = true)
          commitMidtermChangeUseCase
            .invoke(it)
            .fold(
              ifLeft = {
                Snapshot.withMutableSnapshot {
                  listState = listState.copy(isCommittingUpdate = false)
                  errorMessage = it.message
                  commit = false
                }
              },
              ifRight = {
                Snapshot.withMutableSnapshot {
                  listState = listState.copy(isCommittingUpdate = false)
                  contractUpdateDate = it.contractUpdateDate
                }
              },
            )
        }
      }
    }

    return if (errorMessage != null) {
      Error(errorMessage)
    } else if (listState.member != null) {
      Loaded(
        listState = listState,
        addBottomSheetContentState = addBottomSheetContentState,
        removeBottomSheetContentState = removeBottomSheetContentState,
        contractUpdateDate = contractUpdateDate,
        finishedAdding = finishedAdding,
        finishedRemoving = finishedRemoving,
      )
    } else if (isLoading) {
      Loading
    } else {
      Error("Could not fetch member")
    }
  }

  private fun addCoInsuredFromBottomSheet(
    selectedCoInsuredId: String?,
    addBottomSheetContentState: Loaded.AddBottomSheetContentState,
    listState: Loaded.CoInsuredListState,
  ): List<CoInsured> {
    with(addBottomSheetContentState) {
      val firstName = if (showManualInput) manualInfo.firstName else infoFromSsn.firstName
      val lastName = if (showManualInput) manualInfo.lastName else infoFromSsn.lastName
      val ssn = if (showManualInput) null else infoFromSsn.ssn
      val birthDate = if (showManualInput) manualInfo.birthDate else null
      return if (selectedCoInsuredId != null) {
        val updatedCoInsured = CoInsured(
          internalId = selectedCoInsuredId,
          firstName = firstName,
          lastName = lastName,
          birthDate = birthDate,
          ssn = ssn,
          hasMissingInfo = false,
          activatesOn = null, // todo: would that be a correct way? we don't know anything yet here about dates
          terminatesOn = null,
        )
        val old = listState.coInsured.first { it.internalId == selectedCoInsuredId }
        listState.coInsured.updated(old, updatedCoInsured)
      } else {
        val updatedCoInsured = CoInsured(
          firstName = firstName,
          lastName = lastName,
          birthDate = birthDate,
          ssn = ssn,
          hasMissingInfo = false,
          activatesOn = null, // todo: would that be a correct way? we don't know anything yet here about dates
          terminatesOn = null,
        )
        val result = (listState.coInsured + updatedCoInsured)
        result
      }
    }
  }

  private fun updateCoInsuredWithActivationDates(
    coInsured: List<CoInsured>,
    originalIds: List<String>,
    activationDate: LocalDate,
  ): List<CoInsured> {
    return coInsured.map { it ->
      if (originalIds.contains(it.id)) {
        it
      } else {
        it.copy(activatesOn = activationDate)
      }
    }
  }

  private fun addCoInsured(selectedCoInsuredId: String?, coInsured: CoInsured, listState: Loaded.CoInsuredListState) =
    if (selectedCoInsuredId != null) {
      val old = listState.coInsured.first { it.internalId == selectedCoInsuredId }
      listState.coInsured.updated(old, coInsured)
    } else {
      (listState.coInsured + coInsured)
    }
}

internal sealed interface EditCoInsuredEvent {
  data object OnBottomSheetContinue : EditCoInsuredEvent

  data class RemoveCoInsured(val coInsured: CoInsured) : EditCoInsuredEvent

  data class OnEditCoInsuredClicked(val coInsured: CoInsured) : EditCoInsuredEvent

  data object OnDismissError : EditCoInsuredEvent

  data object OnAddCoInsuredClicked : EditCoInsuredEvent

  data object ResetAddBottomSheetState : EditCoInsuredEvent

  data object ResetRemoveBottomSheetState : EditCoInsuredEvent

  data class OnRemoveCoInsuredClicked(val coInsured: CoInsured) : EditCoInsuredEvent

  data object OnCommitChanges : EditCoInsuredEvent

  data object OnAddNewCoInsured : EditCoInsuredEvent

  data class OnFirstNameChanged(val firstName: String) : EditCoInsuredEvent

  data class OnLastNameChanged(val lastName: String) : EditCoInsuredEvent

  data class OnBirthDateChanged(val birthDate: LocalDate) : EditCoInsuredEvent

  data class OnManualInputSwitchChanged(val show: Boolean) : EditCoInsuredEvent

  data class OnCoInsuredSelected(val coInsured: CoInsured) : EditCoInsuredEvent

  data class OnSsnChanged(val ssn: String) : EditCoInsuredEvent
}

internal sealed interface EditCoInsuredState {
  data object Loading : EditCoInsuredState

  data class Error(val message: String?) : EditCoInsuredState

  data class Loaded(
    val listState: CoInsuredListState,
    val addBottomSheetContentState: AddBottomSheetContentState,
    val removeBottomSheetContentState: RemoveBottomSheetContentState,
    val contractUpdateDate: LocalDate? = null,
    val finishedAdding: Boolean = false,
    val finishedRemoving: Boolean = false,
  ) : EditCoInsuredState {
    data class CoInsuredListState(
      val originalCoInsured: List<CoInsured>? = null,
      val updatedCoInsured: List<CoInsured>? = null,
      val allCoInsured: List<CoInsured>? = null,
      val member: Member? = null,
      val priceInfo: PriceInfo? = null,
      val isCommittingUpdate: Boolean = false,
    ) {
      val coInsured = updatedCoInsured ?: originalCoInsured ?: listOf()

      fun hasMadeChanges() = priceInfo != null &&
        originalCoInsured != null &&
        updatedCoInsured != null &&
        originalCoInsured != updatedCoInsured

      fun noCoInsuredHaveMissingInfo() = coInsured.all { !it.hasMissingInfo }
    }

    data class PriceInfo(
      val currentCost: MonthlyCost,
      val newCost: MonthlyCost,
      val newCostBreakDown: List<Pair<String, String>>,
      val validFrom: LocalDate,
    )

    data class ManualInfo(
      val firstName: String? = null,
      val lastName: String? = null,
      val birthDate: LocalDate? = null,
    )

    data class InfoFromSsn(
      val firstName: String? = null,
      val lastName: String? = null,
      val ssn: String? = null,
    )

    data class AddBottomSheetContentState(
      val infoFromSsn: InfoFromSsn,
      val manualInfo: ManualInfo,
      val showManualInput: Boolean = false,
      val selectableCoInsured: List<CoInsured>? = null,
      val selectedCoInsured: CoInsured? = null,
      val errorMessage: String? = null,
      val isLoading: Boolean = false,
      val showUnderAgedInfo: Boolean = false,
    ) {
      fun canPickExistingCoInsured() = !selectableCoInsured.isNullOrEmpty()

      fun canContinue() = (
        showManualInput &&
          manualInfo.firstName != null &&
          manualInfo.lastName != null &&
          manualInfo.birthDate != null
      ) ||
        (!showManualInput && infoFromSsn.ssn?.length == 12) ||
        (selectedCoInsured != null)

      fun shouldFetchInfo() =
        !showManualInput && infoFromSsn.ssn != null && infoFromSsn.firstName == null && infoFromSsn.lastName == null

      fun getSaveLabel() = if (shouldFetchInfo()) {
        SaveButtonLabel.FETCH_INFO
      } else {
        SaveButtonLabel.ADD
      }

      enum class SaveButtonLabel {
        FETCH_INFO,
        ADD,
      }

      val displayName: String = buildString {
        val firstName = if (showManualInput) manualInfo.firstName else infoFromSsn.firstName
        val lastName = if (showManualInput) manualInfo.lastName else infoFromSsn.lastName
        if (firstName != null) {
          append(firstName)
        }
        if (firstName != null && lastName != null) {
          append(" ")
        }
        if (lastName != null) {
          append(lastName)
        }
      }
    }

    data class RemoveBottomSheetContentState(
      val coInsured: CoInsured? = null,
      val errorMessage: String? = null,
      val isLoading: Boolean = false,
    )
  }
}

private fun <T> Iterable<T>.updated(old: T, new: T): List<T> = map { if (it == old) new else it }
