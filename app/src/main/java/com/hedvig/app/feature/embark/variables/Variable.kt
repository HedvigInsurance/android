package com.hedvig.app.feature.embark.variables

sealed class Variable {
    abstract val key: String

    data class Single(
        override val key: String,
        val from: String,
        val castAs: CastType,
    ) : Variable()

    data class Constant(
        override val key: String,
        val value: String,
        val castAs: CastType,
    ) : Variable()

    data class Generated(
        override val key: String,
        val storeAs: String
    ) : Variable()

    data class Multi(
        override val key: String,
        val variables: List<Variable>,
    ) : Variable()
}

enum class CastType {
    STRING, INT, BOOLEAN, FILE, NONE, UNKNOWN;

    fun cast(value: String?) = when (this) {
        STRING -> value
        INT -> getIntOrNull(value)
        BOOLEAN -> value.toBoolean()
        // FILE is not handled here since we need to create a separate multipart request body for
        // uploading. See extractFileVariable in VariableExtractor.
        FILE -> null
        // Unsupported generated types are ignored for now.
        UNKNOWN -> null
        NONE -> null
    }

    private fun getIntOrNull(storeValue: String?): Int? {
        if (storeValue == null) return null
        val intValue = storeValue.toIntOrNull()
        if (intValue != null) return intValue
        // The stored value can in some cases be floats, eg. for computed store values
        return storeValue.toFloatOrNull()?.toInt()
    }
}
