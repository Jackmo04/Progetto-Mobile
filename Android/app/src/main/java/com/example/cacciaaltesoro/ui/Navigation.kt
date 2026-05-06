package com.example.cacciaaltesoro.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.example.cacciaaltesoro.data.repositories.LoginRepository
import com.example.cacciaaltesoro.ui.screens.eventdetails.EventDetailsScreen
import com.example.cacciaaltesoro.ui.screens.eventdetails.EventDetailsViewModel
import com.example.cacciaaltesoro.ui.screens.eventmapeditor.EventMapEditorScreen
import com.example.cacciaaltesoro.ui.screens.eventmapeditor.EventMapEditorViewModel
import com.example.cacciaaltesoro.ui.screens.game.GameScreen
import com.example.cacciaaltesoro.ui.screens.home.HomeScreen
import com.example.cacciaaltesoro.ui.screens.login.LoginScreen
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel
import com.example.cacciaaltesoro.ui.screens.newevent.NewEventScreen
import com.example.cacciaaltesoro.ui.screens.newevent.NewEventViewModel
import com.example.cacciaaltesoro.ui.screens.onlineevents.OnlineEventViewModel
import com.example.cacciaaltesoro.ui.screens.onlineevents.OnlineEventsScreen
import com.example.cacciaaltesoro.ui.screens.savedevents.SavedEventsScreen
import com.example.cacciaaltesoro.ui.screens.savedevents.SavedEventsViewModel
import com.example.cacciaaltesoro.ui.screens.tageditor.TagEditorScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

sealed interface CacciaAlTesoroRoute {
    @Serializable data object Home : CacciaAlTesoroRoute
    @Serializable data object OnlineEvents : CacciaAlTesoroRoute
    @Serializable data class EventDetails(val eventId: Int) : CacciaAlTesoroRoute
    @Serializable data class Game(val eventId: String) : CacciaAlTesoroRoute
    @Serializable data object SavedEvents : CacciaAlTesoroRoute
    @Serializable data object Login : CacciaAlTesoroRoute
    @Serializable data object NewEvent : CacciaAlTesoroRoute
    @Serializable data class EventMapEditor(val eventId: String) : CacciaAlTesoroRoute
    @Serializable data class TagEditor(val tagId: String) : CacciaAlTesoroRoute
}

@Composable
fun CacciaAlTesoroNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = CacciaAlTesoroRoute.Home
    ) {
        composable<CacciaAlTesoroRoute.Home> {
            HomeScreen(navController)
        }
        composable<CacciaAlTesoroRoute.Login>(
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "caccia-al-tesoro://reset-password.*"
                }
            )
        ) {
            val loginVm = koinViewModel<LoginScreenViewModel>()
            LoginScreen(
                navController = navController,
                viewModel= loginVm
            )
        }
        composable<CacciaAlTesoroRoute.OnlineEvents> {
            val newOnlineEvent = koinViewModel<OnlineEventViewModel>()
            OnlineEventsScreen(navController, newOnlineEvent)
        }
        composable<CacciaAlTesoroRoute.EventDetails> { backStackEntry ->

            val eventDetailsViewModel = koinViewModel<EventDetailsViewModel>()
            Log.i("CardLog" , eventDetailsViewModel.toString())
            val route = backStackEntry.toRoute<CacciaAlTesoroRoute.EventDetails>()
            Log.i("CardLog" , route.toString())
            EventDetailsScreen(navController, route.eventId ,eventDetailsViewModel)
        }
        composable<CacciaAlTesoroRoute.Game> { backStackEntry ->
            val route = backStackEntry.toRoute<CacciaAlTesoroRoute.Game>()
            GameScreen(navController, route.eventId)
        }
        composable<CacciaAlTesoroRoute.SavedEvents> {
            val savedEventViewModel = koinViewModel<SavedEventsViewModel>()
            SavedEventsScreen(navController, savedEventViewModel)
        }
        composable<CacciaAlTesoroRoute.NewEvent> {
            val newEventVM = koinViewModel<NewEventViewModel>()
            NewEventScreen(navController, newEventVM)
        }
        composable<CacciaAlTesoroRoute.EventMapEditor> { backStackEntry ->
            val route = backStackEntry.toRoute<CacciaAlTesoroRoute.EventMapEditor>()
            val eventMapEditorVM = koinViewModel<EventMapEditorViewModel>()
            EventMapEditorScreen(navController, eventMapEditorVM, route.eventId)
        }
        composable<CacciaAlTesoroRoute.TagEditor> { backStackEntry ->
            val route = backStackEntry.toRoute<CacciaAlTesoroRoute.TagEditor>()
            TagEditorScreen(navController, route.tagId)
        }
    }
}
