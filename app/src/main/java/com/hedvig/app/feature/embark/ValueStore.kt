package com.hedvig.app.feature.embark

import com.hedvig.app.feature.embark.computedvalues.TemplateExpressionCalculator

interface ValueStore {
    var computedValues: Map<String, String>?
    fun put(key: String, value: String)
    fun get(key: String): String?
    fun toMap(): Map<String, String>
}

class ValueStoreImpl : ValueStore {

    override var computedValues: Map<String, String>? = null
    private val storedValues = HashMap<String, String>()

    override fun put(key: String, value: String) {
        storedValues[key] = value
    }

    override fun get(key: String): String? {
        return computedValues?.get(key)?.let {
            TemplateExpressionCalculator.evaluateTemplateExpression(it, storedValues)
        } ?: storedValues[key]
    }

    override fun toMap(): Map<String, String> {
        return storedValues.toMap()
    }
}
