package com.pixelvibe.vedioplayer.feature.recent.di

import com.pixelvibe.vedioplayer.feature.recent.RecentViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val recentFeatureModule = module {
    viewModelOf(::RecentViewModel)
}
