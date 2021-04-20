package com.hedvig.app.feature.embark

import com.hedvig.app.feature.embark.computedvalues.TemplateExpressionCalculator
import java.util.Stack

interface ValueStore {
    var computedValues: Map<String, String>?
    fun commitVersion()
    fun rollbackVersion()
    fun put(key: String, value: String)
    fun get(key: String): String?
    fun toMap(): Map<String, String>
}

class ValueStoreImpl : ValueStore {
    private val storedValues = Stack<HashMap<String, String>>().apply { push(hashMapOf()) }
    private val stage = HashMap<String, String>()

    override var computedValues: Map<String, String>? = null
    override fun commitVersion() {
        storedValues.push(HashMap(storedValues.peek() + stage))
        stage.clear()
    }

    override fun rollbackVersion() {
        storedValues.pop()
    }

    override fun put(key: String, value: String) {
        stage[key] = value
    }

    override fun get(key: String): String? {
        return computedValues?.get(key)?.let {
            TemplateExpressionCalculator.evaluateTemplateExpression(it, storedValues.peek())
        } ?: storedValues.peek()[key] ?: stage[key]
    }

    override fun toMap(): Map<String, String> {
        return storedValues.peek().toMap() + stage
    }
}
