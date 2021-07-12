package com.hedvig.app.util

import javax.money.MonetaryAmount

operator fun MonetaryAmount.minus(other: MonetaryAmount): MonetaryAmount = subtract(other)
