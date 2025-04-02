package com.hedvig.android.data.cross.sell.after.flow.di

import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepositoryImpl
import org.koin.dsl.module

val dataCrossSellAfterFlowModule = module {
  single<CrossSellAfterFlowRepository> {
    CrossSellAfterFlowRepositoryImpl()
  }
}
