package com.pixelvibe.vedioplayer.feature.network.di

import com.pixelvibe.vedioplayer.feature.network.NetworkViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val networkFeatureModule = module {
    viewModelOf(::NetworkViewModel)
}
