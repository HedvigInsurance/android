package com.hedvig.android.feature.editcoinsured.ui

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.editcoinsured.data.CoInsuredResult
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.InfoFromSsn
import com.hedvig.android.feature.editcoinsured.ui.EditCoInsuredState.Loaded.ManualInfo
import com.hedvig.android.feature.editcoinsured.ui.data.TestCommitMidtermChangeUseCase
import com.hedvig.android.feature.editcoinsured.ui.data.TestCreateMidTermChangeUseCase
import com.hedvig.android.feature.editcoinsured.ui.data.TestFetchCoInsuredPersonalInformationUseCase
import com.hedvig.android.feature.editcoinsured.ui.data.TestGetCoInsuredUseCase
import com.hedvig.android.feature.editcoinsured.ui.data.coInsuredTestList
import com.hedvig.android.feature.editcoinsured.ui.data.testContractId
import com.hedvig.android.feature.editcoinsured.ui.data.testMember
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Test

internal class EditCoInsuredPresenterTest {
  @Test
  fun `when fetching initial co-insured, should show list and member`() = runTest {
    val testGetCoInsuredUseCase = TestGetCoInsuredUseCase()
    val testFetchCoInsuredPersonalInformationUseCase = TestFetchCoInsuredPersonalInformationUseCase()
    val testCreateMidTermChangeUseCase = TestCreateMidTermChangeUseCase()
    val testCommitMidtermChangeUseCase = TestCommitMidtermChangeUseCase()

    val presenter = EditCoInsuredPresenter(
      contractId = testContractId,
      getCoInsuredUseCase = testGetCoInsuredUseCase,
      fetchCoInsuredPersonalInformationUseCase = testFetchCoInsuredPersonalInformationUseCase,
      createMidtermChangeUseCase = testCreateMidTermChangeUseCase,
      commitMidtermChangeUseCase = testCommitMidtermChangeUseCase,
    )

    presenter.test(EditCoInsuredState.Loading) {
      skipItems(1)

      testGetCoInsuredUseCase.coInsured.add(
        CoInsuredResult(
          member = testMember,
          coInsuredOnContract = coInsuredTestList,
          allCoInsured = listOf(),
        ),
      )

      awaitItem().let {
        assertThat(it).isInstanceOf<EditCoInsuredState.Loaded>()
        val uiState = it as EditCoInsuredState.Loaded
        assertThat(uiState.listState.coInsured).isEqualTo(coInsuredTestList)
        assertThat(uiState.listState.member).isEqualTo(testMember)
      }
    }
  }

  @Test
  fun `should open bottom sheet with info from selected co-insured`() = runTest {
    val testGetCoInsuredUseCase = TestGetCoInsuredUseCase()
    val testFetchCoInsuredPersonalInformationUseCase = TestFetchCoInsuredPersonalInformationUseCase()
    val testCreateMidTermChangeUseCase = TestCreateMidTermChangeUseCase()
    val testCommitMidtermChangeUseCase = TestCommitMidtermChangeUseCase()

    val presenter = EditCoInsuredPresenter(
      contractId = testContractId,
      getCoInsuredUseCase = testGetCoInsuredUseCase,
      fetchCoInsuredPersonalInformationUseCase = testFetchCoInsuredPersonalInformationUseCase,
      createMidtermChangeUseCase = testCreateMidTermChangeUseCase,
      commitMidtermChangeUseCase = testCommitMidtermChangeUseCase,
    )

    presenter.test(
      EditCoInsuredState.Loaded(
        listState = EditCoInsuredState.Loaded.CoInsuredListState(
          originalCoInsured = coInsuredTestList,
          member = testMember,
          allCoInsured = listOf(),
        ),
        addBottomSheetState = EditCoInsuredState.Loaded.AddBottomSheetState(
          infoFromSsn = InfoFromSsn(),
          manualInfo = ManualInfo(),
        ),
        removeBottomSheetState = EditCoInsuredState.Loaded.RemoveBottomSheetState(),
      ),
    ) {
      skipItems(1)

      sendEvent(EditCoInsuredEvent.OnEditCoInsuredClicked(coInsuredTestList[0]))

      val item = awaitItem()
      assertThat(item).isInstanceOf<EditCoInsuredState.Loaded>()
      val uiState = item as EditCoInsuredState.Loaded

      assertThat(uiState.addBottomSheetState.manualInfo.firstName).isEqualTo(coInsuredTestList[0].firstName)
      assertThat(uiState.addBottomSheetState.manualInfo.lastName).isEqualTo(coInsuredTestList[0].lastName)
      assertThat(uiState.addBottomSheetState.infoFromSsn.ssn).isEqualTo(coInsuredTestList[0].ssn)
      assertThat(uiState.addBottomSheetState.manualInfo.birthDate).isEqualTo(coInsuredTestList[0].birthDate)
      assertThat(uiState.addBottomSheetState.show).isEqualTo(true)
    }
  }

