package com.hedvig.app.feature.crossselling.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.hedvig.app.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

class CrossSellingResultScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val baseClockTime = Clock.fixed(
        Instant.parse("2021-09-14T12:00:00.00Z"),
        ZoneId.of("Europe/Stockholm")
    )
    private val baseLocalDate = LocalDate.now(baseClockTime)
    private val accidentInsurance = "Accident Insurance"
    private val successfulResultToday = CrossSellingResult.Success(baseLocalDate, accidentInsurance)

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun contractActivatedYesterday() {
        val contractYesterday = successfulResultToday.copy(startingDate = baseLocalDate.minusDays(1))

        compose.setContent {
            CrossSellingResultScreen(contractYesterday, baseClockTime, isoDateFormatter, {}, {})
        }

        compose.onNodeWithText(TextAlternative.AlreadyActivated.getString(context)).assertExists()
        compose.onNodeWithText(TextAlternative.WillActivate.getString(context, contractYesterday.startingDate))
            .assertDoesNotExist()
        compose.onNodeWithText(TextAlternative.Failed.getString(context)).assertDoesNotExist()
    }

    @Test
    fun contractActivatingTomorrow() {
        val contractTomorrow = successfulResultToday.copy(startingDate = baseLocalDate.plusDays(1))

        compose.setContent {
            CrossSellingResultScreen(contractTomorrow, baseClockTime, isoDateFormatter, {}, {})
        }

        compose
            .onNodeWithText(TextAlternative.AlreadyActivated.getString(context))
            .assertDoesNotExist()
        compose
            .onNodeWithText(TextAlternative.WillActivate.getString(context, contractTomorrow.startingDate))
            .assertExists()
            .assertTextContains("2021-09-15", substring = true)
        compose.onNodeWithText(TextAlternative.Failed.getString(context)).assertDoesNotExist()
    }

    @Test
    fun failedContract() {
        val failedResult = CrossSellingResult.Error

        compose.setContent {
            CrossSellingResultScreen(failedResult, baseClockTime, isoDateFormatter, {}, {})
        }

        compose.onNodeWithText(TextAlternative.AlreadyActivated.getString(context)).assertDoesNotExist()
        compose.onNodeWithText(TextAlternative.Failed.getString(context)).assertExists()
    }

    @Test
    fun contractToday() {
        val successfulResultToday = successfulResultToday

        compose.setContent {
            CrossSellingResultScreen(successfulResultToday, baseClockTime, isoDateFormatter, {}, {})
        }

        compose.onNodeWithText(TextAlternative.AlreadyActivated.getString(context)).assertExists()
    }

    @Test
    fun contractNextMonth() {
        val contractNextMonth = CrossSellingResult.Success(LocalDate.of(2021, 10, 12), accidentInsurance)

        compose.setContent {
            CrossSellingResultScreen(contractNextMonth, baseClockTime, isoDateFormatter, {}, {})
        }

        compose
            .onNodeWithText(TextAlternative.WillActivate.getString(context, contractNextMonth.startingDate))
            .assertExists()
    }

    @Test
    fun contractNextYear() {
        val contractNextYear = CrossSellingResult.Success(LocalDate.of(2022, 1, 1), accidentInsurance)

        compose.setContent {
            CrossSellingResultScreen(contractNextYear, baseClockTime, isoDateFormatter, {}, {})
        }

        compose
            .onNodeWithText(TextAlternative.WillActivate.getString(context, contractNextYear.startingDate))
            .assertExists()
    }

    @Test
    fun contractPreviousYear() {
        val contractPreviousYear = CrossSellingResult.Success(LocalDate.of(2020, 1, 1), accidentInsurance)

        compose.setContent {
            CrossSellingResultScreen(contractPreviousYear, baseClockTime, isoDateFormatter, {}, {})
        }

        compose.onNodeWithText(TextAlternative.AlreadyActivated.getString(context)).assertExists()
    }
}

private sealed class TextAlternative(@StringRes protected val stringRes: Int) {

    object Failed : TextAlternative(R.string.purchase_confirmation_error_subtitle) {
        fun getString(context: Context): String = context.getString(stringRes)
    }

    object AlreadyActivated : TextAlternative(
        R.string.purchase_confirmation_new_insurance_today_app_state_description
    ) {
        fun getString(context: Context): String = context.getString(stringRes)
    }

    object WillActivate : TextAlternative(
        R.string.purchase_confirmation_new_insurance_active_in_future_app_state_description
    ) {
        fun getString(
            context: Context,
            activationDate: LocalDate
        ): String = context.getString(stringRes, activationDate.format(isoDateFormatter))
    }
}
