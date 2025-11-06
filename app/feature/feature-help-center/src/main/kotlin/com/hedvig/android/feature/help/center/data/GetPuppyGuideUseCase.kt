package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import kotlinx.serialization.Serializable
import octopus.PuppyGuideQuery

internal interface GetPuppyGuideUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<PuppyGuideStory>?>
}

internal class GetPuppyGuideUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetPuppyGuideUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<PuppyGuideStory>?> {
    return either {
      apolloClient
        .query(PuppyGuideQuery())
        .safeExecute()
        .onLeft { logcat { "Cannot load PuppyGuideStory: $it" } }
        .getOrNull()
        ?.currentMember
        ?.puppyGuideStories
        ?.map { story ->
          PuppyGuideStory(
            categories = story.categories,
            content = story.content,
            image = story.image,
            name = story.name,
            rating = story.rating,
            isRead = story.read,
            subtitle = story.subtitle,
            title = story.title,
          )
        }
    }
  }
}

@Serializable
internal data class PuppyGuideStory(
  val categories: List<String>,
  val content: String,
  val image: String,
  val name: String,
  val rating: Int?,
  val isRead: Boolean,
  val subtitle: String,
  val title: String,
)
