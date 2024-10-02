package com.hedvig.android.data.changetier.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.di.ioDispatcherQualifier
import com.hedvig.android.core.common.di.tierQuotesDatabaseFileQualifier
import com.hedvig.android.data.changetier.data.ChangeTierRepository
import com.hedvig.android.data.changetier.data.ChangeTierRepositoryImpl
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCase
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCaseImpl
import com.hedvig.android.data.changetier.database.TierQuoteDao
import com.hedvig.android.data.changetier.database.TierQuoteMapper
import com.hedvig.android.data.changetier.database.TierQuotesDatabase
import com.hedvig.android.featureflags.FeatureManager
import java.io.File
import kotlin.coroutines.CoroutineContext
import org.koin.dsl.module

val dataChangeTierModule = module {
  single<CreateChangeTierDeductibleIntentUseCase> {
    CreateChangeTierDeductibleIntentUseCaseImpl(
      get<ApolloClient>(),
      get<FeatureManager>(),
    )
  }
  single<ChangeTierRepository> {
    ChangeTierRepositoryImpl(
      createChangeTierDeductibleIntentUseCase = get<CreateChangeTierDeductibleIntentUseCase>(),
      tierQuoteDao = get<TierQuoteDao>(),
      mapper = get<TierQuoteMapper>(),
    )
  }
  single<TierQuoteDao> {
    get<TierQuotesDatabase>().tierQuoteDao()
  }
  single<TierQuoteMapper> {
    TierQuoteMapper()
  }
  single<RoomDatabase.Builder<TierQuotesDatabase>> {
    val dbFile = get<File>(tierQuotesDatabaseFileQualifier)
    val applicationContext = get<Context>()
    Room
      .databaseBuilder<TierQuotesDatabase>(applicationContext, dbFile.absolutePath)
      .setQueryCoroutineContext(get<CoroutineContext>(ioDispatcherQualifier))
  }
  single<TierQuotesDatabase> {
    get<RoomDatabase.Builder<TierQuotesDatabase>>().build()
  }
}
