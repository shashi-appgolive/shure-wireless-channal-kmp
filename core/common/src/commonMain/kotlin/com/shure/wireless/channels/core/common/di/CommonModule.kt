package com.shure.wireless.channels.core.common.di

import com.shure.wireless.channels.core.common.coroutines.AppBackgroundCoroutineScope
import com.shure.wireless.channels.core.common.coroutines.AppCoroutineScope
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule: Module = module {
    single { AppCoroutineScope() }
    single { AppBackgroundCoroutineScope() }
}
