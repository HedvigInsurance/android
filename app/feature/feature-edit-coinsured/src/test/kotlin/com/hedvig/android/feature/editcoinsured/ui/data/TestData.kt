package com.hedvig.android.feature.editcoinsured.ui.data

import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member

internal val testContractId = "123"

internal val coInsuredTestList = listOf(
  CoInsured(
    internalId = "1",
    firstName = "Test2",
    lastName = "Testersson2",
    birthDate = null,
    ssn = "199304111344",
    hasMissingInfo = false,
  ),
  CoInsured(
    internalId = "2",
    firstName = "Test3",
    lastName = "Testersson3",
    birthDate = null,
    ssn = "187405053912",
    hasMissingInfo = false,
  ),
  CoInsured(
    internalId = "3",
    firstName = "Test4",
    lastName = "Testersson4",
    birthDate = null,
    ssn = "173304113940",
    hasMissingInfo = false,
  ),
)

internal val testMember = Member(
  firstName = "Member",
  lastName = "Membersson",
  ssn = "12345",
)
