package com.hedvig.app.util

import d
import org.koin.core.context.loadKoinModules
import org.koin.core.error.DefinitionOverrideException
import org.koin.core.module.Module

fun loadKoinModulesIfNotDefined(modules: List<Module>) {
    modules.forEach {
        try {
            loadKoinModules(it)
        } catch (e: DefinitionOverrideException) {
            d(e) { "module: $it already defined, $e" }
        }
    }
}

