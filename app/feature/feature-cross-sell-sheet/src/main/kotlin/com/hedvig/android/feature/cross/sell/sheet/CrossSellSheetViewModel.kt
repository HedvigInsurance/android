package com.hedvig.android.feature.cross.sell.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.data.addons.data.GetTravelAddonBannerInfoUseCase
import com.hedvig.android.data.addons.data.TravelAddonBannerSource
import com.hedvig.android.data.contract.android.CrossSell
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.transformLatest
import octopus.CrossSellsQuery
import octopus.type.CrossSellType

internal class CrossSellSheetViewModel(
  private val getCrossSellSheetDataUseCaseProvider: Provider<GetCrossSellSheetDataUseCase>,
  private val crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) : MoleculeViewModel<Unit, CrossSellSheetState>(
    CrossSellSheetState.Loading,
    CrossSellSheetPresenter(getCrossSellSheetDataUseCaseProvider, crossSellAfterFlowRepository),
  )

sealed interface CrossSellSheetState {
  data object Loading : CrossSellSheetState

  data object DontShow : CrossSellSheetState

  data class Error(val errorMessage: ErrorMessage) : CrossSellSheetState

  data class Content(val crossSellSheetData: CrossSellSheetData) : CrossSellSheetState
}

private class CrossSellSheetPresenter(
  private val getCrossSellSheetDataUseCaseProvider: Provider<GetCrossSellSheetDataUseCase>,
  private val crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
) : MoleculePresenter<Unit, CrossSellSheetState> {
  @Composable
  override fun MoleculePresenterScope<Unit>.present(lastState: CrossSellSheetState): CrossSellSheetState {
    var state by remember { mutableStateOf(lastState) }
    LaunchedEffect(Unit) {
      crossSellAfterFlowRepository.shouldShowCrossSellSheet().transformLatest { shouldShow ->
        if (!shouldShow) {
          emit(CrossSellSheetState.DontShow)
          return@transformLatest
        }
        emitAll(
          getCrossSellSheetDataUseCaseProvider.provide().invoke().mapLatest { result ->
            result.fold(
              ifLeft = { error -> CrossSellSheetState.Error(error) },
              ifRight = { data -> CrossSellSheetState.Content(data) },
            )
          },
        )
      }.collectLatest {
        state = it
      }
    }
    return state
  }
}

internal class GetCrossSellSheetDataUseCaseProvider(
  override val demoManager: DemoManager,
  override val demoImpl: GetCrossSellSheetDataUseCase,
  override val prodImpl: GetCrossSellSheetDataUseCase,
) : ProdOrDemoProvider<GetCrossSellSheetDataUseCase>

internal interface GetCrossSellSheetDataUseCase {
  suspend fun invoke(): Flow<Either<ErrorMessage, CrossSellSheetData>>
}

internal class GetCrossSellSheetDataUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val getTravelAddonBannerInfoUseCase: GetTravelAddonBannerInfoUseCase,
) : GetCrossSellSheetDataUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, CrossSellSheetData>> {
    return combine(
      apolloClient
        .query(CrossSellsQuery())
        .safeFlow(::ErrorMessage)
        .map { response ->
          response.map { data ->
            data.currentMember.crossSells.map { crossSell ->
              CrossSell(
                id = crossSell.id,
                title = crossSell.title,
                subtitle = crossSell.description,
                storeUrl = crossSell.storeUrl,
                type = when (crossSell.type) {
                  CrossSellType.CAR -> CrossSell.CrossSellType.CAR
                  CrossSellType.HOME -> CrossSell.CrossSellType.HOME
                  CrossSellType.ACCIDENT -> CrossSell.CrossSellType.ACCIDENT
                  CrossSellType.PET -> CrossSell.CrossSellType.PET
                  CrossSellType.UNKNOWN__ -> CrossSell.CrossSellType.UNKNOWN
                },
              )
            }
          }
        },
      getTravelAddonBannerInfoUseCase.invoke(TravelAddonBannerSource.INSURANCES_TAB),
    ) { crossSells, travelAddonBannerInfo ->
      either {
        CrossSellSheetData(crossSells.bind(), travelAddonBannerInfo.bind())
      }
    }
  }
}

internal class DemoGetCrossSellSheetDataUseCase() : GetCrossSellSheetDataUseCase {
  override suspend fun invoke(): Flow<Either<ErrorMessage, CrossSellSheetData>> {
    return flowOf(ErrorMessage("Ineligible for demo mode").left())
  }
}
