package com.hedvig.android.feature.terminateinsurance.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class TerminationReasonMapperTest {
  @Test
  fun `moving options map correctly`() {
    assertThat(TerminationReasonMapper.toReason("MOVED_IN_WITH_SOMEONE")).isEqualTo("MOVED_IN_WITH_SOMEONE")
    assertThat(TerminationReasonMapper.toReason("MOVED_IN_WITH_SOMEONE_STUDENT")).isEqualTo("MOVED_IN_WITH_SOMEONE")
    assertThat(TerminationReasonMapper.toReason("MOVED_ABROAD")).isEqualTo("MOVED_ABROAD")
    assertThat(TerminationReasonMapper.toReason("MOVED_ABROAD_CHANGE_TIER")).isEqualTo("MOVED_ABROAD")
    assertThat(TerminationReasonMapper.toReason("MOVED_OTHER")).isEqualTo("HOME_NO_LONGER_NEED_OTHER")
    assertThat(TerminationReasonMapper.toReason("MOVING")).isEqualTo("MOVED")
    assertThat(TerminationReasonMapper.toReason("MOVING_NEW_ADDRESS")).isEqualTo("MOVED")
    assertThat(
      TerminationReasonMapper.toReason("MOVING_CANCEL_WITHOUT_NEW_QUOTE"),
    ).isEqualTo("MOVING_CANCEL_WITHOUT_NEW_QUOTE")
  }

  @Test
  fun `car options map correctly`() {
    assertThat(TerminationReasonMapper.toReason("CAR_SOLD")).isEqualTo("CAR_SOLD")
    assertThat(TerminationReasonMapper.toReason("CAR_SCRAPPED")).isEqualTo("CAR_SCRAPPED")
    assertThat(TerminationReasonMapper.toReason("CAR_DECOMMISSIONED")).isEqualTo("CAR_DECOMMISSIONED")
    assertThat(TerminationReasonMapper.toReason("CAR_OTHER")).isEqualTo("CAR_NO_LONGER_NEED_OTHER")
    assertThat(TerminationReasonMapper.toReason("CAR_RECOMMISSIONED")).isEqualTo("CAR_RECOMMISSIONED")
  }

  @Test
  fun `pet options map correctly`() {
    assertThat(TerminationReasonMapper.toReason("PET_NEW_OWNER")).isEqualTo("PET_HAS_NEW_OWNER")
    assertThat(TerminationReasonMapper.toReason("PET_NO_LONGER_LIVES")).isEqualTo("PET_DECEASED")
    assertThat(TerminationReasonMapper.toReason("PET_OTHER")).isEqualTo("PET_NO_LONGER_NEED_OTHER")
  }

  @Test
  fun `better price options map to GOT_BETTER_OFFER`() {
    assertThat(TerminationReasonMapper.toReason("BETTER_PRICE")).isEqualTo("GOT_BETTER_OFFER_FROM_OTHER_INSURER")
    assertThat(
      TerminationReasonMapper.toReason("BETTER_PRICE_CANCEL_WITHOUT_NEW_QUOTE"),
    ).isEqualTo("GOT_BETTER_OFFER_FROM_OTHER_INSURER")
    assertThat(TerminationReasonMapper.toReason("BETTER_OFFER")).isEqualTo("GOT_BETTER_OFFER_FROM_OTHER_INSURER")
  }

  @Test
  fun `coverage options map to DISSATISFIED_WITH_COVERAGE`() {
    assertThat(TerminationReasonMapper.toReason("MISSING_COVERAGE_OR_TERMS")).isEqualTo("DISSATISFIED_WITH_COVERAGE")
    assertThat(
      TerminationReasonMapper.toReason("MISSING_COVERAGE_OR_TERMS_CANCEL_WITHOUT_NEW_QUOTE"),
    ).isEqualTo("DISSATISFIED_WITH_COVERAGE")
    assertThat(
      TerminationReasonMapper.toReason("MISSING_COVERAGE_OR_TERMS_CHANGE_COVERAGE_LEVEL"),
    ).isEqualTo("DISSATISFIED_WITH_COVERAGE")
    assertThat(
      TerminationReasonMapper.toReason("BETTER_PRICE_CHANGE_COVERAGE_LEVEL"),
    ).isEqualTo("DISSATISFIED_WITH_COVERAGE")
    assertThat(TerminationReasonMapper.toReason("DISSATISFIED_COVERAGE")).isEqualTo("DISSATISFIED_WITH_COVERAGE")
  }

  @Test
  fun `other dissatisfied options map correctly`() {
    assertThat(TerminationReasonMapper.toReason("DISSATISFIED_SERVICE")).isEqualTo("DISSATISFIED_WITH_SERVICE")
    assertThat(TerminationReasonMapper.toReason("DISSATISFIED_APP")).isEqualTo("DISSATISFIED_WITH_APP")
    assertThat(TerminationReasonMapper.toReason("DISSATISFIED_PRICE")).isEqualTo("PRICE")
    assertThat(TerminationReasonMapper.toReason("DISSATISFIED_OTHER")).isEqualTo("DISSATISFIED_WITH_OTHER")
  }

  @Test
  fun `vacation and misc options map correctly`() {
    assertThat(TerminationReasonMapper.toReason("SOLD_VACATION_HOME")).isEqualTo("OBJECT_SOLD")
    assertThat(TerminationReasonMapper.toReason("OTHER_REASON")).isEqualTo("OTHER")
    assertThat(TerminationReasonMapper.toReason("ACCIDENT_OTHER")).isEqualTo("ACCIDENT_NO_LONGER_NEED_OTHER")
    assertThat(TerminationReasonMapper.toReason("NO_LONGER_NEEDED")).isEqualTo("DONT_NEED_INSURANCE")
    assertThat(TerminationReasonMapper.toReason("NO_LONGER_NEEDED_OTHER")).isEqualTo("DONT_NEED_INSURANCE")
  }
}
