package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.launchVideoPlayer
import com.example.ui.components.launchVideoShare
import com.example.ui.screens.AccountPlansScreen
import com.example.ui.screens.ClipEditorScreen
import com.example.ui.screens.CreateStudioScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExportsHistoryScreen
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.ViralClipTheme
import com.example.ui.theme.VoidBlack
import com.example.viewmodel.ViralClipViewModel

private object AppRoutes {
    const val CREATE = "create"
    const val PROJECTS = "projects"
    const val EXPORTS = "exports"
    const val ACCOUNT = "account"
    const val EDITOR = "editor"
}

private data class TopNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val TopNavItems = listOf(
    TopNavItem(AppRoutes.CREATE, R.string.nav_create, Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome),
    TopNavItem(AppRoutes.PROJECTS, R.string.nav_projects, Icons.Rounded.VideoLibrary, Icons.Outlined.VideoLibrary),
    TopNavItem(AppRoutes.EXPORTS, R.string.nav_exports, Icons.Rounded.DownloadDone, Icons.Outlined.DownloadDone),
    TopNavItem(AppRoutes.ACCOUNT, R.string.nav_account, Icons.Rounded.Person, Icons.Outlined.Person)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            ViralClipTheme {
                ViralClipApp()
            }
        }
    }
}

