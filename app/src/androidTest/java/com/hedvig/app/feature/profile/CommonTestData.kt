package com.hedvig.app.feature.profile

import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.market
import com.hedvig.testutil.context
import org.javamoney.moneta.Money

val defaultAmount = Money.of(349, "SEK").format(context(), market())
