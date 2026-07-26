package com.eyevoicecoach.feature.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.eyevoicecoach.core.ui.EmptyState
import com.eyevoicecoach.feature.home.HomeScreen
import com.eyevoicecoach.feature.home.HomeViewModel
import com.eyevoicecoach.feature.recording.RecordingScreen
import com.eyevoicecoach.feature.recording.RecordingViewModel
import com.eyevoicecoach.feature.settings.AboutScreen
import com.eyevoicecoach.feature.settings.SettingsScreen
import com.eyevoicecoach.feature.settings.SettingsViewModel
import com.eyevoicecoach.feature.statistics.StatisticsScreen
import com.eyevoicecoach.feature.statistics.StatisticsViewModel
import com.eyevoicecoach.feature.training.FavoritesScreen
import com.eyevoicecoach.feature.training.FavoritesViewModel
import com.eyevoicecoach.feature.training.TipDetailScreen
import com.eyevoicecoach.feature.training.TipDetailViewModel
import com.eyevoicecoach.feature.training.TrainingScreen
import com.eyevoicecoach.feature.training.TrainingViewModel

private data class BottomDestination(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val destinations = listOf(
    BottomDestination("home", "الرئيسية", Icons.Rounded.Home),
    BottomDestination("training", "التمارين", Icons.Rounded.SelfImprovement),
    BottomDestination("recordings", "التسجيلات", Icons.Rounded.Mic),
    BottomDestination("statistics", "الإحصائيات", Icons.Rounded.BarChart),
    BottomDestination("settings", "الإعدادات", Icons.Rounded.Settings),
)

/** Decides between the private introduction, a progress screen and the main navigation graph. */
@Composable
fun CoachRoot(state: MainUiState, onFinishOnboarding: () -> Unit) {
    AnimatedContent(targetState = state.isLoading to state.settings.isOnboardingComplete, label = "root") { (loading, onboarded) ->
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            !onboarded -> OnboardingScreen(onContinue = onFinishOnboarding)
            else -> MainNavigation(error = state.error)
        }
    }
}

/** Root navigation scaffold with RTL-friendly bottom destinations. */
@Composable
private fun MainNavigation(error: String?) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    Scaffold(
        bottomBar = {
            if (route in destinations.map(BottomDestination::route)) {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = route == destination.route,
                            onClick = { navController.navigateRoot(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (error != null) {
            EmptyState("⚠️", "حدثت مشكلة في التجهيز", error, Modifier.padding(padding))
        } else {
            CoachNavHost(navController, Modifier.padding(padding))
        }
    }
}

/** App destinations including exercise details, favorites, recording and about screens. */
@Composable
private fun CoachNavHost(navController: NavHostController, modifier: Modifier) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") {
            val viewModel: HomeViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                state = state,
                onOpenDetail = { navController.navigate("detail/$it") },
                onRecord = { navController.navigate("recording/$it") },
                onOpenFavorites = { navController.navigate("favorites") },
                onReset = viewModel::resetAndRefresh,
                onRetry = viewModel::refresh,
            )
        }
        composable("training") {
            val viewModel: TrainingViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            TrainingScreen(state, viewModel::setCategory, viewModel::setContext) { navController.navigate("detail/$it") }
        }
        composable("recordings") {
            val viewModel: RecordingViewModel = hiltViewModel()
            val recordings by viewModel.recordings.collectAsStateWithLifecycle()
            val playback by viewModel.playback.collectAsStateWithLifecycle()
            val amplitudes by viewModel.amplitudes.collectAsStateWithLifecycle()
            RecordingScreen(false, recordings, playback, amplitudes, viewModel::startRecording, viewModel::stopRecording, viewModel::cancelRecording, viewModel::togglePlayback, viewModel::seekBy, viewModel::seekTo, viewModel::delete, onBack = null)
        }
        composable("statistics") {
            val viewModel: StatisticsViewModel = hiltViewModel()
            val stats by viewModel.stats.collectAsStateWithLifecycle()
            StatisticsScreen(stats)
        }
        composable("settings") {
            val viewModel: SettingsViewModel = hiltViewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            SettingsScreen(settings, viewModel::selectContext, viewModel::selectTheme, viewModel::setReminder, onAbout = { navController.navigate("about") })
        }
        composable("detail/{tipId}", arguments = listOf(navArgument("tipId") { type = NavType.IntType })) {
            val viewModel: TipDetailViewModel = hiltViewModel()
            val tip by viewModel.tip.collectAsStateWithLifecycle()
            val favorite by viewModel.isFavorite.collectAsStateWithLifecycle()
            TipDetailScreen(tip, favorite, viewModel::toggleFavorite, onRecord = { navController.navigate("recording/$it") }, onBack = navController::navigateUp)
        }
        composable("recording/{tipId}", arguments = listOf(navArgument("tipId") { type = NavType.IntType })) {
            val viewModel: RecordingViewModel = hiltViewModel()
            val recordings by viewModel.recordings.collectAsStateWithLifecycle()
            val playback by viewModel.playback.collectAsStateWithLifecycle()
            val amplitudes by viewModel.amplitudes.collectAsStateWithLifecycle()
            RecordingScreen(true, recordings, playback, amplitudes, viewModel::startRecording, viewModel::stopRecording, viewModel::cancelRecording, viewModel::togglePlayback, viewModel::seekBy, viewModel::seekTo, viewModel::delete, onBack = navController::navigateUp)
        }
        composable("favorites") {
            val viewModel: FavoritesViewModel = hiltViewModel()
            val favorites by viewModel.favorites.collectAsStateWithLifecycle()
            FavoritesScreen(favorites, onOpen = { navController.navigate("detail/$it") }, onBack = navController::navigateUp)
        }
        composable("about") { AboutScreen(onBack = navController::navigateUp) }
    }
}

private fun NavHostController.navigateRoot(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Concise one-screen Arabic introduction to the offline and private coaching experience. */
@Composable
private fun OnboardingScreen(onContinue: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("◉", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
        Text("مدرب العين والجسد والصوت", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("تدريب يومي هادئ يساعدك على حضور أوضح، وحركة أكثر ثقة، وصوت متزن.", modifier = Modifier.padding(top = 18.dp), style = MaterialTheme.typography.bodyLarge)
        Text("خصوصيتك أولاً: كل البيانات والتسجيلات تبقى على جهازك، وتُحذف التسجيلات تلقائياً بعد ٧ أيام.", modifier = Modifier.padding(top = 14.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.material3.Button(onClick = onContinue, modifier = Modifier.padding(top = 28.dp)) { Text("ابدأ رحلتك") }
    }
}
