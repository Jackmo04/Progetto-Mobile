package com.example.cacciaaltesoro.ui

import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.example.cacciaaltesoro.ui.screens.eventdetails.EventDetailsScreen
import com.example.cacciaaltesoro.ui.screens.eventdetails.EventDetailsViewModel
import com.example.cacciaaltesoro.ui.screens.eventeditor.tageditor.EventTagEditorScreen
import com.example.cacciaaltesoro.ui.screens.game.GameScreen
import com.example.cacciaaltesoro.ui.screens.home.HomeScreen
import com.example.cacciaaltesoro.ui.screens.login.LoginScreen
import com.example.cacciaaltesoro.ui.screens.login.LoginScreenViewModel
import com.example.cacciaaltesoro.ui.screens.eventeditor.EventEditorScreen
import com.example.cacciaaltesoro.ui.screens.eventeditor.EventEditorViewModel
import com.example.cacciaaltesoro.ui.screens.eventeditor.tageditor.EventTagEditorViewModel
import com.example.cacciaaltesoro.ui.screens.game.GameViewModel
import com.example.cacciaaltesoro.ui.screens.onlineevents.OnlineEventsViewModel
import com.example.cacciaaltesoro.ui.screens.onlineevents.OnlineEventsScreen
import com.example.cacciaaltesoro.ui.screens.savedevents.SavedEventsScreen
import com.example.cacciaaltesoro.ui.screens.savedevents.SavedEventsViewModel
import com.example.cacciaaltesoro.ui.screens.splash.SplashScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

sealed interface NavigationRoute {
    @Serializable data object Splash : NavigationRoute
    @Serializable data object Home : NavigationRoute
    @Serializable data object OnlineEvents : NavigationRoute
    @Serializable data class EventDetails(val eventId: Int) : NavigationRoute
    @Serializable data class Game(val eventId: Int) : NavigationRoute
    @Serializable data object SavedEvents : NavigationRoute
    @Serializable data object Login : NavigationRoute
    @Serializable data class EventEditor(val eventId: Int? = null) : NavigationRoute
    @Serializable data class EventTagEditor(val lat: Double, val lon: Double) : NavigationRoute
}

@Composable
fun CacciaAlTesoroNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Splash,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable<NavigationRoute.Splash> {
            SplashScreen(navController = navController)
        }
        composable<NavigationRoute.Home> {
            HomeScreen(navController,koinViewModel<LoginScreenViewModel>())
        }
        composable<NavigationRoute.Login>(
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
        composable<NavigationRoute.OnlineEvents> {
            val newOnlineEvent = koinViewModel<OnlineEventsViewModel>()
            val newLoginEvent = koinViewModel<LoginScreenViewModel>()
            OnlineEventsScreen(navController, newOnlineEvent, newLoginEvent)
        }
        composable<NavigationRoute.EventDetails> { backStackEntry ->

            val eventDetailsViewModel = koinViewModel<EventDetailsViewModel>()
            val newLoginEvent = koinViewModel<LoginScreenViewModel>()
            val route = backStackEntry.toRoute<NavigationRoute.EventDetails>()
            EventDetailsScreen(navController, route.eventId ,eventDetailsViewModel,newLoginEvent)
        }
        composable<NavigationRoute.Game> { backStackEntry ->
            val route = backStackEntry.toRoute<NavigationRoute.Game>()
            val gameViewModel = koinViewModel<GameViewModel>(
                parameters = { parametersOf(route.eventId) }
            )
            GameScreen(navController, gameViewModel)
        }
        composable<NavigationRoute.SavedEvents> {
            val savedEventViewModel = koinViewModel<SavedEventsViewModel>()
            val newLoginEvent = koinViewModel<LoginScreenViewModel>()
            SavedEventsScreen(navController, savedEventViewModel,newLoginEvent)
        }
        composable<NavigationRoute.EventEditor> { backStackEntry ->
            val route = backStackEntry.toRoute<NavigationRoute.EventEditor>()
            val newEventVM = koinViewModel<EventEditorViewModel>(
                parameters = { parametersOf(route.eventId) }
            )
            EventEditorScreen(navController, newEventVM)
        }
        composable<NavigationRoute.EventTagEditor> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry<NavigationRoute.EventEditor>()
            }
            val route = backStackEntry.toRoute<NavigationRoute.EventTagEditor>()
            val sharedViewModel: EventEditorViewModel = koinViewModel(viewModelStoreOwner = parentEntry)
            val viewModel = koinViewModel<EventTagEditorViewModel>()
            EventTagEditorScreen(
                navController,
                sharedViewModel,
                viewModel,
                route.lat,
                route.lon
            )
        }
    }
}
