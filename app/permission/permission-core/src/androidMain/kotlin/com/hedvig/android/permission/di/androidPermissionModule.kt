package com.hedvig.android.permission.di

import android.content.Context
import com.hedvig.android.permission.ActivityCompatPermissionManager
import com.hedvig.android.permission.PermissionManager
import org.koin.dsl.module

val androidPermissionModule = module {
  single<PermissionManager> { ActivityCompatPermissionManager(get<Context>()) }
}
