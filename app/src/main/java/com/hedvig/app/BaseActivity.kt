package com.hedvig.app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.ice.restring.Restring
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity : AppCompatActivity() {

    val disposables = CompositeDisposable()
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Restring.wrapContext(newBase))
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
