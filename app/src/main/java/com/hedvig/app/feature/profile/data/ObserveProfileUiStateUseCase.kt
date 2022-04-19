package com.hedvig.app.feature.profile.data

import arrow.core.Either
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.feature.profile.ui.tab.ProfileQueryDataToProfileUiStateMapper
import com.hedvig.app.feature.profile.ui.tab.ProfileUiState
import com.hedvig.app.util.apollo.QueryResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

class ObserveProfileUiStateUseCase(
    private val profileRepository: ProfileRepository,
    private val profileQueryDataToProfileUiStateMapper: ProfileQueryDataToProfileUiStateMapper,
) {
    suspend fun invoke(): Flow<Either<QueryResult.Error, ProfileUiState>> {
        return profileRepository
            .profile()
            .map(QueryResult<ProfileQuery.Data>::toEither)
            .mapLatest { either ->
                either.map { profileQueryDataToProfileUiStateMapper.map(it) }
            }
    }
}
