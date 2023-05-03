package com.example.feature.changeaddress.di

import com.apollographql.apollo3.ApolloClient
import com.example.feature.changeaddress.data.ChangeAddressRepository
import com.example.feature.changeaddress.data.NetworkChangeAddressRepository
import com.hedvig.android.apollo.octopus.di.octopusClient
import org.koin.dsl.module


@Suppress("RemoveExplicitTypeArguments")
val changeAddressModule = module {
  single<ChangeAddressRepository> { NetworkChangeAddressRepository(get<ApolloClient>(octopusClient)) }
}