@Composable
fun ViralClipApp(
    viewModel: ViralClipViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoutes.CREATE

    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
    val selectedProjectClips by viewModel.selectedProjectClips.collectAsStateWithLifecycle()
    val editingClip by viewModel.editingClip.collectAsStateWithLifecycle()
    val exportHistory by viewModel.exportHistory.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val processingProgress by viewModel.processingProgress.collectAsStateWithLifecycle()
    val exportProgress by viewModel.exportProgress.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    LaunchedEffect(snackbarMessage) {
        val message = snackbarMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeSnackbar()
        }
    }

    val navigateTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val isTopLevelRoute = currentRoute != AppRoutes.EDITOR

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        val useNavRail = maxWidth >= 700.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = VoidBlack,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (isTopLevelRoute && !useNavRail) {
                    NavigationBar(
                        containerColor = ObsidianSurface,
                        contentColor = TextPrimary
                    ) {
                        TopNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                modifier = Modifier.testTag("nav_${item.route}"),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(item.labelRes)
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonBlue,
                                    selectedTextColor = NeonBlue,
                                    indicatorColor = DeepViolet.copy(alpha = 0.35f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevelRoute && useNavRail) {
                    NavigationRail(
                        containerColor = ObsidianSurface,
                        contentColor = TextPrimary,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        TopNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(item.route) },
                                modifier = Modifier.testTag("rail_${item.route}"),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = stringResource(item.labelRes)
                                    )
                                },
                                label = { Text(stringResource(item.labelRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = NeonBlue,
                                    selectedTextColor = NeonBlue,
                                    indicatorColor = DeepViolet.copy(alpha = 0.35f),
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                )
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.CREATE
                    ) {
                        composable(AppRoutes.CREATE) {
                            CreateStudioScreen(
                                importState = importState,
                                processingProgress = processingProgress,
                                userSettings = userSettings,
                                isSampleAiMode = viewModel.isSampleAiMode,
                                onSelectSourceType = viewModel::selectSourceType,
                                onYouTubeUrlChange = viewModel::updateYouTubeUrl,
                                onFetchYouTube = viewModel::fetchYouTubeMetadata,
                                onLocalVideoPicked = viewModel::onLocalVideoPicked,
                                onClipCountChange = viewModel::updateRequestedClipCount,
                                onDurationSecChange = viewModel::updateTargetDurationSec,
                                onToggleMoment = viewModel::toggleMomentType,
                                onLanguageChange = viewModel::updateImportCaptionLanguage,
                                onPlatformChange = viewModel::updateImportPlatform,
                                onGenerateClips = {
                                    viewModel.generateViralClips { projectId ->
                                        viewModel.selectProject(projectId)
                                        navigateTopLevel(AppRoutes.PROJECTS)
                                    }
                                },
                                onOpenPlans = { navigateTopLevel(AppRoutes.ACCOUNT) }
                            )
                        }

                        composable(AppRoutes.PROJECTS) {
                            DashboardScreen(
                                projects = projects,
                                selectedProject = selectedProject,
                                selectedProjectClips = selectedProjectClips,
                                exportProgress = exportProgress,
                                isSampleAiMode = viewModel.isSampleAiMode,
                                onSelectProject = viewModel::selectProject,
                                onDeleteProject = viewModel::deleteProject,
                                onAttachLocalVideo = viewModel::attachLocalVideoToProject,
                                onEditClip = { projectId, clipId ->
                                    viewModel.openClipEditor(projectId, clipId)
                                    navController.navigate(AppRoutes.EDITOR)
                                },
                                onExportClip = { clip -> viewModel.exportSingleClip(clip) },
                                onExportAllClips = viewModel::exportAllClipsForProject,
                                onPlayExported = { uri, path ->
                                    if (!launchVideoPlayer(context, uri, path)) {
                                        viewModel.showMessage("No external MP4 player found on device.")
                                    }
                                },
                                onShareExported = { uri, path ->
                                    if (!launchVideoShare(context, uri, path, "Viral Clip AI Short")) {
                                        viewModel.showMessage("Unable to open share sheet for this MP4.")
                                    }
                                },
                                onDismissExportBanner = viewModel::dismissExportBanner,
                                onNavigateToCreate = { navigateTopLevel(AppRoutes.CREATE) }
                            )
                        }

                        composable(AppRoutes.EDITOR) {
                            ClipEditorScreen(
                                project = selectedProject,
                                clip = editingClip,
                                fullHdExport = userSettings.fullHdExport,
                                exportProgress = exportProgress,
                                isSampleAiMode = viewModel.isSampleAiMode,
                                onBack = { navController.popBackStack() },
                                onAttachLocalVideo = viewModel::attachLocalVideoToProject,
                                onUpdateClip = viewModel::updateClip,
                                onUpdateCueText = viewModel::updateSubtitleCueText,
                                onExportClip = { clip, platform ->
                                    viewModel.exportSingleClip(clip, overridePlatform = platform)
                                },
                                onPlayExported = { uri, path ->
                                    if (!launchVideoPlayer(context, uri, path)) {
                                        viewModel.showMessage("No external MP4 player found on device.")
                                    }
                                },
                                onShareExported = { uri, path ->
                                    if (!launchVideoShare(context, uri, path, editingClip?.title ?: "Viral Clip")) {
                                        viewModel.showMessage("Unable to open share sheet for this MP4.")
                                    }
                                },
                                onDismissExportBanner = viewModel::dismissExportBanner
                            )
                        }

                        composable(AppRoutes.EXPORTS) {
                            ExportsHistoryScreen(
                                exports = exportHistory,
                                onPlayExport = { item ->
                                    if (!launchVideoPlayer(context, item.mediaStoreUri, item.filePath)) {
                                        viewModel.showMessage("No external MP4 player found on device.")
                                    }
                                },
                                onShareExport = { item ->
                                    if (!launchVideoShare(context, item.mediaStoreUri, item.filePath, item.clipTitle)) {
                                        viewModel.showMessage("Unable to open share sheet for this MP4.")
                                    }
                                },
                                onDeleteExport = viewModel::deleteExportItem,
                                onClearAll = viewModel::clearAllExports
                            )
                        }

                        composable(AppRoutes.ACCOUNT) {
                            AccountPlansScreen(
                                settings = userSettings,
                                isSampleAiMode = viewModel.isSampleAiMode,
                                onUpdateProfile = viewModel::updateUserProfile,
                                onSelectPlan = viewModel::switchSubscriptionPlan,
                                onResetMonthlyUsage = viewModel::resetMonthlyUsage,
                                onUpdateDefaultLanguage = viewModel::updateDefaultLanguage,
                                onUpdateDefaultPlatform = viewModel::updateDefaultPlatform,
                                onToggleFullHd = viewModel::updateFullHdExport,
                                onToggleAutoSilence = viewModel::updateAutoRemoveSilence,
                                onToggleEmojiDefault = viewModel::updateDefaultEmojiCaptions
                            )
                        }
                    }
                }
            }
        }
    }
}
