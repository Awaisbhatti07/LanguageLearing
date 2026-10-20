package com.lingomate.ai.core.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lingomate.ai.R
import com.lingomate.ai.core.designsystem.InfoCard
import com.lingomate.ai.core.designsystem.ScreenColumn
import com.lingomate.ai.core.designsystem.SectionTitle
import com.lingomate.ai.feature.courses.CourseScreen
import com.lingomate.ai.feature.courses.LearnScreen
import com.lingomate.ai.feature.courses.LearningViewModel
import com.lingomate.ai.feature.lessons.LessonScreen
import com.lingomate.ai.feature.lessons.LessonViewModel
import com.lingomate.ai.feature.revision.ReviewScreen
import com.lingomate.ai.data.LearningRepository
import com.lingomate.ai.feature.home.HomeScreen
import com.lingomate.ai.feature.onboarding.OnboardingScreen
import com.lingomate.ai.feature.settings.AppState
import com.lingomate.ai.feature.settings.AppViewModel
import com.lingomate.ai.feature.settings.SettingsScreen
import com.lingomate.learning.LearningTrack

enum class MainDestination(val route: String, val label: String, @DrawableRes val icon: Int) {
    HOME("home", "Home", R.drawable.ic_home),
    LEARN("learn", "Learn", R.drawable.ic_book),
    SPEAK("speak", "Speak", R.drawable.ic_chat),
    REVIEW("review", "Review", R.drawable.ic_review),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LingoMateRoot(state: AppState, model: AppViewModel, repository: LearningRepository) {
    val saving by model.saving.collectAsStateWithLifecycle()
    val writeError by model.writeError.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        val settings = state.settings
        if (settings == null) {
            Scaffold { padding ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    if (state.loadError) {
                        ScreenColumn {
                            InfoCard("Settings could not be read", "Your saved data has not been reset. Try reading it again.", "Try again", model::retry)
                        }
                    } else CircularProgressIndicator()
                }
            }
            return@CompositionLocalProvider
        }
        if (!settings.onboardingComplete) {
            Scaffold { padding ->
                Box(Modifier.padding(padding)) { OnboardingScreen(saving, model::finishOnboarding) }
            }
        } else {
            val learningModel: LearningViewModel = viewModel(factory = viewModelFactory { initializer { LearningViewModel(repository) } })
            val learning by learningModel.state.collectAsStateWithLifecycle()
            val nav = rememberNavController()
            val entry by nav.currentBackStackEntryAsState()
            val currentRoute = entry?.destination?.route ?: "home"
            val main = MainDestination.entries.firstOrNull { it.route == currentRoute }
            val onTrack: (LearningTrack) -> Unit = { nav.navigate("course/${it.name}") { launchSingleTop = true } }
            val onLesson: (String) -> Unit = { nav.navigate("lesson/$it") { launchSingleTop = true } }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(main?.let { if (it == MainDestination.HOME) "LingoMate AI" else it.label } ?: if (currentRoute == "settings") "Settings" else "Your course") },
                        navigationIcon = {
                            if (main == null) IconButton(onClick = { nav.popBackStack() }) {
                                Icon(painterResource(R.drawable.ic_back), "Back")
                            }
                        },
                        actions = {
                            if (currentRoute != "settings") IconButton(onClick = { nav.navigate("settings") { launchSingleTop = true } }) {
                                Icon(painterResource(R.drawable.ic_settings), "Settings")
                            }
                        },
                    )
                },
                bottomBar = {
                    if (main != null) NavigationBar {
                        MainDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = main == destination,
                                onClick = {
                                    nav.navigate(destination.route) {
                                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                                label = { Text(destination.label) },
                            )
                        }
                    }
                },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    NavHost(navController = nav, startDestination = "home") {
                        composable("home") { HomeScreen(settings, learning, learningModel::retry, onTrack) }
                        composable("learn") { LearnScreen(learning, learningModel::retry, onTrack) }
                        composable("speak") {
                            ScreenColumn {
                                SectionTitle("Build your speaking confidence", "Listen, repeat, and take your time.")
                                InfoCard("AI teacher is not available yet", "Push-to-talk and live AI conversation are planned. A secure online provider must be configured first. No microphone or paid service is active.")
                                InfoCard("Start with a lesson", "Build vocabulary and practise examples before a conversation.", "Explore lessons", { nav.navigate("learn") { launchSingleTop = true } })
                            }
                        }
                        composable("review") { ReviewScreen(learning, learningModel::retry, { nav.navigate("learn") { launchSingleTop = true } }, onLesson) }
                        composable("settings") { SettingsScreen(settings, saving, model::setTheme, model::setGoal) }
                        composable("course/{track}", arguments = listOf(navArgument("track") { type = NavType.StringType })) { backStack ->
                            val track = LearningTrack.entries.firstOrNull { it.name == backStack.arguments?.getString("track") }
                            if (track != null) CourseScreen(track, learning, learningModel::retry, onLesson)
                            else ScreenColumn { InfoCard("Course unavailable", "Return to Learn and choose a learning track.") }
                        }
                        composable("lesson/{lessonId}", arguments = listOf(navArgument("lessonId") { type = NavType.StringType })) { backStack ->
                            val lessonId = requireNotNull(backStack.arguments?.getString("lessonId"))
                            val lessonModel: LessonViewModel = viewModel(factory = viewModelFactory {
                                initializer { LessonViewModel(repository, lessonId, createSavedStateHandle()) }
                            })
                            LessonScreen(lessonModel, onBackToCourse = { nav.popBackStack() })
                        }
                    }
                }
            }
        }
        if (writeError) AlertDialog(
            onDismissRequest = model::dismissError,
            title = { Text("Could not save settings") },
            text = { Text("Your previous settings are still in place. Try your change again.") },
            confirmButton = { TextButton(onClick = model::dismissError) { Text("OK") } },
        )
    }
}
