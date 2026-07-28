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
import androidx.compose.runtime.LaunchedEffect
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
import com.eyevoicecoach.domain.catalog.TrainingCatalog
import com.eyevoicecoach.domain.model.CommunicationStyle
import com.eyevoicecoach.feature.library.SituationDetailScreen
import com.eyevoicecoach.feature.library.SituationsScreen
import com.eyevoicecoach.feature.library.SpecializedDrillsScreen
import com.eyevoicecoach.feature.lifeskills.LifeLessonScreen
import com.eyevoicecoach.feature.lifeskills.LifeLessonViewModel
import com.eyevoicecoach.feature.lifeskills.LifeSkillsScreen
import com.eyevoicecoach.feature.lifeskills.LifeSkillsTrackScreen
import com.eyevoicecoach.feature.lifeskills.LifeSkillsTrackViewModel
import com.eyevoicecoach.feature.lifeskills.LifeSkillsViewModel
import com.eyevoicecoach.feature.privacy.PrivacyScreen
import com.eyevoicecoach.feature.privacy.PrivacyViewModel
import com.eyevoicecoach.feature.programs.AchievementsScreen
import com.eyevoicecoach.feature.programs.AchievementsViewModel
import com.eyevoicecoach.feature.programs.ProgramDayScreen
import com.eyevoicecoach.feature.programs.ProgramDetailScreen
import com.eyevoicecoach.feature.programs.ProgramDetailViewModel
import com.eyevoicecoach.feature.programs.ProgramsScreen
import com.eyevoicecoach.feature.programs.ProgramsViewModel
import com.eyevoicecoach.feature.home.HomeViewModel
import com.eyevoicecoach.feature.recording.RecordingScreen
import com.eyevoicecoach.feature.recording.RecordingViewModel
import com.eyevoicecoach.feature.settings.AboutScreen
import com.eyevoicecoach.feature.settings.SettingsScreen
import com.eyevoicecoach.feature.settings.SettingsViewModel
import com.eyevoicecoach.feature.statistics.StatisticsScreen
import com.eyevoicecoach.feature.statistics.StatisticsViewModel
import com.eyevoicecoach.feature.social.SocialCardViewModel
import com.eyevoicecoach.feature.social.SocialStatisticsScreen
import com.eyevoicecoach.feature.social.SocialStatisticsViewModel
import com.eyevoicecoach.feature.social.SocialTrainingCardScreen
import com.eyevoicecoach.feature.social.SocialTrainingScreen
import com.eyevoicecoach.feature.social.SocialTrainingViewModel
import com.eyevoicecoach.feature.social.CommunicationStyleGuideScreen
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
                onOpenPrograms = { navController.navigate("programs") },
                onReset = { viewModel.resetAndRefresh() },
                onRetry = { viewModel.refresh() },
            )
        }
        composable("training") {
            val viewModel: TrainingViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            TrainingScreen(state, viewModel::setCategory, viewModel::setContext, onOpen = { navController.navigate("detail/$it") }, onDrills = { navController.navigate("drills") }, onSituations = { navController.navigate("situations") }, onSocial = { navController.navigate("socialTraining") }, onLifeSkills = { navController.navigate("lifeSkills") })
        }
        composable("recordings") { RecordingDestination(canRecord = false, onBack = null) }
        composable("programs") {
            val viewModel: ProgramsViewModel = hiltViewModel()
            val items by viewModel.programs.collectAsStateWithLifecycle()
            ProgramsScreen(items, onOpen = { navController.navigate("program/$it") }, onAchievements = { navController.navigate("achievements") })
        }
        composable("statistics") {
            val viewModel: StatisticsViewModel = hiltViewModel()
            val stats by viewModel.stats.collectAsStateWithLifecycle()
            StatisticsScreen(stats, onSocialStats = { navController.navigate("socialStats") }, onLifeSkills = { navController.navigate("lifeSkills") })
        }
        composable("settings") {
            val viewModel: SettingsViewModel = hiltViewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            SettingsScreen(settings, onContext = { viewModel.selectContext(it) }, onTheme = { viewModel.selectTheme(it) }, onReminder = { enabled, hour, minute -> viewModel.setReminder(enabled, hour, minute) }, onAbout = { navController.navigate("about") }, onPrivacy = { navController.navigate("privacy") })
        }
        composable("detail/{tipId}", arguments = listOf(navArgument("tipId") { type = NavType.IntType })) {
            val viewModel: TipDetailViewModel = hiltViewModel()
            val tip by viewModel.tip.collectAsStateWithLifecycle()
            val favorite by viewModel.isFavorite.collectAsStateWithLifecycle()
            TipDetailScreen(tip, favorite, onFavorite = { viewModel.toggleFavorite() }, onRecord = { navController.navigate("recording/$it") }, onBack = navController::navigateUp)
        }
        composable("recording/{tipId}", arguments = listOf(navArgument("tipId") { type = NavType.IntType })) {
            RecordingDestination(canRecord = true, onBack = navController::navigateUp)
        }
        composable("recording/{tipId}/{programId}/{dayNumber}", arguments = listOf(navArgument("tipId") { type = NavType.IntType }, navArgument("programId") { type = NavType.StringType }, navArgument("dayNumber") { type = NavType.IntType })) {
            RecordingDestination(canRecord = true, onBack = navController::navigateUp)
        }
        composable("program/{programId}", arguments = listOf(navArgument("programId") { type = NavType.StringType })) {
            val viewModel: ProgramDetailViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            ProgramDetailScreen(state, onStart = { viewModel.start() }, onOpenDay = { day -> navController.navigate("programDay/${state.program.id}/${day.number}") }, onBack = navController::navigateUp)
        }
        composable("programDay/{programId}/{dayNumber}", arguments = listOf(navArgument("programId") { type = NavType.StringType }, navArgument("dayNumber") { type = NavType.IntType })) { entry ->
            val programId = checkNotNull(entry.arguments?.getString("programId"))
            val dayNumber = entry.arguments?.getInt("dayNumber") ?: 1
            val day = TrainingCatalog.program(programId)?.days?.firstOrNull { it.number == dayNumber }
            if (day == null) {
                EmptyState("⌕", "اليوم غير متاح", "عد إلى البرنامج واختر يوماً متاحاً.")
            } else {
                ProgramDayScreen(day, onRecord = { navController.navigate("recording/${day.tipId}/$programId/${day.number}") }, onBack = navController::navigateUp)
            }
        }
        composable("achievements") {
            val viewModel: AchievementsViewModel = hiltViewModel()
            val items by viewModel.achievements.collectAsStateWithLifecycle()
            AchievementsScreen(items, onBack = navController::navigateUp)
        }
        composable("lifeSkills") {
            val viewModel: LifeSkillsViewModel = hiltViewModel()
            val progress by viewModel.progress.collectAsStateWithLifecycle()
            LifeSkillsScreen(viewModel.tracks, progress, onOpenTrack = { navController.navigate("lifeTrack/$it") }, onBack = navController::navigateUp)
        }
        composable("lifeTrack/{lifeTrackId}", arguments = listOf(navArgument("lifeTrackId") { type = NavType.StringType })) {
            val viewModel: LifeSkillsTrackViewModel = hiltViewModel()
            val progress by viewModel.progress.collectAsStateWithLifecycle()
            LifeSkillsTrackScreen(viewModel.track, progress, onOpenLesson = { navController.navigate("lifeLesson/$it") }, onBack = navController::navigateUp)
        }
        composable("lifeLesson/{lifeLessonId}", arguments = listOf(navArgument("lifeLessonId") { type = NavType.StringType })) {
            val viewModel: LifeLessonViewModel = hiltViewModel()
            val progress by viewModel.progress.collectAsStateWithLifecycle()
            LifeLessonScreen(viewModel.track, viewModel.lesson, progress, onComplete = { reflection, option -> viewModel.complete(reflection, option) }, onBack = navController::navigateUp)
        }
        composable("socialTraining") { entry ->
            val viewModel: SocialTrainingViewModel = hiltViewModel()
            val selectedStyleCode by entry.savedStateHandle.getStateFlow("social_style", "").collectAsStateWithLifecycle()
            LaunchedEffect(selectedStyleCode) {
                if (selectedStyleCode.isNotBlank()) viewModel.selectStyle(CommunicationStyle.fromCode(selectedStyleCode))
            }
            val settings by viewModel.ethicsAccepted.collectAsStateWithLifecycle()
            val boost by viewModel.boost.collectAsStateWithLifecycle()
            val content by viewModel.content.collectAsStateWithLifecycle()
            val selectedStyle by viewModel.selectedStyle.collectAsStateWithLifecycle()
            SocialTrainingScreen(settings, boost, content, selectedStyle, onScenario = viewModel::selectScenario, onGoal = viewModel::selectGoal, onStyle = viewModel::selectStyle, onTrack = viewModel::selectTrack, onRefreshBoost = { viewModel.refreshBoost() }, onAcknowledgeEthics = { viewModel.acknowledgeEthics() }, onOpenCard = { contentId, selectedStyle -> navController.navigate("socialCard/$contentId/${selectedStyle?.code ?: "unknown"}") }, onStyleAssistant = { navController.navigate("communicationStyles") }, onBack = navController::navigateUp)
        }
        composable("socialCard/{socialContentId}/{socialStyle}", arguments = listOf(navArgument("socialContentId") { type = NavType.IntType }, navArgument("socialStyle") { type = NavType.StringType })) { entry ->
            val viewModel: SocialCardViewModel = hiltViewModel()
            val content by viewModel.content.collectAsStateWithLifecycle()
            val choice by viewModel.choice.collectAsStateWithLifecycle()
            val selectedStyle = CommunicationStyle.fromCode(entry.arguments?.getString("socialStyle"))
            SocialTrainingCardScreen(content, choice, selectedStyle, onRecord = { contentId, style ->
                content?.let { current -> navController.navigate("socialRecording/${((current.id - 2001) % 18) + 1}/$contentId/${style.code}") }
            }, onBack = navController::navigateUp)
        }
        composable("socialRecording/{tipId}/{socialContentId}/{socialStyle}", arguments = listOf(navArgument("tipId") { type = NavType.IntType }, navArgument("socialContentId") { type = NavType.IntType }, navArgument("socialStyle") { type = NavType.StringType })) {
            RecordingDestination(canRecord = true, onBack = navController::navigateUp)
        }
        composable("communicationStyles") { CommunicationStyleGuideScreen(onSelect = { style -> navController.previousBackStackEntry?.savedStateHandle?.set("social_style", style.code); navController.navigateUp() }, onBack = navController::navigateUp) }
        composable("socialStats") {
            val viewModel: SocialStatisticsViewModel = hiltViewModel()
            val stats by viewModel.stats.collectAsStateWithLifecycle()
            SocialStatisticsScreen(stats, onBack = navController::navigateUp)
        }
        composable("drills") { SpecializedDrillsScreen(onRecord = { navController.navigate("recording/$it") }, onBack = navController::navigateUp) }
        composable("situations") { SituationsScreen(onOpen = { navController.navigate("situation/$it") }, onBack = navController::navigateUp) }
        composable("situation/{situationId}", arguments = listOf(navArgument("situationId") { type = NavType.StringType })) { entry ->
            val id = entry.arguments?.getString("situationId")
            SituationDetailScreen(TrainingCatalog.situations.firstOrNull { it.id == id }, onRecord = { navController.navigate("recording/$it") }, onBack = navController::navigateUp)
        }
        composable("privacy") {
            val viewModel: PrivacyViewModel = hiltViewModel()
            val message by viewModel.message.collectAsStateWithLifecycle()
            PrivacyScreen(message, onDeleteRecordings = { viewModel.deleteRecordings() }, onDeleteHistory = { viewModel.deleteHistory() }, onDeleteAssessments = { viewModel.deleteAssessments() }, onDeleteSocialTraining = { viewModel.deleteSocialTraining() }, onDeleteLifeSkills = { viewModel.deleteLifeSkillsData() }, onResetAll = { viewModel.resetApplication() }, onClearMessage = { viewModel.clearMessage() }, onBack = navController::navigateUp)
        }
        composable("favorites") {
            val viewModel: FavoritesViewModel = hiltViewModel()
            val favorites by viewModel.favorites.collectAsStateWithLifecycle()
            FavoritesScreen(favorites, onOpen = { navController.navigate("detail/$it") }, onBack = navController::navigateUp)
        }
        composable("about") { AboutScreen(onBack = navController::navigateUp) }
    }
}

