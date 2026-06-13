package com.pixelvibe.vedioplayer.feature.settings.di

import com.pixelvibe.vedioplayer.feature.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val settingsFeatureModule = module {
    viewModelOf(::SettingsViewModel)
}
