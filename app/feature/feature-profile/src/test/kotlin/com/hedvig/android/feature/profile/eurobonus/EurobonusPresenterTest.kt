package com.hedvig.android.feature.profile.eurobonus

import com.hedvig.android.feature.profile.myinfo.FakeProfileRepository
import com.hedvig.android.feature.profile.myinfo.MyInfoPresenter
import com.hedvig.android.feature.profile.myinfo.MyInfoUiState
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Test

class EurobonusPresenterTes {
  @Test
  fun `if phone and email are updated they are received in state`() = runTest {
    val profileRepository = FakeProfileRepository()
    val presenter = MyInfoPresenter { profileRepository }

    presenter.test(MyInfoUiState.Loading) {
    }
  }
}
