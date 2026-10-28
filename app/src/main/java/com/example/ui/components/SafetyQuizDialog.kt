package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.model.QuizQuestion
import com.example.ui.theme.*

@Composable
fun SafetyQuizDialog(
    questions: List<QuizQuestion>,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var isSubmitted by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    val currentQuestion = questions.getOrNull(currentIndex)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(IndigoContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                tint = IndigoSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) "डिजिटल सुरक्षा प्रश्नोत्तरी" else "Digital Safety Quiz",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isCompleted) {
                    // Result Summary
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Safe",
                                tint = ForestGreen,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (selectedLanguage == AppLanguage.HINDI) "सुरक्षा प्रमाण पत्र: सफल!" else "Safety Certified Citizen!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ForestGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You scored $score / ${questions.size}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TerracottaPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (score >= 4) {
                                "Excellent! You are well-protected against common village phone frauds, fake OTP requests, and phishing SMS."
                            } else {
                                "Good effort! Remember: never share your UPI PIN or Bank OTP, and never click unknown SMS links."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back to Digital Shiksha")
                        }
                    }
                } else if (currentQuestion != null) {
                    // Question Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TerracottaPrimary
                        )
                        LinearProgressIndicator(
                            progress = { (currentIndex + 1).toFloat() / questions.size },
                            modifier = Modifier
                                .width(100.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = TerracottaPrimary,
                            trackColor = if (isDarkMode) DarkCardBorder else Color(0xFFE0E0E0)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (selectedLanguage == AppLanguage.HINDI) currentQuestion.questionHi else currentQuestion.questionEn,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val options = if (selectedLanguage == AppLanguage.HINDI) currentQuestion.optionsHi else currentQuestion.optionsEn
                    options.forEachIndexed { index, option ->
                        val isChosen = selectedOption == index
                        val isCorrect = isSubmitted && index == currentQuestion.correctIndex
                        val isWrong = isSubmitted && isChosen && index != currentQuestion.correctIndex

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isCorrect -> Color(0xFFE8F5E9)
                                isWrong -> Color(0xFFFFEBEE)
                                isChosen -> if (isDarkMode) Color(0xFF2D3250) else ActiveNavPill
                                else -> if (isDarkMode) DarkBackground else WarmCreamBackground
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when {
                                    isCorrect -> ForestGreen
                                    isWrong -> AlertRed
                                    isChosen -> IndigoSecondary
                                    else -> if (isDarkMode) DarkCardBorder else CardBorderColor
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isSubmitted) { selectedOption = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCorrect -> ForestGreen
                                                isWrong -> AlertRed
                                                isChosen -> IndigoSecondary
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isChosen || isCorrect || isWrong) Color.Transparent else TextSubtle,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCorrect) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else if (isWrong) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else if (isChosen) {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = option,
                                    fontSize = 13.sp,
                                    fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                )
                            }
                        }
                    }

                    if (isSubmitted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF9C4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (selectedLanguage == AppLanguage.HINDI) currentQuestion.explanationHi else currentQuestion.explanationEn,
                                fontSize = 12.sp,
                                color = Color(0xFF5D4037),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isSubmitted) {
                        Button(
                            onClick = {
                                if (selectedOption != null) {
                                    isSubmitted = true
                                    if (selectedOption == currentQuestion.correctIndex) {
                                        score++
                                    }
                                }
                            },
                            enabled = selectedOption != null,
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("quiz_submit_answer_button")
                        ) {
                            Text("Check Answer", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentIndex < questions.size - 1) {
                                    currentIndex++
                                    selectedOption = null
                                    isSubmitted = false
                                } else {
                                    isCompleted = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("quiz_next_button")
                        ) {
                            Text(if (currentIndex < questions.size - 1) "Next Question →" else "View Results", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
