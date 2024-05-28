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
import com.hedvig.android.core.common.formatShortSsn
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.CoInsuredError
import com.hedvig.android.feature.editcoinsured.data.CommitMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.CreateMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.data.FetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.data.GetCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.data.Member
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.OnAddCoInsuredClicked
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.OnDismissError
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.OnRemoveCoInsuredClicked
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.RemoveCoInsured
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.ResetAddBottomSheetState
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredEvent.ResetRemoveBottomSheetState
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Error
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loading
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
    var addBottomSheetState by remember {
      val lastBottomSheetState = lastState.safeCast<Loaded>()?.addBottomSheetState
      mutableStateOf(lastBottomSheetState ?: Loaded.AddBottomSheetState())
    }
    var removeBottomSheetState by remember {
      val lastBottomSheetState = lastState.safeCast<Loaded>()?.removeBottomSheetState
      mutableStateOf(lastBottomSheetState ?: Loaded.RemoveBottomSheetState())
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var fetchInfoFromSsn by remember { mutableIntStateOf(0) }
    var intentId by remember { mutableStateOf<String?>(null) }
    var selectedCoInsuredId by remember { mutableStateOf<String?>(null) }
    var commit by remember { mutableStateOf(false) }
    var contractUpdateDate by remember { mutableStateOf<LocalDate?>(null) }
    var editedCoInsuredList by remember { mutableStateOf<ImmutableList<CoInsured>?>(null) }

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
          val selectedCoInsured = addBottomSheetState.selectedCoInsured
          if (selectedCoInsured != null) {
            editedCoInsuredList = addCoInsured(selectedCoInsuredId, selectedCoInsured, listState)
          } else if (addBottomSheetState.shouldFetchInfo()) {
            fetchInfoFromSsn++
          } else {
            editedCoInsuredList = addCoInsuredFromBottomSheet(selectedCoInsuredId, addBottomSheetState, listState)
          }
        }

        is EditCoInsuredEvent.OnCoInsuredSelected ->
          addBottomSheetState = addBottomSheetState.copy(
            selectedCoInsured = event.coInsured,
            errorMessage = null,
          )

        is RemoveCoInsured -> {
          editedCoInsuredList = listState.coInsured.filterNot { it == event.coInsured }.toImmutableList()
        }

        is EditCoInsuredEvent.OnSsnChanged ->
          if (event.ssn.length <= 12) {
            addBottomSheetState = addBottomSheetState.copy(
              ssn = event.ssn,
              errorMessage = null,
              firstName = null,
              lastName = null,
            )
          }

        is EditCoInsuredEvent.OnBirthDateChanged ->
          addBottomSheetState =
            addBottomSheetState.copy(birthDate = event.birthDate, errorMessage = null)

        is EditCoInsuredEvent.OnFirstNameChanged ->
          addBottomSheetState =
            addBottomSheetState.copy(firstName = event.firstName, errorMessage = null)

        is EditCoInsuredEvent.OnLastNameChanged ->
          addBottomSheetState =
            addBottomSheetState.copy(lastName = event.lastName, errorMessage = null)

        is EditCoInsuredEvent.OnManualInputSwitchChanged -> {
          addBottomSheetState =
            Loaded.AddBottomSheetState(
              showManualInput = event.show,
              show = true,
            )
        }

        is EditCoInsuredEvent.OnEditCoInsuredClicked -> {
          selectedCoInsuredId = event.coInsured.internalId
          addBottomSheetState = Loaded.AddBottomSheetState(
            firstName = event.coInsured.firstName,
            lastName = event.coInsured.lastName,
            ssn = event.coInsured.ssn,
            birthDate = event.coInsured.birthDate,
            selectableCoInsured = listState.allCoInsured,
            show = true,
          )
        }

        is OnRemoveCoInsuredClicked -> removeBottomSheetState = Loaded.RemoveBottomSheetState(
          coInsured = event.coInsured,
          show = true,
        )

        OnAddCoInsuredClicked -> addBottomSheetState = Loaded.AddBottomSheetState(
          show = true,
          selectableCoInsured = listState.allCoInsured,
        )

        EditCoInsuredEvent.OnAddNewCoInsured ->
          addBottomSheetState =
            addBottomSheetState.copy(
              selectableCoInsured = null,
              selectedCoInsured = null,
              errorMessage = null,
            )

        ResetAddBottomSheetState -> {
          addBottomSheetState = Loaded.AddBottomSheetState()
        }

        ResetRemoveBottomSheetState -> removeBottomSheetState = Loaded.RemoveBottomSheetState()
        OnDismissError -> errorMessage = null
        EditCoInsuredEvent.OnCommitChanges -> commit = true
      }
    }

    LaunchedEffect(fetchInfoFromSsn) {
      addBottomSheetState = addBottomSheetState.copy(errorMessage = null)
      val ssn = addBottomSheetState.ssn
      if (ssn != null) {
        val paddedSsn = formatShortSsn(ssn)
        either {
          val result = fetchCoInsuredPersonalInformationUseCase.invoke(paddedSsn).bind()
          addBottomSheetState = addBottomSheetState.copy(
            firstName = result.firstName,
            lastName = result.lastName,
            ssn = paddedSsn,
            errorMessage = null,
          )
        }.onLeft {
          addBottomSheetState = addBottomSheetState.copy(errorMessage = it.message)
        }
      }
    }

    LaunchedEffect(editedCoInsuredList) {
      editedCoInsuredList?.let { list ->
        Snapshot.withMutableSnapshot {
          addBottomSheetState = addBottomSheetState.copy(isLoading = true)
          removeBottomSheetState = removeBottomSheetState.copy(isLoading = true)
        }

        createMidtermChangeUseCase
          .invoke(contractId, list)
          .fold(
            ifLeft = {
              Snapshot.withMutableSnapshot {
                removeBottomSheetState = removeBottomSheetState.copy(
                  errorMessage = it.message,
                  isLoading = false,
                )
                addBottomSheetState = addBottomSheetState.copy(
                  errorMessage = it.message,
                  isLoading = false,
                )
              }
            },
            ifRight = {
              Snapshot.withMutableSnapshot {
                intentId = it.id
                listState = listState.copy(
                  updatedCoInsured = it.coInsured,
                  priceInfo = Loaded.PriceInfo(
                    previousPrice = it.currentPremium,
                    newPrice = it.newPremium,
                    validFrom = it.activatedDate,
                  ),
                )
                selectedCoInsuredId = null
                addBottomSheetState = Loaded.AddBottomSheetState(show = false)
                removeBottomSheetState = Loaded.RemoveBottomSheetState(show = false)
                editedCoInsuredList = null
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
        addBottomSheetState = addBottomSheetState,
        removeBottomSheetState = removeBottomSheetState,
        contractUpdateDate = contractUpdateDate,
      )
    } else if (isLoading) {
      Loading
    } else {
      Error("Could not fetch member")
    }
  }

  private fun addCoInsuredFromBottomSheet(
    selectedCoInsuredId: String?,
    addBottomSheetState: Loaded.AddBottomSheetState,
    listState: Loaded.CoInsuredListState,
  ) = if (selectedCoInsuredId != null) {
    val updatedCoInsured = CoInsured(
      internalId = selectedCoInsuredId,
      firstName = addBottomSheetState.firstName,
      lastName = addBottomSheetState.lastName,
      birthDate = addBottomSheetState.birthDate,
      ssn = addBottomSheetState.ssn,
      hasMissingInfo = false,
    )
    val old = listState.coInsured.first { it.internalId == selectedCoInsuredId }
    listState.coInsured.updated(old, updatedCoInsured).toImmutableList()
  } else {
    val updatedCoInsured = CoInsured(
      firstName = addBottomSheetState.firstName,
      lastName = addBottomSheetState.lastName,
      birthDate = addBottomSheetState.birthDate,
      ssn = addBottomSheetState.ssn,
      hasMissingInfo = false,
    )
    (listState.coInsured + updatedCoInsured).toImmutableList()
  }

  private fun addCoInsured(selectedCoInsuredId: String?, coInsured: CoInsured, listState: Loaded.CoInsuredListState) =
    if (selectedCoInsuredId != null) {
      val old = listState.coInsured.first { it.internalId == selectedCoInsuredId }
      listState.coInsured.updated(old, coInsured).toImmutableList()
    } else {
      (listState.coInsured + coInsured).toImmutableList()
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
    val addBottomSheetState: AddBottomSheetState,
    val removeBottomSheetState: RemoveBottomSheetState,
    val contractUpdateDate: LocalDate? = null,
  ) : EditCoInsuredState {
    data class CoInsuredListState(
      val originalCoInsured: ImmutableList<CoInsured>? = null,
      val updatedCoInsured: ImmutableList<CoInsured>? = null,
      val allCoInsured: ImmutableList<CoInsured>? = null,
      val member: Member? = null,
      val priceInfo: PriceInfo? = null,
      val isCommittingUpdate: Boolean = false,
    ) {
      val coInsured = updatedCoInsured ?: originalCoInsured ?: persistentListOf()

      fun hasMadeChanges() = priceInfo != null &&
        originalCoInsured != null &&
        updatedCoInsured != null &&
        originalCoInsured != updatedCoInsured

      fun noCoInsuredHaveMissingInfo() = coInsured.all { !it.hasMissingInfo }
    }

    data class PriceInfo(
      val previousPrice: UiMoney,
      val newPrice: UiMoney,
      val validFrom: LocalDate,
    )

    data class AddBottomSheetState(
      val firstName: String? = null,
      val lastName: String? = null,
      val ssn: String? = null,
      val birthDate: LocalDate? = null,
      val showManualInput: Boolean = false,
      val selectableCoInsured: ImmutableList<CoInsured>? = null,
      val selectedCoInsured: CoInsured? = null,
      val errorMessage: String? = null,
      val isLoading: Boolean = false,
      val show: Boolean = false,
    ) {
      fun canPickExistingCoInsured() = !selectableCoInsured.isNullOrEmpty()

      fun canContinue() = (showManualInput && firstName != null && lastName != null && birthDate != null) ||
        (!showManualInput && ssn?.length == 12) ||
        (selectedCoInsured != null)

      fun shouldFetchInfo() = !showManualInput && ssn != null && firstName == null && lastName == null

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

    data class RemoveBottomSheetState(
      val coInsured: CoInsured? = null,
      val errorMessage: String? = null,
      val isLoading: Boolean = false,
      val show: Boolean = false,
    )
  }
}

private fun <T> Iterable<T>.updated(old: T, new: T): List<T> = map { if (it == old) new else it }
