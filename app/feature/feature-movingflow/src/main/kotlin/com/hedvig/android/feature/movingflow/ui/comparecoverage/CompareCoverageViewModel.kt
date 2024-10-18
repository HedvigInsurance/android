package com.hedvig.android.feature.movingflow.ui.comparecoverage

import androidx.compose.runtime.Composable
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class CompareCoverageViewModel : MoleculeViewModel<Unit, Unit>(
  Unit,
  object : MoleculePresenter<Unit, Unit> {
    @Composable
    override fun MoleculePresenterScope<Unit>.present(lastState: Unit) {
      Unit
    }
  },
)
