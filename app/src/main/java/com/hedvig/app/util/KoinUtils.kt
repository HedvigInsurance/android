package com.hedvig.app.util

import org.koin.core.context.loadKoinModules
import org.koin.core.error.DefinitionOverrideException
import org.koin.core.module.Module
import timber.log.Timber

fun loadKoinModulesIfNotDefined(modules: List<Module>) {
    modules.forEach {
        try {
            loadKoinModules(it)
        } catch (e: DefinitionOverrideException) {
            Timber.d("module: $it already defined, $e")
        }
    }
}