  @Test
  fun `should update co-insured from the right mode, manual or ssn fetch, when saving from bottom sheet`() = runTest {
    val testGetCoInsuredUseCase = TestGetCoInsuredUseCase()
    val testFetchCoInsuredPersonalInformationUseCase = TestFetchCoInsuredPersonalInformationUseCase()
    val testCreateMidTermChangeUseCase = TestCreateMidTermChangeUseCase()
    val testCommitMidtermChangeUseCase = TestCommitMidtermChangeUseCase()

    val presenter = EditCoInsuredPresenter(
      contractId = testContractId,
      getCoInsuredUseCase = testGetCoInsuredUseCase,
      fetchCoInsuredPersonalInformationUseCase = testFetchCoInsuredPersonalInformationUseCase,
      createMidtermChangeUseCase = testCreateMidTermChangeUseCase,
      commitMidtermChangeUseCase = testCommitMidtermChangeUseCase,
    )

    presenter.test(
      initialState = EditCoInsuredState.Loaded(
        listState = EditCoInsuredState.Loaded.CoInsuredListState(
          originalCoInsured = coInsuredTestList,
          member = testMember,
          allCoInsured = listOf(),
        ),
        addBottomSheetState = EditCoInsuredState.Loaded.AddBottomSheetState(
          showManualInput = false,
          infoFromSsn = InfoFromSsn(),
          manualInfo = ManualInfo(),
        ),
        removeBottomSheetState = EditCoInsuredState.Loaded.RemoveBottomSheetState(),
      ),
    ) {
      skipItems(1)
      sendEvent(EditCoInsuredEvent.OnEditCoInsuredClicked(coInsuredTestList[2]))
      skipItems(1)
      sendEvent(EditCoInsuredEvent.OnSsnChanged("5555"))
      skipItems(1)
      sendEvent(EditCoInsuredEvent.OnManualInputSwitchChanged(show = true))
      skipItems(1)
      sendEvent(EditCoInsuredEvent.OnLastNameChanged("New last name manual"))
      val state2 = awaitItem()
      assertThat(
        (state2 as EditCoInsuredState.Loaded).addBottomSheetState.manualInfo.lastName,
      ).isEqualTo("New last name manual")
      sendEvent(EditCoInsuredEvent.OnBottomSheetContinue)
      skipItems(1)
      testCreateMidTermChangeUseCase.addCreateMidtermChangeResult(
        "test",
        currentPremium = UiMoney(300.0, UiCurrencyCode.SEK),
        newPremium = UiMoney(400.0, UiCurrencyCode.SEK),
        activatedDate = LocalDate.fromEpochDays(400),
      )
      val item = awaitItem()
      assertThat(item).isInstanceOf<EditCoInsuredState.Loaded>()
      val uiState = item as EditCoInsuredState.Loaded
      assertThat(uiState.addBottomSheetState.manualInfo).isEqualTo(ManualInfo())
      assertThat(uiState.listState.coInsured[2].ssn).isEqualTo(null)
      assertThat(uiState.listState.coInsured[2].lastName).isEqualTo("New last name manual")
    }
  }
}
