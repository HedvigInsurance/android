package com.hedvig.app.feature.profile

import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.context
import com.hedvig.app.util.market
import org.javamoney.moneta.Money

val defaultAmount: String
    get() = Money.of(349, "SEK").format(context(), market())
