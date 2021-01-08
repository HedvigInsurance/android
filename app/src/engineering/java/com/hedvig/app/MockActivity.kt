package com.hedvig.app

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.databinding.ActivityGenericDevelopmentBinding
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module

abstract class MockActivity : AppCompatActivity(R.layout.activity_generic_development) {
    protected val binding by viewBinding(ActivityGenericDevelopmentBinding::bind)

    protected abstract val original: List<Module>
    protected abstract val mocks: List<Module>

    abstract fun adapter(): GenericDevelopmentAdapter

    val context: Context
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unloadKoinModules(original)
        loadKoinModules(mocks)

        binding.root.apply {
            setEdgeToEdgeSystemUiFlags(true)
            doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(
                    top = initialState.paddings.top + insets.systemWindowInsetTop,
                    bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
                )
            }
            adapter = adapter()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unloadKoinModules(mocks)
        loadKoinModules(original)
    }
}
