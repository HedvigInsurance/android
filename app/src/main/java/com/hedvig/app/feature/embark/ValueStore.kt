package com.hedvig.app.feature.embark

import com.hedvig.app.feature.embark.computedvalues.TemplateExpressionCalculator
import java.util.Stack

interface ValueStore : ValueStoreView {
    var computedValues: Map<String, String>?
    fun <T> withCommittedVersion(block: ValueStore.() -> T): T
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
    private val stage = Stack<HashMap<String, String?>>().apply { push(hashMapOf()) }

    private val prefillValues = HashMap<String, String?>()

    override var computedValues: Map<String, String>? = null

    /**
     * Runs [block] on the committed version of the current [ValueStore]. Makes sure to revert the store to its previous
     * state after the [block] scope.
     */
    override fun <T> withCommittedVersion(block: ValueStore.() -> T): T {
        commitVersion()
        val result = this.block()
        rollbackVersion()
        return result
    }

    override fun commitVersion() {
        val newHead = storedValues.peek() + stage.peek()
        storedValues.push(HashMap(newHead))
        stage.push(hashMapOf())
    }

    override fun rollbackVersion() {
        storedValues.pop()
        stage.pop()
    }

    override fun put(key: String, value: String?) {
        stage.peek()[key] = value
        prefillValues[key] = value
    }

    /**
     * Currently only used to store quoteIds, therefore no stage or prefill is required
     */
    override fun put(key: String, value: List<String>) {
        storedListValues[key] = value
    }

    override fun get(key: String): String? {
        val value = computedValues?.get(key)?.let {
            TemplateExpressionCalculator.evaluateTemplateExpression(it, storedValues.peek() + stage.peek())
        } ?: storedValues.peek()[key] ?: stage.peek()[key]

        return if (value.equals("null")) {
            null
        } else {
            value
        }
    }

    override fun getList(key: String): List<String>? {
        return storedListValues[key]
    }

    override val prefill = object : ValueStoreView {
        override fun get(key: String): String? {
            val value = this@ValueStoreImpl.get(key) ?: prefillValues[key]

            return if (value.equals("null")) {
                null
            } else {
                value
            }
        }

        override fun getList(key: String): List<String>? = null
    }

    override fun toMap(): Map<String, String?> {
        return storedValues.peek() + stage.peek()
    }

    override fun getMultiActionItems(key: String): List<Map<String, String>> {
        val source = storedValues.peek() + stage.peek()
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

    override fun toString(): String {
        return "ValueStoreImpl(storedListValues=$storedListValues, storedValues=$storedValues, stage=$stage, prefillValues=$prefillValues, computedValues=$computedValues, prefill=$prefill)"
    }

    companion object {
        private val MULTI_ACTION_KEY = Regex("\\[([0-9]+)\\]([a-zA-Z.]+)\$")
    }
}
