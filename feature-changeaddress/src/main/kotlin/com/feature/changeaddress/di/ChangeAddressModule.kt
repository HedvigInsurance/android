package com.feature.changeaddress.di

import com.apollographql.apollo3.ApolloClient
import com.feature.changeaddress.ChangeAddressViewModel
import com.feature.changeaddress.data.ChangeAddressRepository
import com.feature.changeaddress.data.NetworkChangeAddressRepository
import com.hedvig.android.apollo.octopus.di.octopusClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


@Suppress("RemoveExplicitTypeArguments")
val changeAddressModule = module {
  single<ChangeAddressRepository> { NetworkChangeAddressRepository(get<ApolloClient>(octopusClient)) }
  viewModel<ChangeAddressViewModel> { ChangeAddressViewModel(get<ChangeAddressRepository>()) }
}
