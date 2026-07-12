package vn.phuclh.myapplication.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import vn.phuclh.myapplication.presentation.articles.ArticlesScreen
import vn.phuclh.myapplication.presentation.bookmarks.BookmarkScreen
import vn.phuclh.myapplication.presentation.detail.DetailScreen
import vn.phuclh.myapplication.util.decodeUrl
import vn.phuclh.myapplication.util.encodeUrl

sealed class Screen(
    val route: String,
) {
    data object Articles : Screen("articles")

    data object Bookmarks : Screen("bookmarks")

    data object Detail : Screen("detail/{articleUrl}") {
        fun createRoute(url: String) = "detail/${encodeUrl(url)}"
    }
}

private data class BottomTab(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

private val bottomTabs =
    listOf(
        BottomTab(Screen.Articles, "Articles", Icons.Filled.Newspaper),
        BottomTab(Screen.Bookmarks, "Bookmarks", Icons.Filled.Bookmark),
    )

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomTabs.any { it.screen.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { _ ->
        NavHost(navController = navController, startDestination = Screen.Articles.route) {
            composable(Screen.Articles.route) {
                ArticlesScreen(onArticleClick = { navController.navigate(Screen.Detail.createRoute(it)) })
            }
            composable(Screen.Bookmarks.route) {
                BookmarkScreen(onArticleClick = { navController.navigate(Screen.Detail.createRoute(it)) })
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("articleUrl") { type = NavType.StringType }),
            ) { backStackEntry ->
                val url = decodeUrl(backStackEntry.arguments?.getString("articleUrl") ?: "")
                DetailScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = koinViewModel { parametersOf(url) },
                )
            }
        }
    }
}
