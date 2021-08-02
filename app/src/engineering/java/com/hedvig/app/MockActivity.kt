package com.hedvig.app

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.hedvig.app.databinding.ActivityGenericDevelopmentBinding
import com.hedvig.app.util.extensions.viewBinding
import dev.chrisbanes.insetter.applyInsetter
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
            WindowCompat.setDecorFitsSystemWindows(window, false)
            applyInsetter {
                type(navigationBars = true, statusBars = true) {
                    padding()
                }
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
