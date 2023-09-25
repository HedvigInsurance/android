package com.hedvig.android.feature.changeaddress.di

 import com.apollographql.apollo3.ApolloClient
 import com.hedvig.android.apollo.octopus.di.octopusClient
 import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
 import com.hedvig.android.feature.changeaddress.data.ChangeAddressRepository
 import com.hedvig.android.feature.changeaddress.data.NetworkChangeAddressRepository
 import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val changeAddressModule = module {
  single<ChangeAddressRepository> { NetworkChangeAddressRepository(get<ApolloClient>(octopusClient)) }
  viewModel<ChangeAddressViewModel> { ChangeAddressViewModel(get<ChangeAddressRepository>()) }
}
