package com.hedvig.app

import com.google.common.truth.Truth.assertThat
import com.hedvig.app.feature.embark.computedvalues.TemplateExpressionCalculator
import org.junit.Test

class ComputedValuesTest {

    private val emptyStore = hashMapOf<String, String>()

    @Test
    fun testSubstitutingANumber() {
        val expression = "2"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("2")
    }

    @Test
    fun testCalculateTwoIntegers() {
        val expression = "41+1"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("42")

        val expressionWithSpace = "41 + 1"
        val result2 = TemplateExpressionCalculator.evaluateTemplateExpression(expressionWithSpace, emptyStore)
        assertThat(result2).isEqualTo("42")
    }

    @Test
    fun testCalculateMultipleIntegersWithAdditionAndSubtraction() {
        val expression = "1 +2 + 3 -1 "
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("5")
    }

    @Test
    fun testCalculateWithFloats() {
        val expression = "13.17 + 0.2"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("13.37")
    }

    @Test
    fun testCalculateWithFloatAndInt() {
        val expression = "13 + 0.37"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("13.37")
    }

    @Test
    fun testCalculateAnEvenFloatAndIntToBeAnInt() {
        val expression = "1337 + 1.0"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("1338")
    }

    @Test
    fun testConcatenateDoubleQuoteStrings() {
        val expression = "\"133\" ++ \"7\""
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("1337")
    }

    @Test
    fun testConcatenateSingleQuoteStrings() {
        val expression = "\'133\' ++ \'7\'"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("1337")
    }

    @Test
    fun testConcatenateSingleAndDoubleQuoteStrings() {
        val expression = "\"133\" ++ \'7\'"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("1337")
    }

    @Test
    fun testReferenceStoreKey() {
        val expression = "myKey"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, hashMapOf("myKey" to "my value"))
        assertThat(result).isEqualTo("my value")
    }

    @Test
    fun testCalculateWithStoreReference() {
        val expression = "myKey + 1"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, hashMapOf("myKey" to "41"))
        assertThat(result).isEqualTo("42")
    }

    @Test
    fun testStringConcatenationWithStoreReference() {
        val expression = "\"foo\" ++ \" \"++ myKey ++ ' '++\"baz\""
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, hashMapOf("myKey" to "bar"))
        assertThat(result).isEqualTo("foo bar baz")
    }

    @Test
    fun failsWhenUsingMultipleOperatorsInSuccession() {
        val expression = "\"foo\" ++ ++ \"bar\""
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("Invalid use of operator \"++\", must have expressions on both sides")
    }

    @Test
    fun twoNumbersShouldGenerateError() {
        val expression = "42 1337"
        val result = TemplateExpressionCalculator.evaluateTemplateExpression(expression, emptyStore)
        assertThat(result).isEqualTo("Unexpected number constant \"1337\"")
    }

}
