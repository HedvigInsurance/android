package com.hedvig.android.feature.terminateinsurance

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.terminateinsurance.data.GetMemberEmailUseCase

internal class FakeGetMemberEmailUseCase(
  private val result: Either<ErrorMessage, String> = "member@example.com".right(),
) : GetMemberEmailUseCase {
  override suspend fun invoke(): Either<ErrorMessage, String> = result
}
