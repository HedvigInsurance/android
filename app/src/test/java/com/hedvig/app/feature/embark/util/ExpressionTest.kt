package com.hedvig.app.feature.embark.util

import assertk.assertThat
import assertk.assertions.isInstanceOf
import com.hedvig.app.feature.embark.ExpressionResult
import com.hedvig.app.feature.embark.ValueStoreImpl
import com.hedvig.app.testdata.feature.embark.builders.ExpressionBuilder
import org.junit.Test

class ExpressionTest {
    @Test
    fun `given a true unary expression, should evaluate to true`() {
        val expression = ExpressionBuilder(type = ExpressionBuilder.ExpressionType.ALWAYS).build()

        assertThat(evaluateExpression(expression, ValueStoreImpl())).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false unary expression, should evaluate to true`() {
        val expression = ExpressionBuilder(type = ExpressionBuilder.ExpressionType.NEVER).build()

        assertThat(evaluateExpression(expression, ValueStoreImpl())).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a true equals expression, should evaluate to true`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.EQUALS,
            key = "FOO",
            value = "BAR"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "BAR")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false equals expression, should evaluate to false`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.EQUALS,
            key = "FOO",
            value = "BAR"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "BAZ")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a true equals expression involving null, should evaluate to true`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.EQUALS,
            key = "FOO",
            value = "null"
        ).build()

        assertThat(evaluateExpression(expression, ValueStoreImpl())).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false equals expression involving null, should evaluate to false`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.EQUALS,
            key = "FOO",
            value = "null"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", emptyList())

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a true greater than expression, should evaluate to false`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.GREATER_THAN,
            key = "FOO",
            value = "4"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "5")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false greater than expression, should evaluate to false`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.GREATER_THAN,
            key = "FOO",
            value = "6"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "5")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a true greater than or equals expression, should evaluate to true`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.GREATER_THAN_OR_EQUALS,
            key = "FOO",
            value = "5"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "5")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false greater than or equals expression, should evaluate to false`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.GREATER_THAN_OR_EQUALS,
            key = "FOO",
            value = "6"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "5")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a true less than expression, should evaluate to true`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.LESS_THAN,
            key = "FOO",
            value = "6"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "5")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false less than expression, should evaluate to false`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.LESS_THAN,
            key = "FOO",
            value = "4"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "5")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a true less than or equals expression, should evaluate to true`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.LESS_THAN_OR_EQUALS,
            key = "FOO",
            value = "5"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "5")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false less than or equals expression, should evaluate to false`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.LESS_THAN_OR_EQUALS,
            key = "FOO",
            value = "4"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "5")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a true not equals expression, should evaluate to true`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.NOT_EQUALS,
            key = "FOO",
            value = "BAZ"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "BAR")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a true not equals expression involving null, should evaluate to true`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.NOT_EQUALS,
            key = "FOO",
            value = "null"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", emptyList())
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false not equals expression involving null, should evaluate to false`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.NOT_EQUALS,
            key = "FOO",
            value = "null"
        ).build()

        assertThat(evaluateExpression(expression, ValueStoreImpl())).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a false not equals expression, should evaluate to false`() {
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.NOT_EQUALS,
            key = "FOO",
            value = "BAZ"
        ).build()

        val valueStore = ValueStoreImpl()
        valueStore.put("FOO", "BAZ")
        valueStore.commitVersion()

        assertThat(evaluateExpression(expression, valueStore)).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a true and expression, should evaluate to true`() {
        val expressionOne = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.ALWAYS,
        ).build()
        val expressionTwo = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.ALWAYS,
        ).build()
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.AND,
            subExpressions = listOf(expressionOne, expressionTwo),
        ).build()

        assertThat(evaluateExpression(expression, ValueStoreImpl())).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false and expression, should evaluate to false`() {
        val expressionOne = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.ALWAYS,
        ).build()
        val expressionTwo = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.NEVER,
        ).build()
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.AND,
            subExpressions = listOf(expressionOne, expressionTwo),
        ).build()

        assertThat(evaluateExpression(expression, ValueStoreImpl())).isInstanceOf(ExpressionResult.False::class)
    }

    @Test
    fun `given a true or expression, should evaluate to true`() {
        val expressionOne = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.ALWAYS,
        ).build()
        val expressionTwo = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.NEVER,
        ).build()
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.OR,
            subExpressions = listOf(expressionOne, expressionTwo),
        ).build()

        assertThat(evaluateExpression(expression, ValueStoreImpl())).isInstanceOf(ExpressionResult.True::class)
    }

    @Test
    fun `given a false or expression, should evaluate to false`() {
        val expressionOne = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.NEVER,
        ).build()
        val expressionTwo = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.NEVER,
        ).build()
        val expression = ExpressionBuilder(
            type = ExpressionBuilder.ExpressionType.OR,
            subExpressions = listOf(expressionOne, expressionTwo),
        ).build()

        assertThat(evaluateExpression(expression, ValueStoreImpl())).isInstanceOf(ExpressionResult.False::class)
    }
}
