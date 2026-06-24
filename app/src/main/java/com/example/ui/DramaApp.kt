package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.ui.theme.Crimson
import com.example.ui.theme.DarkGray
import com.example.ui.theme.LightGrayInfo
import com.example.ui.theme.PureBlack

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun DramaApp(viewModel: DramaViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    val navItems = listOf(
        BottomNavItem("الرئيسية", Icons.Default.Home, "home_route"),
        BottomNavItem("من أجلك", Icons.Default.PlayArrow, "foryou_route"),
        BottomNavItem("قائمتي", Icons.AutoMirrored.Filled.List, "mylist_route"),
        BottomNavItem("الحساب", Icons.Default.Person, "profile_route")
    )

    val isPlayerScreen = currentDestination?.startsWith("player_route") == true
    val isAdminScreen = currentDestination?.startsWith("admin_route") == true
    val showBottomBar = !isPlayerScreen && !isAdminScreen

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = PureBlack,
                    contentColor = LightGrayInfo
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentDestination == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Crimson,
                                selectedTextColor = Crimson,
                                unselectedIconColor = LightGrayInfo,
                                unselectedTextColor = LightGrayInfo,
                                indicatorColor = DarkGray
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home_route",
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            composable("home_route") { 
                HomeScreen(
                    viewModel = viewModel,
                    onDramaClick = { dramaId -> navController.navigate("player_route/$dramaId/0") },
                    onSeriesClick = { dramaId -> navController.navigate("series_route/$dramaId") },
                    onAdminClick = { navController.navigate("admin_route") },
                    onNotificationsClick = { navController.navigate("notifications_route") }
                ) 
            }
            composable("notifications_route") {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onDramaClick = { dramaId -> navController.navigate("series_route/$dramaId") }
                )
            }
            composable("admin_route") {
                AdminScreen(
                    onBack = { navController.popBackStack() },
                    onAddContentClick = { isMovie -> navController.navigate("admin_add_content/$isMovie") },
                    onMoviesListClick = { navController.navigate("admin_movies_list") },
                    onSeriesListClick = { navController.navigate("admin_series_list") },
                    onAdsClick = { navController.navigate("admin_ads_screen") }
                )
            }
            composable(
                "admin_add_content/{isMovie}",
                arguments = listOf(androidx.navigation.navArgument("isMovie") { type = androidx.navigation.NavType.BoolType })
            ) { backStackEntry ->
                val isMovie = backStackEntry.arguments?.getBoolean("isMovie") ?: true
                AdminAddContentScreen(
                    isMovieConfig = isMovie,
                    onBack = { navController.popBackStack() }, 
                    viewModel = viewModel
                )
            }
            composable("admin_movies_list") {
                AdminContentListScreen(
                    title = "الأفلام", 
                    isMovie = true,
                    onBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("admin_edit_content/$id") },
                    viewModel = viewModel
                )
            }
            composable("admin_series_list") {
                AdminContentListScreen(
                    title = "المسلسلات", 
                    isMovie = false,
                    onBack = { navController.popBackStack() },
                    onEditClick = { id -> navController.navigate("admin_edit_content/$id") },
                    viewModel = viewModel
                )
            }
            composable("admin_ads_screen") {
                AdminAdsScreen(onBack = { navController.popBackStack() })
            }
            composable("admin_edit_content/{contentId}") { backStackEntry ->
                val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
                AdminEditContentScreen(
                    contentId = contentId,
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel
                )
            }
            composable("foryou_route") { 
                ForYouScreen(
                    viewModel = viewModel,
                    onDramaClick = { dramaId -> navController.navigate("player_route/$dramaId/0") }
                ) 
            }
            composable("mylist_route") { 
                MyListScreen(
                    viewModel = viewModel,
                    onDramaClick = { dramaId -> navController.navigate("player_route/$dramaId/0") }
                ) 
            }
            composable("profile_route") { 
                ProfileScreen(onLoginClick = { navController.navigate("login_route") }) 
            }
            composable("login_route") {
                LoginScreen(onBack = { navController.popBackStack() })
            }
            composable("series_route/{dramaId}") { backStackEntry ->
                val dramaId = backStackEntry.arguments?.getString("dramaId") ?: ""
                SeriesDetailsScreen(
                    dramaId = dramaId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onEpisodeClick = { episodeIndex -> navController.navigate("player_route/$dramaId/$episodeIndex") }
                )
            }
            composable(
                "player_route/{dramaId}/{episodeIndex}",
                arguments = listOf(
                    androidx.navigation.navArgument("dramaId") { type = androidx.navigation.NavType.StringType },
                    androidx.navigation.navArgument("episodeIndex") { type = androidx.navigation.NavType.IntType; defaultValue = 0 }
                )
            ) { backStackEntry ->
                val dramaId = backStackEntry.arguments?.getString("dramaId") ?: ""
                val episodeIndex = backStackEntry.arguments?.getInt("episodeIndex") ?: 0
                PlayerScreen(
                    dramaId = dramaId,
                    initialEpisodeIndex = episodeIndex,
                    onBack = { navController.popBackStack() },
                    onLoginClick = { navController.navigate("login_route") },
                    onAllEpisodesClick = { navController.navigate("series_route/$dramaId") },
                    viewModel = viewModel
                )
            }
            // Fallback old route
            composable("player_route/{dramaId}") { backStackEntry ->
                val dramaId = backStackEntry.arguments?.getString("dramaId") ?: ""
                PlayerScreen(
                    dramaId = dramaId,
                    initialEpisodeIndex = 0,
                    onBack = { navController.popBackStack() },
                    onLoginClick = { navController.navigate("login_route") },
                    onAllEpisodesClick = { navController.navigate("series_route/$dramaId") },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = title, color = com.example.ui.theme.WhiteText)
    }
}
