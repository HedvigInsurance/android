package com.hedvig.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.databinding.ActivityGenericDevelopmentBinding
import com.hedvig.app.util.extensions.viewBinding
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module

abstract class MockActivity : AppCompatActivity(R.layout.activity_generic_development) {
    protected val binding by viewBinding(ActivityGenericDevelopmentBinding::bind)

    protected abstract val original: List<Module>
    protected abstract val mocks: List<Module>

    abstract fun adapter(): GenericDevelopmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unloadKoinModules(original)
        loadKoinModules(mocks)

        binding.root.adapter = adapter()
    }

    override fun onDestroy() {
        super.onDestroy()

        unloadKoinModules(mocks)
        loadKoinModules(original)
    }
}
