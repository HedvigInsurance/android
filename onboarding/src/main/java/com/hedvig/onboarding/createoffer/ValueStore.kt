package com.hedvig.onboarding.createoffer

import com.hedvig.onboarding.createoffer.computedvalues.TemplateExpressionCalculator

class ValueStore(
    private val computedValues: Map<String, String>
) {

    private val storedValues = HashMap<String, String>()

    fun put(key: String, value: String) {
        storedValues[key] = value
    }

    fun get(key: String): String? {
        return computedValues[key]?.let {
            TemplateExpressionCalculator.evaluateTemplateExpression(it, storedValues)
        } ?: storedValues[key]
    }

    fun toMap(): Map<String, String> {
        return storedValues.toMap()
    }
}
