package com.example.cacciaaltesoro

import com.example.cacciaaltesoro.data.database.Supabase
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.data.repositories.EventRepositoryImpl
import com.example.cacciaaltesoro.data.repositories.TagRepository
import com.example.cacciaaltesoro.data.repositories.TagRepositoryImpl
import com.example.cacciaaltesoro.ui.screens.eventmapeditor.EventMapEditorViewModel
import com.example.cacciaaltesoro.ui.screens.newevent.NewEventViewModel
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single<TagRepository> { TagRepositoryImpl() }
    single<EventRepository> { EventRepositoryImpl() }

    single<SupabaseClient> { Supabase().supabase }

    viewModel { NewEventViewModel(get()) }
    viewModel { EventMapEditorViewModel(get()) }
}