package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.InstallApkModalBottomSheet
import com.example.ui.components.QuranHeader
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import kotlinx.coroutines.flow.collectLatest

enum class ScreenTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HALAQAT("الحلقات", Icons.Default.School),
    ATTENDANCE("التحضير", Icons.Default.FactCheck),
    PARENT_PORTAL("بوابة الولي", Icons.Default.FamilyRestroom),
    MESSAGES("المحادثات", Icons.Default.Forum),
    TOOLS("الأدوات والتحميل", Icons.Default.Build),
    DEVELOPER("المطور", Icons.Default.AdminPanelSettings)
}

@Composable
fun MainScreen(viewModel: QuranViewModel) {
    val isAuthGatePassed by viewModel.isAuthGatePassed.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()
    val isFullscreen by viewModel.isFullscreen.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    var currentTab by remember { mutableStateOf(ScreenTab.HALAQAT) }
    var showInstallApkSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.toastMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Gate Screen: If not authenticated or passed gate, show AuthProtectionScreen
    if (!isAuthGatePassed) {
        Box(modifier = Modifier.fillMaxSize()) {
            AuthProtectionScreen(viewModel = viewModel)
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            )
        }
        return
    }

    LaunchedEffect(currentRole) {
        if (currentRole == UserRole.PARENT && currentTab == ScreenTab.HALAQAT) {
            currentTab = ScreenTab.PARENT_PORTAL
        } else if ((currentRole == UserRole.DEVELOPER || currentRole == UserRole.SUPERVISOR) && currentTab == ScreenTab.PARENT_PORTAL) {
            currentTab = ScreenTab.DEVELOPER
        } else if (currentRole == UserRole.TEACHER && currentTab == ScreenTab.PARENT_PORTAL) {
            currentTab = ScreenTab.HALAQAT
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            QuranHeader(
                currentRole = currentRole,
                syncStatus = syncStatus,
                isNightMode = isNightMode,
                isFullscreen = isFullscreen,
                currentLanguage = currentLanguage,
                onRoleChange = { newRole ->
                    viewModel.setRole(newRole)
                },
                onSyncClick = {
                    viewModel.triggerSyncNow()
                },
                onNightModeToggle = {
                    viewModel.toggleNightMode()
                },
                onLanguageToggle = {
                    viewModel.toggleLanguage()
                },
                onFullscreenToggle = {
                    viewModel.toggleFullscreen()
                },
                onDownloadApkClick = {
                    showInstallApkSheet = true
                },
                onLockClick = {
                    viewModel.lockToAuthGate()
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val availableTabs = if (currentRole == UserRole.DEVELOPER || currentRole == UserRole.SUPERVISOR) {
                    listOf(
                        ScreenTab.HALAQAT,
                        ScreenTab.ATTENDANCE,
                        ScreenTab.PARENT_PORTAL,
                        ScreenTab.MESSAGES,
                        ScreenTab.TOOLS,
                        ScreenTab.DEVELOPER
                    )
                } else {
                    listOf(
                        ScreenTab.HALAQAT,
                        ScreenTab.ATTENDANCE,
                        ScreenTab.PARENT_PORTAL,
                        ScreenTab.MESSAGES,
                        ScreenTab.TOOLS
                    )
                }

                availableTabs.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) EmeraldPrimary else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) EmeraldPrimary else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_${tab.name}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.HALAQAT -> HalaqatScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { studentId ->
                        viewModel.setSelectedChatStudentId(studentId)
                        currentTab = ScreenTab.MESSAGES
                    }
                )
                ScreenTab.ATTENDANCE -> QuickAttendanceScreen(viewModel = viewModel)
                ScreenTab.PARENT_PORTAL -> ParentPortalScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { studentId ->
                        viewModel.setSelectedChatStudentId(studentId)
                        currentTab = ScreenTab.MESSAGES
                    }
                )
                ScreenTab.MESSAGES -> MessagesScreen(viewModel = viewModel)
                ScreenTab.TOOLS -> ToolsScreen(viewModel = viewModel)
                ScreenTab.DEVELOPER -> DeveloperScreen(viewModel = viewModel)
            }

            // Floating Exit Fullscreen Button when Fullscreen is active
            AnimatedVisibility(
                visible = isFullscreen,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                FilledTonalButton(
                    onClick = { viewModel.toggleFullscreen() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = EmeraldDark.copy(alpha = 0.9f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("exit_fullscreen_floating_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "خروج من ملء الشاشة",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("خروج من ملء الشاشة 🗕", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }

    if (showInstallApkSheet) {
        InstallApkModalBottomSheet(onDismiss = { showInstallApkSheet = false })
    }
}
