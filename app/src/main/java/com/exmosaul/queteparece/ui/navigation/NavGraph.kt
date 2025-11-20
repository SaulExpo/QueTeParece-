package com.exmosaul.queteparece.ui.navigation


import FavoritesScreen
import SearchViewAllScreen
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.exmosaul.queteparece.ui.screens.actor.ActorDetailScreen
import com.exmosaul.queteparece.ui.screens.auth.AuthScreen
import com.exmosaul.queteparece.ui.screens.detail.MovieDetailScreen
import com.exmosaul.queteparece.ui.screens.home.HomeScreen
import com.exmosaul.queteparece.ui.screens.home.MovieListScreen
import com.exmosaul.queteparece.ui.screens.profile.EditProfileScreen
import com.exmosaul.queteparece.ui.screens.profile.EditRecommendationsScreen
import com.exmosaul.queteparece.ui.screens.profile.FriendProfileScreen
import com.exmosaul.queteparece.ui.screens.profile.FriendRequestsScreen
import com.exmosaul.queteparece.ui.screens.profile.ProfileScreen
import com.exmosaul.queteparece.ui.screens.profile.SearchFriendsScreen
import com.exmosaul.queteparece.ui.screens.search.SearchScreen
import com.google.firebase.auth.FirebaseAuth


sealed class Routes(val route: String) {
    data object Auth: Routes("auth")
    data object Home: Routes("home")
    data object Search: Routes("search")
    data object Favorites: Routes("favorites")
    data object Profile: Routes("profile")
    data object MovieDetail: Routes("movieDetail/{movieId}")
    data object ActorDetail : Routes("actorDetail/{actorId}")
    data object EditProfile : Routes("editProfile")
    data object Friends : Routes("friends")
    data object EditRecommendations : Routes("edit_recommendations")
    object FriendProfile : Routes("friendProfile/{friendId}") {
        fun create(friendId: String) = "friendProfile/$friendId"
    }
    data object FriendRequests : Routes("friendRequests")
    data object SearchFriends : Routes("searchFriends")

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
        composable(
            route = "movieDetail/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.StringType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
            MovieDetailScreen(movieId = movieId, navController = navController)
        }
        composable(
            route = "actorDetail/{actorId}",
            arguments = listOf(navArgument("actorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val actorId = backStackEntry.arguments?.getString("actorId") ?: ""
            ActorDetailScreen(actorId = actorId, navController = navController)
        }
        composable(Routes.EditProfile.route) {
            EditProfileScreen(navController)
        }
        composable(Routes.EditRecommendations.route) {
            EditRecommendationsScreen(navController)
        }
        composable(
            route = Routes.FriendProfile.route,
            arguments = listOf(navArgument("friendId") { type = NavType.StringType })
        ) { backStackEntry ->
            val friendId = backStackEntry.arguments?.getString("friendId")!!
            FriendProfileScreen(navController, friendId)
        }
        composable(Routes.FriendRequests.route) {
            FriendRequestsScreen(navController)
        }
        composable(Routes.SearchFriends.route) {
            SearchFriendsScreen(navController)
        }
        composable("movieList/{title}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: ""
            MovieListScreen(navController, title)
        }
        composable("searchViewAll") {
            SearchViewAllScreen(navController)
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()

    NavigationBar(containerColor = MaterialTheme.colorScheme.primary) {
        items.forEach { item ->

            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(item.route) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
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
