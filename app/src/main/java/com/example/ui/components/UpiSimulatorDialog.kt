package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.theme.*

@Composable
fun UpiSimulatorDialog(
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Setup, 2: PIN, 3: Success
    var recipient by remember { mutableStateOf("Palampur Village Kirana Store") }
    var amount by remember { mutableStateOf("150") }
    var pin by remember { mutableStateOf("") }
    var txnId by remember { mutableStateOf("UPI/2026/0908/${(100000..999999).random()}") }

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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header with Close
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
                                .background(Color(0xFFFFE0B2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = TerracottaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UPI Practice Simulator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (step) {
                    1 -> {
                        // Transfer Setup
                        Text(
                            text = "Step 1: Choose Village Shop & Demo Amount",
                            style = MaterialTheme.typography.labelLarge,
                            color = TerracottaPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Select Recipient
                        Text(
                            text = "Select Recipient (दुकानदार चुनें):",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val recipients = listOf(
                            "Palampur Village Kirana Store",
                            "Kisan Krishi Seva Kendra (Seeds & Fertilizer)",
                            "Primary Health Clinic Chemist"
                        )

                        recipients.forEach { r ->
                            val isSelected = recipient == r
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) (if (isDarkMode) Color(0xFF2D3250) else ActiveNavPill) else Color.Transparent)
                                    .border(1.dp, if (isSelected) IndigoSecondary else (if (isDarkMode) DarkCardBorder else CardBorderColor), RoundedCornerShape(10.dp))
                                    .clickable { recipient = r }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { recipient = r },
                                    colors = RadioButtonDefaults.colors(selectedColor = IndigoSecondary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = r,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Amounts
                        Text(
                            text = "Demo Amount (डेमो राशि):",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("50", "150", "500", "1000").forEach { amt ->
                                OutlinedButton(
                                    onClick = { amount = amt },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (amount == amt) TerracottaContainer else Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("₹$amt", fontWeight = FontWeight.Bold, color = TerracottaPrimary, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                pin = ""
                                step = 2
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("upi_proceed_button")
                        ) {
                            Text("Proceed to Enter PIN →", fontWeight = FontWeight.Bold)
                        }
                    }

                    2 -> {
                        // PIN Entry Pad
                        // Critical Warning Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFEBEE),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = AlertRed,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SAFETY RULE: Never share UPI PIN or OTP. PIN is ONLY entered to SEND money, NEVER to receive!",
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Paying ₹$amount to $recipient",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4 PIN Dots
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            for (i in 0 until 4) {
                                val filled = i < pin.length
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (filled) IndigoSecondary else (if (isDarkMode) Color.DarkGray else Color(0xFFE0E0E0)))
                                        .border(1.dp, IndigoSecondary, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Keypad 1-9, 0, Backspace
                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("", "0", "DEL")
                        )

                        keys.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                row.forEach { key ->
                                    if (key.isEmpty()) {
                                        Spacer(modifier = Modifier.size(54.dp))
                                    } else {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isDarkMode) DarkBackground else Color(0xFFF5F5F5),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clickable {
                                                    if (key == "DEL") {
                                                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                    } else {
                                                        if (pin.length < 4) pin += key
                                                    }
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (key == "DEL") {
                                                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = AlertRed)
                                                } else {
                                                    Text(
                                                        text = key,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (pin.length == 4) {
                                    txnId = "UPI/2026/0908/${(100000..999999).random()}"
                                    step = 3
                                }
                            },
                            enabled = pin.length == 4,
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("upi_confirm_pin_button")
                        ) {
                            Text("Confirm & Pay ₹$amount (डेमो)", fontWeight = FontWeight.Bold)
                        }
                    }

                    3 -> {
                        // Payment Success Screen
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = ForestGreen,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Demo Payment Successful!",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ForestGreen
                        )
                        Text(
                            text = "डेमो भुगतान सफलतापूर्वक पूरा हुआ",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDarkMode) DarkBackground else WarmCreamBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Amount Paid:", fontSize = 13.sp, color = TextSecondaryMuted)
                                    Text("₹$amount.00", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("To:", fontSize = 13.sp, color = TextSecondaryMuted)
                                    Text(recipient, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark, maxLines = 1)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("UPI Ref No:", fontSize = 13.sp, color = TextSecondaryMuted)
                                    Text(txnId, fontSize = 12.sp, color = TextPrimaryDark)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Debited From:", fontSize = 13.sp, color = TextSecondaryMuted)
                                    Text("SBI (A/C ...4829)", fontSize = 12.sp, color = TextPrimaryDark)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    pin = ""
                                    step = 1
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Practice Again")
                            }

                            Button(
                                onClick = onDismiss,
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
}
