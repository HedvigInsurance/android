package com.hedvig.android.feature.home.yearinreview.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.PillowType
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal interface GetYearInReviewUseCase {
  suspend fun invoke(): Either<ErrorMessage, YearInReview>
}

/**
 * The 2024 highlights published on hedvig.com, the same content the iOS app shows, with how long the
 * member has been with Hedvig right after the opening one. The membership story is left out when the member
 * has no started insurance or it cannot be fetched, so the highlights still show.
 *
 * TODO: Fetch the highlights from the backend once the API exists.
 */
@ContributesBinding(AppScope::class)
@Inject
internal class GetYearInReviewUseCaseImpl(
  private val getMembershipStartDateUseCase: GetMembershipStartDateUseCase,
  private val clock: Clock,
  private val timeZone: TimeZone,
) : GetYearInReviewUseCase {
  override suspend fun invoke(): Either<ErrorMessage, YearInReview> {
    val membershipStory = getMembershipStartDateUseCase.invoke().getOrNull()?.let { start ->
      val today = clock.now().toLocalDateTime(timeZone).date
      membershipStory(membershipDuration(start, today))
    }
    return YearInReview(stories2024.take(1) + listOfNotNull(membershipStory) + stories2024.drop(1)).right()
  }
}

private fun membershipStory(duration: MembershipDuration): YearInReviewStory {
  val durationText = duration.toSwedishText()
  return YearInReviewStory(
    id = "membership",
    // TODO: Add "You have been with us" / "Du har varit med oss" to Lokalise
    title = " Du har varit med oss",
    // TODO: Add "You have been a member with us for {duration}." / "Du har varit medlem hos oss i {duration}."
    //  and "You became a member today." / "Du blev medlem hos oss idag." to Lokalise
    leadText = if (durationText == null) {
      "Du blev medlem hos oss idag."
    } else {
      "Du har varit medlem hos oss i $durationText."
    },
    // TODO: Add "Thank you!" / "Tack!" to Lokalise
    text = "Tack!",
    imageUrl = "https://a.storyblok.com/f/165473/1200x1200/1b3ca72d1a/student-dator-1200.jpg",
    backgroundPillow = PillowType.HOME,
  )
}

// TODO: Comes from the backend once the API exists; until then this content is not in Lokalise.
private val stories2024 = listOf(
  YearInReviewStory(
    id = "first",
    title = "Höjdpunkter från året som gått – Hedvig 2024",
    leadText = "Året börjar lida mot sitt slut.",
    text = "För att runda av 2024 har vi på Hedvig sammanfattat några höjdpunkter och kuriosa från året som gått.",
    imageUrl = "https://assets.hedvig.com/f/165473/1326x884/a6e90a2901/logo-hedvig-hojdpunkter-2024.png",
    showImageOnCard = true,
    backgroundPillow = PillowType.HOMEOWNER,
  ),
  YearInReviewStory(
    id = "second",
    title = "Här växte vi mest",
    leadText = "Hedvig växer i bland annat Ånge,",
    text = "men flest antal nya medlemmar har vi fått i Stockholm.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/4a1d0a6e6d/animation-stader.gif",
    backgroundPillow = PillowType.RENTAL,
  ),
  YearInReviewStory(
    id = "third",
    title = "Antal hanterade skador",
    leadText = "Vi hjälpte till med 29 125 skador under 2024.",
    text = "Hela 2 000 fler än förra året.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/ceecba5143/animation-antal-skador.gif",
    backgroundPillow = PillowType.VACATION,
  ),
  YearInReviewStory(
    id = "fourth",
    title = "Flest försäkringar på en person",
    leadText = "En medlem har skaffat försäkring för sina 8 katter,",
    text = "vilket hittills är flest försäkringar tecknade av en person hos Hedvig.",
    imageUrl = "https://assets.hedvig.com/f/165473/1500x1500/acdaefca75/katt-selfie-hedvig-1500.jpg",
    backgroundPillow = PillowType.CAT,
  ),
  YearInReviewStory(
    id = "fifth",
    title = "Årets otursdagar",
    leadText = "De datum flest angett som skadetillfälle är 1 juli, 1 mars och 1 januari.",
    text = "Är den 1:a den nya 13:e?",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/d2530910de/animation-otursdagar.gif",
    backgroundPillow = PillowType.STUDENT,
  ),
  YearInReviewStory(
    id = "sixth",
    title = "Antal försäkrade husdjur",
    leadText = "Nu försäkrar vi 21 024 hundar och katter över hela landet.",
    text = "Det är nästan en fördubbling mot förra året.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/5c0e698822/animation-antal-husdjur.gif",
    backgroundPillow = PillowType.DOG,
  ),
  YearInReviewStory(
    id = "seventh",
    title = "Tredje bästa",
    leadText = "Vi har blivit utsedda till Sveriges tredje mest välansedda försäkringsbolag",
    text = "av Trust & Reputation.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/75123e2961/tre-stjarna.png",
    backgroundPillow = PillowType.ACCIDENT,
  ),
  YearInReviewStory(
    id = "eighth",
    title = "Årets snabbaste claim",
    leadText = "Vår snabbaste skadeanmälan i år var på 123 sekunder",
    text = "från anmäld skada till utbetald ersättning.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/050f68dbf0/animation-tidtagning.gif",
    backgroundPillow = PillowType.VILLA,
  ),
  YearInReviewStory(
    id = "ninth",
    title = "Årets mest försäkrade bil",
    leadText = "Den bilmodell som vi har försäkrat allra mest under året är Volkswagen Golf.",
    text = "En klassiker.",
    imageUrl = "https://a.storyblok.com/f/165473/1200x1200/1dda1c7b23/bil-skog.jpg",
    backgroundPillow = PillowType.CAR,
  ),
  YearInReviewStory(
    id = "tenth",
    title = "Djur-vabb",
    leadText = "Under året har 71 procent fler valt Djurförsäkring Premium,",
    text = "där ersättning för djur-vabb ingår.",
    imageUrl = "https://a.storyblok.com/f/165473/1400x1120/67c20dc5ca/hundar-djur-vabb.jpg",
    backgroundPillow = PillowType.DOG,
  ),
)
