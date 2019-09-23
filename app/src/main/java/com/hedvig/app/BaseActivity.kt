package com.hedvig.app

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

abstract class BaseActivity : AppCompatActivity() {

    val disposables = CompositeDisposable()

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
