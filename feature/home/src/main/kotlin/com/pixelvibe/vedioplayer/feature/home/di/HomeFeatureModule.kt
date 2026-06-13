package com.pixelvibe.vedioplayer.feature.home.di

import com.pixelvibe.vedioplayer.feature.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val homeFeatureModule = module {
    viewModelOf(::HomeViewModel)
}
