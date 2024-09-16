package com.hedvig.android.feature.chat.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

internal class GetCbmChatRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: CbmChatRepository,
  override val prodImpl: CbmChatRepository,
) : ProdOrDemoProvider<CbmChatRepository>
