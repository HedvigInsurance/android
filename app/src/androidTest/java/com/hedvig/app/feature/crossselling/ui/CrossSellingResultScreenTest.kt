package com.hedvig.app.feature.crossselling.ui

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CrossSellingResultScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val baseClockTime = Clock.fixed(
        Instant.parse("2021-09-14T12:00:00.00Z"),
        ZoneId.of("Europe/Stockholm")
    )
    private val baseLocalDate = LocalDate.now(baseClockTime)
    private val insuranceType = "Accident Insurance"
    private val successfulResultToday = CrossSellingResult.Success(baseLocalDate, insuranceType)

    private enum class TextAlternative(val text: String) {
        Failed("couldn't be completed"),
        AlreadyActivated("It’s already activated"),
        WillActivate("It’ll activate on"),
    }

    private fun SemanticsNodeInteractionsProvider.onNodeWithText(
        textAlternative: TextAlternative,
        useUnmergedTree: Boolean = false,
    ): SemanticsNodeInteraction = onNode(hasText(textAlternative.text, true, true), useUnmergedTree)

    @Test
    fun contractActivatedYesterday() {
        val contractYesterday = successfulResultToday.copy(startingDate = baseLocalDate.minusDays(1))

        compose.setContent {
            CrossSellingResultScreen(contractYesterday, baseClockTime)
        }

        compose.onNodeWithText(TextAlternative.AlreadyActivated).assertExists()
        compose.onNodeWithText(TextAlternative.WillActivate).assertDoesNotExist()
        compose.onNodeWithText(TextAlternative.Failed).assertDoesNotExist()
    }

    @Test
    fun contractActivatingTomorrow() {
        val contractTomorrow = successfulResultToday.copy(startingDate = baseLocalDate.plusDays(1))

        compose.setContent {
            CrossSellingResultScreen(contractTomorrow, baseClockTime)
        }

        compose
            .onNodeWithText(TextAlternative.AlreadyActivated)
            .assertDoesNotExist()
        compose
            .onNodeWithText(TextAlternative.WillActivate)
            .assertExists()
            .assertTextContains("2021-09-15", substring = true)
        compose.onNodeWithText(TextAlternative.Failed).assertDoesNotExist()
    }

    @Test
    fun failedContract() {
        val failedResult = CrossSellingResult.Error

        compose.setContent {
            CrossSellingResultScreen(failedResult, baseClockTime)
        }

        compose.onNodeWithText(TextAlternative.AlreadyActivated).assertDoesNotExist()
        compose.onNodeWithText(TextAlternative.WillActivate).assertDoesNotExist()
        compose.onNodeWithText(TextAlternative.Failed).assertExists()
    }

    @Test
    fun contractNextMonth() {
        val contractNextMonth = CrossSellingResult.Success(LocalDate.of(2021, 10, 12), insuranceType)

        compose.setContent {
            CrossSellingResultScreen(contractNextMonth, baseClockTime)
        }

        compose
            .onNodeWithText(TextAlternative.WillActivate)
            .assertExists()
            .assertTextContains("2021-10-12", substring = true)
    }

    @Test
    fun contractNextYear() {
        val contractNextYear = CrossSellingResult.Success(LocalDate.of(2022, 1, 1), insuranceType)

        compose.setContent {
            CrossSellingResultScreen(contractNextYear, baseClockTime)
        }

        compose
            .onNodeWithText(TextAlternative.WillActivate)
            .assertExists()
            .assertTextContains("2022-01-01", substring = true)
    }

    @Test
    fun contractPreviousYear() {
        val contractPreviousYear = CrossSellingResult.Success(LocalDate.of(2020, 1, 1), insuranceType)

        compose.setContent {
            CrossSellingResultScreen(contractPreviousYear, baseClockTime)
        }

        compose.onNodeWithText(TextAlternative.AlreadyActivated).assertExists()
    }
}
