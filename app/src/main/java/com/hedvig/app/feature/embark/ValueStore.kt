package com.hedvig.app.feature.embark

import com.hedvig.app.feature.embark.computedvalues.TemplateExpressionCalculator
import java.util.Stack

interface ValueStore : ValueStoreView {
    var computedValues: Map<String, String>?
    fun commitVersion()
    fun rollbackVersion()
    fun put(key: String, value: String?)
    fun put(key: String, value: List<String>)
    val prefill: ValueStoreView
    fun toMap(): Map<String, String?>
    fun getMultiActionItems(key: String): List<Map<String, String>>
}

interface ValueStoreView {
    fun get(key: String): String?
    fun getList(key: String): List<String>?
}

class ValueStoreImpl : ValueStore {
    private val storedListValues = HashMap<String, List<String>>()

    private val storedValues = Stack<HashMap<String, String?>>().apply { push(hashMapOf()) }
    private val stage = HashMap<String, String?>()

    private val prefillValues = HashMap<String, String?>()

    override var computedValues: Map<String, String>? = null

    override fun commitVersion() {
        storedValues.push(HashMap(storedValues.peek() + stage))
        stage.clear()
    }

    override fun rollbackVersion() {
        storedValues.pop()
    }

    override fun put(key: String, value: String?) {
        stage[key] = value
        prefillValues[key] = value
    }

    /**
     * Currently only used to store quoteIds, therefore no stage or prefill is required
     */
    override fun put(key: String, value: List<String>) {
        storedListValues[key] = value
    }

    override fun get(key: String): String? {
        return computedValues?.get(key)?.let {
            TemplateExpressionCalculator.evaluateTemplateExpression(it, storedValues.peek() + stage)
        } ?: storedValues.peek()[key] ?: stage[key]
    }

    override fun getList(key: String): List<String>? {
        return storedListValues[key]
    }

    override val prefill = object : ValueStoreView {
        override fun get(key: String) = this@ValueStoreImpl.get(key) ?: prefillValues[key]
        override fun getList(key: String): List<String>? = null
    }

    override fun toMap(): Map<String, String?> {
        return storedValues.peek().toMap() + stage
    }

    override fun getMultiActionItems(key: String): List<Map<String, String>> {
        val source = storedValues.peek() + stage
        return source
            .keys
            .filter { it.contains(key) }
            .fold(hashMapOf<String, MutableMap<String, String>>()) { acc, curr ->
                val withoutKey = curr.replace(key, "")
                if (!(withoutKey matches MULTI_ACTION_KEY)) {
                    return@fold acc
                }
                val groupValues = MULTI_ACTION_KEY.find(withoutKey)?.groupValues ?: return@fold acc
                val entry = groupValues.getOrNull(1) ?: return@fold acc
                val subKey = groupValues.getOrNull(2) ?: return@fold acc

                val obj = acc[entry] ?: hashMapOf()
                obj[subKey] = source[curr] ?: return@fold acc
                acc[entry] = obj

                acc
            }
            .entries
            .sortedBy { (k, _) -> k.toInt() }
            .map { (_, v) -> v }
    }

    companion object {
        private val MULTI_ACTION_KEY = Regex("\\[([0-9]+)\\]([a-zA-Z.]+)\$")
    }
}
