package com.hedvig.app.feature.embark

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.hedvig.app.feature.embark.util.MaskType
import org.junit.Test
import java.time.LocalDate

class MaskTypeTest {

    @Test
    fun `should parse age from personal number`() {
        val derivedValue = MaskType.PERSONAL_NUMBER.derivedValues(
            text = "910113-1234",
            key = "personalNumber",
            currentDate = LocalDate.of(2022, 1, 17)
        )
        assertThat(derivedValue).isNotNull()
        assertThat(derivedValue!![0].second).isEqualTo("31")

        val derivedValue2 = MaskType.PERSONAL_NUMBER.derivedValues(
            text = "020113-1234",
            key = "personalNumber",
            currentDate = LocalDate.of(2022, 1, 17)
        )
        assertThat(derivedValue2).isNotNull()
        assertThat(derivedValue2!![0].second).isEqualTo("20")
    }

    @Test
    fun `should parse age from birth date`() {
        val derivedValue = MaskType.BIRTH_DATE.derivedValues(
            text = "910113",
            key = "birthDate",
            currentDate = LocalDate.of(2022, 1, 17)
        )
        assertThat(derivedValue).isNotNull()
        assertThat(derivedValue!![0].second).isEqualTo("31")

        val derivedValue2 = MaskType.PERSONAL_NUMBER.derivedValues(
            text = "020113",
            key = "birthDate",
            currentDate = LocalDate.of(2022, 1, 17)
        )
        assertThat(derivedValue2).isNotNull()
        assertThat(derivedValue2!![0].second).isEqualTo("20")
    }

    @Test
    fun `should parse age from reversed birth date`() {
        val derivedValue = MaskType.BIRTH_DATE_REVERSE.derivedValues(
            text = MaskType.BIRTH_DATE_REVERSE.unMask("13-01-1991"),
            key = "birthDate",
            currentDate = LocalDate.of(2022, 1, 17)
        )
        assertThat(derivedValue).isNotNull()
        assertThat(derivedValue!![0].second).isEqualTo("31")

        val derivedValue2 = MaskType.BIRTH_DATE_REVERSE.derivedValues(
            text = MaskType.BIRTH_DATE_REVERSE.unMask("13-01-2002"),
            key = "birthDate",
            currentDate = LocalDate.of(2022, 1, 17)
        )
        assertThat(derivedValue2).isNotNull()
        assertThat(derivedValue2!![0].second).isEqualTo("20")
    }
}