/** Connects a navigation recording destination to its Hilt-backed session state. */
@Composable
private fun RecordingDestination(canRecord: Boolean, onBack: (() -> Unit)?) {
    val viewModel: RecordingViewModel = hiltViewModel()
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val amplitudes by viewModel.amplitudes.collectAsStateWithLifecycle()
    val socialRoleplayPrompt by viewModel.socialRoleplayPrompt.collectAsStateWithLifecycle()
    val pending by viewModel.pendingAssessment.collectAsStateWithLifecycle()
    val socialSummary by viewModel.socialSummary.collectAsStateWithLifecycle()
    RecordingScreen(canRecord, recordings, playback, amplitudes, socialRoleplayPrompt, onStart = viewModel::startRecording, onStop = { viewModel.stopRecording() }, onCancel = { viewModel.cancelRecording() }, onTogglePlayback = { viewModel.togglePlayback(it) }, onSeekBy = { viewModel.seekBy(it) }, onSeekTo = { viewModel.seekTo(it) }, onDelete = { viewModel.delete(it) }, pendingAssessment = pending, socialSummary = socialSummary, onDismissSocialSummary = { viewModel.dismissSocialSummary() }, onSaveAssessment = { clarity, pace, confidence, pauses, tension, note, respect, listening, calmness -> viewModel.saveAssessment(clarity, pace, confidence, pauses, tension, note, respect, listening, calmness) }, onDismissAssessment = { viewModel.dismissAssessment() }, onBack = onBack)
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
