package com.hedvig.app

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import com.hedvig.app.feature.norway.NorwegianAuthenticationViewModel

class MockNorwegianAuthenticationViewModel : NorwegianAuthenticationViewModel() {
    override val redirectUrl = MutableLiveData<String>()

    init {
        Handler().postDelayed({
            redirectUrl.postValue("https://www.example.com")
        }, 500)
    }
}
