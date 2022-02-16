package com.hedvig.app.feature.genericauth

import com.hedvig.app.MockActivity
import com.hedvig.app.genericDevelopmentAdapter
import org.koin.core.module.Module

class GenericAuthMockActivity : MockActivity() {
    override val original: List<Module> = emptyList()
    override val mocks: List<Module> = emptyList()

    override fun adapter() = genericDevelopmentAdapter {
        header("Email input")
        clickableItem("Launch") {
            startActivity(GenericAuthActivity.newInstance(context))
        }
    }
}
