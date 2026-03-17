package com.hedvig.android.permission.di

import com.hedvig.android.permission.NoopPermissionManager
import com.hedvig.android.permission.PermissionManager
import org.koin.dsl.module

val noopPermissionModule = module {
  single<PermissionManager> { NoopPermissionManager() }
}
