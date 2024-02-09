package com.hedvig.android.feature.chat.data

import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.ProdOrDemoProvider

class GetChatRepositoryProvider(
  override val demoManager: DemoManager,
  override val demoImpl: ChatRepository,
  override val prodImpl: ChatRepository,
) : ProdOrDemoProvider<ChatRepository>
