package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.right
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable

private const val HARDCODE_RESPONSE = true

interface GetPuppyGuideUseCase {
  fun invoke(): Flow<Either<ErrorMessage, List<PuppyGuideStory>?>>
}

internal class GetPuppyGuideUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetPuppyGuideUseCase {
  override fun invoke(): Flow<Either<ErrorMessage, List<PuppyGuideStory>?>> {
    if (HARDCODE_RESPONSE) {
      return flowOf(
        List(5) {
          PuppyGuideStory(
            categories = List(3) { "category#$it" },
            content = "content",
            image = "image",
            name = "name",
            rating = 4,
            isRead = false,
            subtitle = "subtitle",
            title = "title",
          )
        }.right(),
      )
    } else {
      error("Not implemented yet")
//      return apolloClient
//        .query(PuppyGuideQuery())
//        .fetchPolicy(FetchPolicy.CacheAndNetwork)
//        .safeFlow(::ErrorMessage)
//        .map { either ->
//          either
//            .onLeft { logcat { "Cannot load PuppyGuideStory: $it" } }
//            .map { data ->
//              data.currentMember.puppyGuideStories.map { story ->
//                PuppyGuideStory(
//                  categories = story.categories,
//                  content = story.content,
//                  image = story.image,
//                  name = story.name,
//                  rating = story.rating,
//                  isRead = story.read,
//                  subtitle = story.subtitle,
//                  title = story.title,
//                )
//              }
//            }
//        }
    }
  }
}

@Serializable
data class PuppyGuideStory(
  val categories: List<String>,
  val content: String,
  val image: String,
  val name: String,
  val rating: Int?,
  val isRead: Boolean,
  val subtitle: String,
  val title: String,
)
