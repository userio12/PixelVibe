package com.pixelvibe.vedioplayer.feature.player.di

import com.pixelvibe.vedioplayer.feature.player.PlayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val playerFeatureModule = module {
    viewModelOf(::PlayerViewModel)
}

// AudioEffectManager is provided by core:player's PlayerModule
