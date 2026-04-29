package com.example.cacciaaltesoro

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.cacciaaltesoro.data.database.Supabase
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.data.repositories.EventRepositoryImpl
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import com.example.cacciaaltesoro.data.repositories.TagRepository
import com.example.cacciaaltesoro.data.repositories.TagRepositoryImpl
import com.example.cacciaaltesoro.ui.CacciaAlTesoroRoute
import com.example.cacciaaltesoro.ui.screens.eventmapeditor.EventMapEditorViewModel
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel
import com.example.cacciaaltesoro.ui.screens.newevent.NewEventViewModel
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val Context.dataStore by preferencesDataStore("settings")

val appModule = module {
    single { get<Context>().dataStore }

    single { LoginRepository(get() , get()) }
    single<TagRepository> { TagRepositoryImpl() }
    single<EventRepository> { EventRepositoryImpl(get()) }

    single<SupabaseClient> { Supabase().supabase }

    viewModel { LoginScreenViewModel(get() ) }
    viewModel { NewEventViewModel(get()) }
    viewModel { EventMapEditorViewModel(get()) }
}