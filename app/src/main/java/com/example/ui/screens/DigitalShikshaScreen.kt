package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.components.SafetyQuizDialog
import com.example.ui.components.UpiSimulatorDialog
import com.example.ui.theme.*

@Composable
fun DigitalShikshaScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onVoiceSpeak: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lessons = repository.digitalLessons
    var showUpiSimulator by remember { mutableStateOf(false) }
    var showQuiz by remember { mutableStateOf(false) }
    var selectedLessonDetail by remember { mutableStateOf<DigitalLesson?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Digital Shiksha & Safety Hub"
                        AppLanguage.HINDI -> "डिजिटल शिक्षा और सुरक्षा हब"
                        AppLanguage.MARATHI -> "डिजिटल शिक्षण आणि सुरक्षा हब"
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (selectedLanguage) {
                        AppLanguage.ENGLISH -> "Practical lessons on smartphone operations, safe digital payments, and cyber fraud defense"
                        AppLanguage.HINDI -> "स्मार्टफोन उपयोग, सुरक्षित डिजिटल भुगतान और साइबर धोखाधड़ी से बचाव के सरल पाठ"
                        AppLanguage.MARATHI -> "स्मार्टफोन वापर, सुरक्षित डिजिटल पेमेंट्स आणि सायबर फसवणुकीपासून संरक्षणाचे सोपे धडे"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                )
            }
        }

        // Interactive Launchers Row (UPI Simulator + Quiz)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDarkMode) Color(0xFF2E271F) else Color(0xFFFFF9F0),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF574630) else Color(0xFFFFE0B2)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showUpiSimulator = true }
                        .testTag("launch_upi_simulator_button")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFE0B2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("UPI Simulator", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimaryDark)
                        Text("Practice demo money transfers safely", fontSize = 11.sp, color = TextSecondaryMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Launch Simulator →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDarkMode) Color(0xFF282538) else Color(0xFFF3F4FD),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF3F466E) else Color(0xFFD6DBF8)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showQuiz = true }
                        .testTag("launch_safety_quiz_button")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0E5FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, tint = IndigoSecondary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Safety Quiz", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimaryDark)
                        Text("Test your cyber fraud readiness", fontSize = 11.sp, color = TextSecondaryMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Take 5-Q Quiz →", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                    }
                }
            }
        }

        // Cyber Crime Helpline Banner (1930)
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) Color(0xFF312121) else Color(0xFFFFF2F2),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF5E3535) else Color(0xFFFFCDD2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AlertRed, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cyber Fraud Emergency: Dial 1930",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AlertRed
                        )
                        Text(
                            text = "If cheated online or on phone, call within 2 hours to block transaction.",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted
                        )
                    }
                }
            }
        }

        // Lessons List Header
        item {
            Text(
                text = "Interactive Learning Modules (कदम दर कदम सीखें)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
            )
        }

        items(lessons) { lesson ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedLessonDetail = lesson }
                    .testTag("lesson_card_${lesson.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when (lesson.categoryId) {
                                        "basics" -> Color(0xFFE8EDFF)
                                        "upi" -> Color(0xFFFFE0B2)
                                        "forms" -> Color(0xFFE8F5E9)
                                        else -> Color(0xFFFFEBEE)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (selectedLanguage == AppLanguage.HINDI) lesson.badgeHi else lesson.badgeEn,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (lesson.categoryId) {
                                    "basics" -> IndigoSecondary
                                    "upi" -> AmberAccent
                                    "forms" -> ForestGreen
                                    else -> AlertRed
                                }
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = if (isDarkMode) DarkTextSecondary else TextSubtle
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (selectedLanguage == AppLanguage.HINDI) lesson.titleHi else lesson.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (selectedLanguage == AppLanguage.HINDI) lesson.descriptionHi else lesson.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Read all ${lesson.lessonCount} lessons & safety tips →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoSecondary
                    )
                }
            }
        }
    }

    // UPI Simulator Modal
    if (showUpiSimulator) {
        UpiSimulatorDialog(
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onDismiss = { showUpiSimulator = false }
        )
    }

    // Safety Quiz Modal
    if (showQuiz) {
        SafetyQuizDialog(
            questions = repository.quizQuestions,
            selectedLanguage = selectedLanguage,
            isDarkMode = isDarkMode,
            onDismiss = { showQuiz = false }
        )
    }

    // Lesson Step-by-Step Viewer
    selectedLessonDetail?.let { lesson ->
        Dialog(onDismissRequest = { selectedLessonDetail = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) lesson.titleHi else lesson.titleEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedLessonDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) lesson.descriptionHi else lesson.descriptionEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )

                        Text("Steps to Follow:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TerracottaPrimary)

                        val steps = if (selectedLanguage == AppLanguage.HINDI) lesson.contentStepsHi else lesson.contentStepsEn
                        steps.forEachIndexed { idx, step ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkMode) DarkBackground else WarmCreamBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(IndigoSecondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${idx + 1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(step, fontSize = 12.sp, color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Critical Security Tips:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AlertRed)

                        val tips = if (selectedLanguage == AppLanguage.HINDI) lesson.safetyTipsHi else lesson.safetyTipsEn
                        tips.forEach { tip ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tip, fontSize = 11.sp, color = Color(0xFFB71C1C), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                val content = if (selectedLanguage == AppLanguage.HINDI) {
                                    "${lesson.titleHi}. ${lesson.descriptionHi}. " + lesson.contentStepsHi.joinToString(". ")
                                } else {
                                    "${lesson.titleEn}. ${lesson.descriptionEn}. " + lesson.contentStepsEn.joinToString(". ")
                                }
                                onVoiceSpeak(content)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Read Aloud")
                        }

                        Button(
                            onClick = { selectedLessonDetail = null },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}
