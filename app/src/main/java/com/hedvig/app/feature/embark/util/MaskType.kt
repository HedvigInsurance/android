package com.hedvig.app.feature.embark.util

import com.hedvig.app.feature.embark.masking.ISO_8601_DATE
import com.hedvig.app.feature.embark.masking.REVERSE_DATE
import com.hedvig.app.util.ANY_REGEX
import com.hedvig.app.util.BIRTH_DATE_REGEX
import com.hedvig.app.util.BIRTH_DATE_REVERSE_REGEX
import com.hedvig.app.util.DANISH_PERSONAL_NUMBER_REGEX
import com.hedvig.app.util.EMAIL_REGEX
import com.hedvig.app.util.NORWEGIAN_PERSONAL_NUMBER_REGEX
import com.hedvig.app.util.NORWEGIAN_POSTAL_CODE_REGEX
import com.hedvig.app.util.SWEDISH_PERSONAL_NUMBER_REGEX
import com.hedvig.app.util.SWEDISH_POSTAL_CODE_REGEX
import java.time.LocalDate

// TODO Move masking/unmasking to view, and use androidx.compose.ui.text.input.VisualTransformation, see SwedishSSNVisualTransformation.kt
enum class MaskType {
    PERSONAL_NUMBER {
        override fun mask(text: String) = StringBuilder(text).apply { insert(6, "-") }.toString()
        override fun unMask(text: String) = text.replace("-", "")
        override fun isValid(text: String): Boolean = SWEDISH_PERSONAL_NUMBER_REGEX.matcher(text).find()
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>> {
            val birthDate = text.substring(0..5)
            val years = MaskTypeUtil.getYearsBetween(birthDate, currentDate, "MMdd")
            return listOf("$key.Age" to years)
        }
    },
    NORWEGIAN_PERSONAL_NUMBER {
        override fun mask(text: String) = StringBuilder(text).apply { insert(6, "-") }.toString()
        override fun unMask(text: String) = text
        override fun isValid(text: String): Boolean = NORWEGIAN_PERSONAL_NUMBER_REGEX.matcher(text).find()
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>> {
            val years = MaskTypeUtil.getYearsBetween(text, currentDate, "MMdd")
            return listOf("$key.Age" to years)
        }
    },
    DANISH_PERSONAL_NUMBER {
        override fun mask(text: String) = StringBuilder(text).apply { insert(6, "-") }.toString()
        override fun unMask(text: String) = text.replace("-", "")
        override fun isValid(text: String): Boolean = DANISH_PERSONAL_NUMBER_REGEX.matcher(text).find()
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>> {
            val years = MaskTypeUtil.getYearsBetween(text, currentDate, "MMdd")
            return listOf("$key.Age" to years)
        }
    },
    POSTAL_CODE {
        override fun mask(text: String) = StringBuilder(text).apply { insert(2, " ") }.toString()
        override fun unMask(text: String) = text.replace(Regex("\\s"), "")
        override fun isValid(text: String): Boolean = SWEDISH_POSTAL_CODE_REGEX.matcher(text).find()
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>>? =
            null
    },
    EMAIL {
        override fun mask(text: String) = text
        override fun unMask(text: String) = text
        override fun isValid(text: String): Boolean = EMAIL_REGEX.matcher(text).find()
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>>? =
            null
    },
    BIRTH_DATE {
        override fun mask(text: String) = text
        override fun unMask(text: String) = text
        override fun isValid(text: String): Boolean = BIRTH_DATE_REGEX.matcher(text).find()
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>> {
            val years = MaskTypeUtil.getYearsBetween(text, currentDate, "MMdd")
            return listOf("$key.Age" to years)
        }
    },
    BIRTH_DATE_REVERSE {
        override fun mask(text: String): String = LocalDate.parse(
            text,
            ISO_8601_DATE
        ).format(REVERSE_DATE)

        override fun unMask(text: String): String = LocalDate.parse(
            text,
            REVERSE_DATE
        ).format(ISO_8601_DATE)

        override fun isValid(text: String): Boolean = BIRTH_DATE_REVERSE_REGEX.matcher(text).find()
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>> {
            val years = MaskTypeUtil.getYearsBetween(text, currentDate, "-MM-dd")
            return listOf("$key.Age" to years)
        }
    },
    NORWEGIAN_POSTAL_CODE {
        override fun mask(text: String) = text
        override fun unMask(text: String) = text
        override fun isValid(text: String): Boolean = NORWEGIAN_POSTAL_CODE_REGEX.matcher(text).find()
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>>? =
            null
    },
    DIGITS {
        override fun mask(text: String) = text
        override fun unMask(text: String) = text
        override fun isValid(text: String): Boolean = text.all { it.isDigit() }
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>>? =
            null
    },
    UNKNOWN {
        override fun mask(text: String) = text
        override fun unMask(text: String) = text
        override fun isValid(text: String): Boolean = ANY_REGEX.matcher(text).find()
        override fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>>? =
            null
    };

    abstract fun mask(text: String): String
    abstract fun unMask(text: String): String
    abstract fun isValid(text: String): Boolean
    abstract fun derivedValues(text: String, key: String, currentDate: LocalDate): List<Pair<String, String>>?
}

fun maskTypeFromString(type: String) = when (type) {
    "PersonalNumber" -> MaskType.PERSONAL_NUMBER
    "NorwegianPersonalNumber" -> MaskType.NORWEGIAN_PERSONAL_NUMBER
    "DanishPersonalNumber" -> MaskType.DANISH_PERSONAL_NUMBER
    "PostalCode" -> MaskType.POSTAL_CODE
    "Email" -> MaskType.EMAIL
    "BirthDate" -> MaskType.BIRTH_DATE
    "BirthDateReverse" -> MaskType.BIRTH_DATE_REVERSE
    "NorwegianPostalCode" -> MaskType.NORWEGIAN_POSTAL_CODE
    "Digits" -> MaskType.DIGITS
    else -> MaskType.UNKNOWN
}
