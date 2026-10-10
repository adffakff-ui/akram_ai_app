package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.sync.SyncStatus
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun QuranHeader(
    currentRole: UserRole,
    syncStatus: SyncStatus,
    isNightMode: Boolean,
    isFullscreen: Boolean = false,
    currentLanguage: String,
    onRoleChange: (UserRole) -> Unit,
    onSyncClick: () -> Unit,
    onNightModeToggle: () -> Unit,
    onLanguageToggle: () -> Unit,
    onFullscreenToggle: () -> Unit = {},
    onDownloadApkClick: () -> Unit,
    onLockClick: () -> Unit = {}
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = EmeraldDark,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth().testTag("quran_header")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // App Title & Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "شعار منارة القرآن",
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (currentLanguage == "ar") "مَنَارَةُ القُرْآنِ الكَرِيمِ" else "Manarat Al-Quran",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentLanguage == "ar") "المنظومة الشاملة لإدارة الحلقات والتحفيظ" else "Quran Memorization System",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Action Icons (Fullscreen, APK Download, Dark mode, Lang)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Fullscreen Toggle Button
                    IconButton(
                        onClick = onFullscreenToggle,
                        modifier = Modifier.testTag("fullscreen_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullscreen) "إلغاء ملء الشاشة" else "تفعيل ملء الشاشة",
                            tint = if (isFullscreen) GoldPrimary else Color.White
                        )
                    }

                    // Direct APK & Web Install Button
                    IconButton(
                        onClick = onDownloadApkClick,
                        modifier = Modifier.testTag("download_apk_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "تحميل وتثبيت التطبيق APK",
                            tint = GoldPrimary
                        )
                    }

                    // Night Mode Toggle
                    IconButton(
                        onClick = onNightModeToggle,
                        modifier = Modifier.testTag("night_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "الوضع الليلي",
                            tint = Color.White
                        )
                    }

                    // Lock / Auth Gate Screen Toggle Button
                    IconButton(
                        onClick = onLockClick,
                        modifier = Modifier.testTag("auth_gate_lock_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "شاشة الحماية والمصادقة (قفل)",
                            tint = GoldPrimary
                        )
                    }

                    // Language Toggle
                    IconButton(
                        onClick = onLanguageToggle,
                        modifier = Modifier.testTag("language_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "تغيير اللغة",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-row: Active Role Pill + Offline Sync Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Role Selector Dropdown
                Box {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .clickable { roleMenuExpanded = true }
                            .testTag("role_selector_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = when (currentRole) {
                                    UserRole.SUPERVISOR -> Icons.Default.SupervisorAccount
                                    UserRole.DEVELOPER -> Icons.Default.AdminPanelSettings
                                    UserRole.TEACHER -> Icons.Default.School
                                    UserRole.PARENT -> Icons.Default.FamilyRestroom
                                    UserRole.STUDENT -> Icons.Default.Person
                                },
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentRole.arabicTitle,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        UserRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.arabicTitle) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (role) {
                                            UserRole.SUPERVISOR -> Icons.Default.SupervisorAccount
                                            UserRole.DEVELOPER -> Icons.Default.AdminPanelSettings
                                            UserRole.TEACHER -> Icons.Default.School
                                            UserRole.PARENT -> Icons.Default.FamilyRestroom
                                            UserRole.STUDENT -> Icons.Default.Person
                                        },
                                        contentDescription = null,
                                        tint = EmeraldDark
                                    )
                                },
                                onClick = {
                                    onRoleChange(role)
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Sync Status Pill (Clickable to trigger immediate sync)
                Surface(
                    color = when (syncStatus) {
                        SyncStatus.ONLINE_SYNCED -> EmeraldLight.copy(alpha = 0.25f)
                        SyncStatus.SYNCING -> GoldPrimary.copy(alpha = 0.25f)
                        SyncStatus.OFFLINE_PENDING -> Color(0xFFF59E0B).copy(alpha = 0.25f)
                        SyncStatus.OFFLINE_SYNCED -> Color.White.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .clickable { onSyncClick() }
                        .testTag("sync_status_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = when (syncStatus) {
                                SyncStatus.SYNCING -> Icons.Default.Sync
                                SyncStatus.ONLINE_SYNCED -> Icons.Default.CloudDone
                                else -> Icons.Default.CloudOff
                            },
                            contentDescription = syncStatus.arabicLabel,
                            tint = when (syncStatus) {
                                SyncStatus.ONLINE_SYNCED -> EmeraldLight
                                SyncStatus.SYNCING -> GoldPrimary
                                else -> Color.White.copy(alpha = 0.8f)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = syncStatus.arabicLabel,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
