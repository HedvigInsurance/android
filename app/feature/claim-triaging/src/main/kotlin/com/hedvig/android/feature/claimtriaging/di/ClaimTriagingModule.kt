package com.hedvig.android.feature.claimtriaging.di

import com.hedvig.android.data.claimtriaging.ClaimGroupId
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.feature.claimtriaging.claimentrypointoptions.ClaimEntryPointOptionsViewModel
import com.hedvig.android.feature.claimtriaging.claimentrypoints.ClaimEntryPointsViewModel
import com.hedvig.android.feature.claimtriaging.claimgroups.ClaimGroupsViewModel
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val claimTriagingModule = module {
  viewModel<ClaimGroupsViewModel> { ClaimGroupsViewModel(get()) }
  viewModel<ClaimEntryPointsViewModel> { (claimGroupId: ClaimGroupId) ->
    ClaimEntryPointsViewModel(claimGroupId, get())
  }
  viewModel<ClaimEntryPointOptionsViewModel> { (entryPointOptions: ImmutableList<EntryPointOption>) ->
    ClaimEntryPointOptionsViewModel(entryPointOptions)
  }
}
