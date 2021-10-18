package com.hedvig.app.util.compose

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.hedvig.app.ui.compose.theme.LocalMarket
import com.hedvig.app.util.apollo.format
import javax.money.MonetaryAmount

sealed class DisplayableText private constructor() {
    @Composable
    abstract fun text(): String

    private class FromStringResource(
        @StringRes private val stringRes: Int,
    ) : DisplayableText() {
        @Composable
        override fun text(): String {
            return stringResource(stringRes)
        }
    }

    private class FromMonetaryAmount(
        private val monetaryAmount: MonetaryAmount,
    ) : DisplayableText() {
        @Composable
        override fun text(): String {
            return monetaryAmount.format(LocalContext.current, LocalMarket.current, 0)
        }
    }

    private class FromString(
        private val string: String
    ) : DisplayableText() {
        @Composable
        override fun text(): String {
            return string
        }
    }

    companion object {
        operator fun invoke(text: String): DisplayableText = FromString(text)
        operator fun invoke(@StringRes stringRes: Int): DisplayableText = FromStringResource(stringRes)
        operator fun invoke(monetaryAmount: MonetaryAmount): DisplayableText = FromMonetaryAmount(monetaryAmount)
    }
}
