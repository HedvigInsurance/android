package com.hedvig.android.data.conversations.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.conversations.HasAnyActiveConversationUseCase
import org.koin.dsl.module

val dataConversationsModule = module {
  single<HasAnyActiveConversationUseCase> { HasAnyActiveConversationUseCase(get<ApolloClient>()) }
}
