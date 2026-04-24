package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.data.repositories.MapEditorRepository
import com.example.cacciaaltesoro.data.repositories.MapEditorRepositoryImpl
import com.example.cacciaaltesoro.ui.screens.eventmapeditor.EventMapEditorViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<MapEditorRepository> { MapEditorRepositoryImpl() }

    viewModel { EventMapEditorViewModel(get()) }
}