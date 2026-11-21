package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.CaptionLanguage
import com.example.data.model.ExportPlatform
import com.example.data.model.SubscriptionPlan
import com.example.data.model.UserProfileSettings
import com.example.ui.components.GlassStudioCard
import com.example.ui.components.SampleAiNoticeBanner
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.HighlightAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ViralEmerald
import com.example.ui.theme.ViralGradients
import com.example.ui.theme.VoidBlack

@Composable
fun AccountPlansScreen(
    settings: UserProfileSettings,
    isSampleAiMode: Boolean,
    onUpdateProfile: (String, String) -> Unit,
    onSelectPlan: (SubscriptionPlan) -> Unit,
    onResetMonthlyUsage: () -> Unit,
    onUpdateDefaultLanguage: (CaptionLanguage) -> Unit,
    onUpdateDefaultPlatform: (ExportPlatform) -> Unit,
    onToggleFullHd: (Boolean) -> Unit,
    onToggleAutoSilence: (Boolean) -> Unit,
    onToggleEmojiDefault: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var nameInput by remember(settings.displayName) { mutableStateOf(settings.displayName) }
    var handleInput by remember(settings.handle) { mutableStateOf(settings.handle) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ViralGradients.screenBackdrop),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 880.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Profile, Plans & Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )

            // 1. User Profile Card
            GlassStudioCard(highlighted = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(ViralGradients.primaryAction),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = settings.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "${settings.handle} • Local Studio Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonBlue
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Creator Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = handleInput,
                    onValueChange = { handleInput = it },
                    label = { Text("Social Handle") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onUpdateProfile(nameInput, handleInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepViolet, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Creator Profile", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (isSampleAiMode) {
                SampleAiNoticeBanner()
            }

            // 2. Monthly Usage & Subscription Management
            GlassStudioCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = HighlightAmber)
                        Spacer(Modifier.width(8.dp))
                        Text("Subscription & Monthly Quota", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    }
                }

                Spacer(Modifier.height(12.dp))
                val limit = settings.plan.monthlyClipLimit
                val used = settings.clipsGeneratedThisMonth
                val fraction = if (limit >= 9999) 0.15f else (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Active Plan: ${settings.plan.title}", style = MaterialTheme.typography.titleSmall, color = NeonBlue)
                    Text(
                        text = if (limit >= 9999) "$used / Unlimited clips" else "$used / $limit clips used",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (fraction > 0.85f) NeonMagenta else NeonBlue,
                    trackColor = ObsidianElevated
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onResetMonthlyUsage,
                    border = BorderStroke(1.dp, ObsidianBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reset Monthly Counter", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                }

                Spacer(Modifier.height(16.dp))

                // Plan Cards (Free, Creator Pro, Studio Ultra)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SubscriptionPlan.entries.forEach { plan ->
                        val isCurrent = settings.plan == plan
                        val accent = when (plan) {
                            SubscriptionPlan.FREE -> NeonBlue
                            SubscriptionPlan.CREATOR_PRO -> NeonPurple
                            SubscriptionPlan.STUDIO_ULTRA -> HighlightAmber
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectPlan(plan) }
                                .testTag("plan_card_${plan.name.lowercase()}"),
                            color = if (isCurrent) accent.copy(alpha = 0.14f) else ObsidianElevated,
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) accent else ObsidianBorder
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(plan.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                                        Text(plan.tagline, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    }
                                    Text(plan.priceMonthly, style = MaterialTheme.typography.titleMedium, color = accent)
                                }
                                Spacer(Modifier.height(10.dp))
                                plan.features.forEach { feat ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = ViralEmerald,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(feat, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = if (isCurrent) "CURRENT ACTIVE PLAN" else "TAP TO ACTIVATE PLAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCurrent) ViralEmerald else accent
                                )
                            }
                        }
                    }
                }
            }

            // 3. Studio Settings
            GlassStudioCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Settings, contentDescription = null, tint = NeonBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("Studio Export & Caption Defaults", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                }

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("1080×1920 Full HD Export", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Disable for faster 720×1280 rendering", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Switch(
                        checked = settings.fullHdExport,
                        onCheckedChange = onToggleFullHd,
                        colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = NeonBlue)
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Remove Silent Portions", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Tighten pacing in new clips automatically", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Switch(
                        checked = settings.autoRemoveSilence,
                        onCheckedChange = onToggleAutoSilence,
                        colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = NeonPurple)
                    )
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Emoji Captions by Default", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Include expressive viral emojis on subtitles", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                    Switch(
                        checked = settings.defaultEmojiCaptions,
                        onCheckedChange = onToggleEmojiDefault,
                        colors = SwitchDefaults.colors(checkedThumbColor = VoidBlack, checkedTrackColor = NeonBlue)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text("Default Caption Language", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CaptionLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = settings.defaultLanguage == lang,
                            onClick = { onUpdateDefaultLanguage(lang) },
                            label = { Text(lang.nativeLabel) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepViolet,
                                selectedLabelColor = TextPrimary,
                                containerColor = ObsidianElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Default Platform Preset", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportPlatform.entries.forEach { platform ->
                        FilterChip(
                            selected = settings.defaultPlatform == platform,
                            onClick = { onUpdateDefaultPlatform(platform) },
                            label = { Text(platform.shortLabel) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonMagenta.copy(alpha = 0.25f),
                                selectedLabelColor = TextPrimary,
                                containerColor = ObsidianElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
