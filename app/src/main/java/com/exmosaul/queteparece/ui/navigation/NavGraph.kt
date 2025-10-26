package com.exmosaul.queteparece.ui.navigation


import FavoritesScreen
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exmosaul.queteparece.ui.screens.auth.AuthScreen
import com.exmosaul.queteparece.ui.screens.auth.HomeScreen
import com.exmosaul.queteparece.ui.screens.search.SearchScreen
import androidx.compose.runtime.getValue
import com.exmosaul.queteparece.ui.screens.profile.ProfileScreen
import com.google.firebase.auth.FirebaseAuth


sealed class Routes(val route: String) {
    data object Auth: Routes("auth")
    data object Home: Routes("home")
    data object Search: Routes("search")
    data object Favorites: Routes("favorites")
    data object Profile: Routes("profile")
}


@Composable
fun AppNavHost(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser != null) {
        Routes.Home.route
    } else {
        Routes.Auth.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Auth.route) {
            AuthScreen(onAuthenticated = {
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Auth.route) { inclusive = true }
                }
            })
        }
        composable(Routes.Home.route) {
            HomeScreen(navController)
        }
        composable(Routes.Search.route) {
            SearchScreen(navController)
        }
        composable(Routes.Favorites.route) {
            FavoritesScreen(navController)
        }
        composable(Routes.Profile.route) {
            ProfileScreen(navController)
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Inicio", Icons.Filled.Home, Routes.Home.route),
        BottomNavItem("Buscar", Icons.Filled.Search, Routes.Search.route),
        BottomNavItem("Favoritos", Icons.Filled.Favorite, Routes.Favorites.route),
        BottomNavItem("Perfil", Icons.Filled.Person, Routes.Profile.route)
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Routes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = if (item.label == "Perfil") Modifier.clip(CircleShape) else Modifier
                    )
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

