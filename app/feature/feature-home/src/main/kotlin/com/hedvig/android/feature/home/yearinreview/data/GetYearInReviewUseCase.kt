package com.hedvig.android.feature.home.yearinreview.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

internal interface GetYearInReviewUseCase {
  suspend fun invoke(): Either<ErrorMessage, YearInReview>
}

/**
 * Serves the 2024 highlights published on hedvig.com, the same content the iOS app shows.
 *
 * TODO: Replace with a use case that fetches the member's year in review from the backend once the API exists.
 */
@ContributesBinding(AppScope::class)
@Inject
internal class FakeGetYearInReviewUseCase : GetYearInReviewUseCase {
  override suspend fun invoke(): Either<ErrorMessage, YearInReview> = YearInReview(stories2024).right()
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
  ),
  YearInReviewStory(
    id = "second",
    title = "Här växte vi mest",
    leadText = "Hedvig växer i bland annat Ånge,",
    text = "men flest antal nya medlemmar har vi fått i Stockholm.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/4a1d0a6e6d/animation-stader.gif",
  ),
  YearInReviewStory(
    id = "third",
    title = "Antal hanterade skador",
    leadText = "Vi hjälpte till med 29 125 skador under 2024.",
    text = "Hela 2 000 fler än förra året.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/ceecba5143/animation-antal-skador.gif",
  ),
  YearInReviewStory(
    id = "fourth",
    title = "Flest försäkringar på en person",
    leadText = "En medlem har skaffat försäkring för sina 8 katter,",
    text = "vilket hittills är flest försäkringar tecknade av en person hos Hedvig.",
    imageUrl = "https://assets.hedvig.com/f/165473/1500x1500/acdaefca75/katt-selfie-hedvig-1500.jpg",
  ),
  YearInReviewStory(
    id = "fifth",
    title = "Årets otursdagar",
    leadText = "De datum flest angett som skadetillfälle är 1 juli, 1 mars och 1 januari.",
    text = "Är den 1:a den nya 13:e?",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/d2530910de/animation-otursdagar.gif",
  ),
  YearInReviewStory(
    id = "sixth",
    title = "Antal försäkrade husdjur",
    leadText = "Nu försäkrar vi 21 024 hundar och katter över hela landet.",
    text = "Det är nästan en fördubbling mot förra året.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/5c0e698822/animation-antal-husdjur.gif",
  ),
  YearInReviewStory(
    id = "seventh",
    title = "Tredje bästa",
    leadText = "Vi har blivit utsedda till Sveriges tredje mest välansedda försäkringsbolag",
    text = "av Trust & Reputation.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/75123e2961/tre-stjarna.png",
  ),
  YearInReviewStory(
    id = "eighth",
    title = "Årets snabbaste claim",
    leadText = "Vår snabbaste skadeanmälan i år var på 123 sekunder",
    text = "från anmäld skada till utbetald ersättning.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/050f68dbf0/animation-tidtagning.gif",
  ),
  YearInReviewStory(
    id = "ninth",
    title = "Årets mest försäkrade bil",
    leadText = "Den bilmodell som vi har försäkrat allra mest under året är Volkswagen Golf.",
    text = "En klassiker.",
    imageUrl = "https://assets.hedvig.com/f/165473/1920x1080/99e0d1b868/summercar-bottom.jpg",
  ),
  YearInReviewStory(
    id = "tenth",
    title = "Djur-vabb",
    leadText = "Under året har 71 procent fler valt Djurförsäkring Premium,",
    text = "där ersättning för djur-vabb ingår.",
    imageUrl = "https://assets.hedvig.com/f/165473/1080x1080/be939841d2/hund-springer.png",
  ),
)
