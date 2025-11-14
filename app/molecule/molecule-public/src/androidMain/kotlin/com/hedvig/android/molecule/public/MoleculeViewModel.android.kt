package com.hedvig.android.molecule.public

import app.cash.molecule.AndroidUiDispatcher
import kotlin.coroutines.CoroutineContext

internal actual val mainDispatcher: CoroutineContext = AndroidUiDispatcher.Main
