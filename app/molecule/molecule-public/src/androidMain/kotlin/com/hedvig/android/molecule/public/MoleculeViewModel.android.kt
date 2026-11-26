package com.hedvig.android.molecule.public

import app.cash.molecule.AndroidUiDispatcher
import kotlin.coroutines.CoroutineContext

actual val mainDispatcher: CoroutineContext = AndroidUiDispatcher.Main
