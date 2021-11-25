package com.hedvig.app.util

import java.util.regex.Pattern

val EMAIL_REGEX: Pattern = Pattern.compile("^.+@.+\\..+\$", Pattern.CASE_INSENSITIVE)
