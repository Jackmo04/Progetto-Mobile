package com.example.cacciaaltesoro

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.cacciaaltesoro.data.database.Supabase
import com.example.cacciaaltesoro.data.repositories.EventRepository
import com.example.cacciaaltesoro.data.repositories.EventRepositoryImpl
import com.example.cacciaaltesoro.data.repositories.LoginRepositoryImpl
import com.example.cacciaaltesoro.data.repositories.TagRepository
import com.example.cacciaaltesoro.data.repositories.TagRepositoryImpl
import com.example.cacciaaltesoro.ui.screens.eventdetails.EventDetailsViewModel
import com.example.cacciaaltesoro.ui.screens.eventmapeditor.EventMapEditorViewModel
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel
import com.example.cacciaaltesoro.ui.screens.newevent.NewEventViewModel
import com.example.cacciaaltesoro.ui.screens.onlineevents.OnlineEventViewModel
import com.example.cacciaaltesoro.ui.screens.savedevents.SavedEventsViewModel
import io.github.jan.supabase.SupabaseClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val Context.dataStore by preferencesDataStore("settings")

val appModule = module {
    single { get<Context>().dataStore }

    single { LoginRepositoryImpl(get() , get()) }
    single<TagRepository> { TagRepositoryImpl() }
    single<EventRepository> { EventRepositoryImpl(get()) }

    single<SupabaseClient> { Supabase().supabase }

    viewModel { LoginScreenViewModel(get() ) }
    viewModel { OnlineEventViewModel(get(), get ()) }
    viewModel { SavedEventsViewModel(get(), get ()) }
    viewModel { NewEventViewModel(get()) }
    viewModel { EventMapEditorViewModel(get()) }
    viewModel { EventDetailsViewModel(get(), get()) }
}