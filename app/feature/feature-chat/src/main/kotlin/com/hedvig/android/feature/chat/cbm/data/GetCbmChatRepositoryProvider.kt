package com.hedvig.android.feature.chat.cbm.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider
import com.hedvig.android.feature.chat.cbm.CbmChatRepository

internal class GetCbmChatRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: CbmChatRepository,
  override val prodImpl: CbmChatRepository,
) : ProdOrDemoProvider<CbmChatRepository>
