package com.hedvig.app

import android.app.Application
import android.content.Context
import androidx.work.WorkerParameters
import com.hedvig.app.feature.embark.passages.numberactionset.NumberActionParams
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import kotlinx.coroutines.CoroutineScope
import org.junit.Ignore
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify
import java.net.URI

class KoinCheckModulesTest : KoinTest {
  @OptIn(KoinExperimentalAPI::class)
  @Test
  // TODO check if koin provides some way to work around the issue of not being able to ignore classes from inside
  //  submodules which are marked as internal
  @Ignore
  fun checkAllModules() {
    applicationModule.verify(
      // This list is a list of types that Koin shouldn't be checking if they exist in the graph for various reasons
      listOf(
        URI::class,
        // Classes coming by default from koin inside the `startKoin` block, like for WorkManager or application context
        Application::class,
        Context::class,
        WorkerParameters::class,
        // Provided classes on a per-call-site basis, using parametersOf()
        ChangeDateBottomSheetData::class,
        QuoteCartId::class,
        NumberActionParams::class,
//        com.hedvig.android.odyssey.model.FlowId::class,
//        com.hedvig.android.odyssey.navigation.AudioContent::class,
        // Types that don't exist because they're set as default parameters anwyay
        CoroutineScope::class,
        // Set for when doing Koin.getAll() and turning it into a set to remove duplicates
        Set::class,
        // Navigator uses a `Class<*>` to know where to go on logout.
        Class::class,
      ),
    )
  }
}
