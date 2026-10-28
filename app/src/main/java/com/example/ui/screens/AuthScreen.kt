package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.repository.DigitalSaathiRepository
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AuthScreen(
    repository: DigitalSaathiRepository,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUpMode by remember { mutableStateOf(false) }

    // Sign In form fields
    var loginUsernameOrEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Sign Up form fields
    var signUpName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpVillage by remember { mutableStateOf("") }
    var signUpDistrict by remember { mutableStateOf("") }
    var signUpPhone by remember { mutableStateOf("") }
    var isSignUpPasswordVisible by remember { mutableStateOf(false) }

    // State from repository
    val isLoading by repository.isAuthLoading.collectAsState()
    val authError by repository.authError.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var showLanguagePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkMode) DarkBackground else WarmCreamBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row with Language Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) Color(0xFF263326) else Color(0xFFE8F5E9),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDarkMode) Color(0xFF3B5E3C) else Color(0xFFC7E6CA)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ForestGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GOVERNMENT PORTAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
                modifier = Modifier.clickable { showLanguagePicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, contentDescription = "Language", tint = IndigoSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedLanguage.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App Emblem & Hero Header
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(TerracottaPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Digital Saathi",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = TerracottaPrimary
        )

        Text(
            text = when (selectedLanguage) {
                AppLanguage.ENGLISH -> "National Citizen Portal & Digital Companion"
                AppLanguage.HINDI -> "राष्ट्रीय ग्रामीण नागरिक सेवा एवं योजना पोर्टल"
                AppLanguage.MARATHI -> "राष्ट्रीय ग्रामीण नागरिक सेवा आणि योजना पोर्टल"
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Authentication Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) DarkCardBorder else CardBorderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Sign In / Sign Up Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkMode) DarkBackground else Color(0xFFF3F1EC))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isSignUpMode) (if (isDarkMode) Color(0xFF2D3250) else Color.White) else Color.Transparent)
                            .clickable { isSignUpMode = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.ENGLISH -> "Sign In"
                                AppLanguage.HINDI -> "लॉग इन"
                                AppLanguage.MARATHI -> "प्रवेश करा"
                            },
                            fontWeight = if (!isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (!isSignUpMode) (if (isDarkMode) IndigoContainer else IndigoSecondary) else TextSecondaryMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSignUpMode) (if (isDarkMode) Color(0xFF2D3250) else Color.White) else Color.Transparent)
                            .clickable { isSignUpMode = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (selectedLanguage) {
                                AppLanguage.ENGLISH -> "Create Account"
                                AppLanguage.HINDI -> "नया खाता बनाएं"
                                AppLanguage.MARATHI -> "नवीन खाते"
                            },
                            fontWeight = if (isSignUpMode) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSignUpMode) (if (isDarkMode) IndigoContainer else IndigoSecondary) else TextSecondaryMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Error Message banner
                authError?.let { err ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDarkMode) Color(0xFF3E1F1F) else Color(0xFFFFEBEE),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDarkMode) Color(0xFF6E2B2B) else Color(0xFFFFCDD2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = err, color = AlertRed, fontSize = 12.sp)
                        }
                    }
                }

                if (!isSignUpMode) {
                    // SIGN IN MODE
                    Text(
                        text = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Sign In with Email and Password"
                            AppLanguage.HINDI -> "उपयोगकर्ता नाम या पासवर्ड से प्रवेश करें"
                            AppLanguage.MARATHI -> "युझरनेम किंवा पासवर्डने प्रवेश करा"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginUsernameOrEmail,
                        onValueChange = { loginUsernameOrEmail = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g. ramesh.kumar@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TerracottaPrimary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password (पासवर्ड)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TerracottaPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            repository.loginWithCredentials(
                                usernameOrEmail = loginUsernameOrEmail,
                                password = loginPassword
                            )
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> "Sign In to Portal"
                                    AppLanguage.HINDI -> "पोर्टल में प्रवेश करें"
                                    AppLanguage.MARATHI -> "पोर्टलमध्ये प्रवेश करा"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { repository.resetPassword(loginUsernameOrEmail) }, enabled = !isLoading) {
                        Text("Forgot password?")
                    }
                    // "OR" Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = if (isDarkMode) DarkCardBorder else CardBorderColor)
                        Text(
                            text = "  OR CONNECT WITH  ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryMuted
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = if (isDarkMode) DarkCardBorder else CardBorderColor)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In with Google / Gmail Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) Color(0xFF282834) else Color(0xFFFFFFFF),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDarkMode) Color(0xFF43435C) else Color(0xFFD3D7E0)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable(enabled = !isLoading) { repository.loginWithGoogle(context) }
                            .testTag("login_google_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Google 'G' icon badge
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEA4335)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> "Sign In with Gmail / Google"
                                    AppLanguage.HINDI -> "गूगल (Gmail) से प्रवेश करें"
                                    AppLanguage.MARATHI -> "गुगल (Gmail) ने प्रवेश करा"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                            )
                        }
                    }
                } else {
                    // SIGN UP MODE
                    Text(
                        text = when (selectedLanguage) {
                            AppLanguage.ENGLISH -> "Create New Citizen Account"
                            AppLanguage.HINDI -> "नया ग्रामीण नागरिक खाता बनाएं"
                            AppLanguage.MARATHI -> "नवीन ग्रामीण नागरिक खाते तयार करा"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDarkMode) DarkTextPrimary else TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = signUpName,
                        onValueChange = { signUpName = it },
                        label = { Text("Full Name (पूरा नाम)") },
                        placeholder = { Text("Ramesh Kumar") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = IndigoSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signUpEmail,
                        onValueChange = { signUpEmail = it },
                        label = { Text("Gmail or Email Address") },
                        placeholder = { Text("ramesh.kumar@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = IndigoSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signUpPassword,
                        onValueChange = { signUpPassword = it },
                        label = { Text("Create Password (पासवर्ड बनाएं)") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoSecondary) },
                        trailingIcon = {
                            IconButton(onClick = { isSignUpPasswordVisible = !isSignUpPasswordVisible }) {
                                Icon(
                                    imageVector = if (isSignUpPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password"
                                )
                            }
                        },
                        visualTransformation = if (isSignUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = signUpVillage,
                            onValueChange = { signUpVillage = it },
                            label = { Text("Village (ग्राम)") },
                            placeholder = { Text("Palampur") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = signUpDistrict,
                            onValueChange = { signUpDistrict = it },
                            label = { Text("District (जिला)") },
                            placeholder = { Text("Kangra") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = signUpPhone,
                        onValueChange = { signUpPhone = it },
                        label = { Text("Mobile Number (फोन)") },
                        placeholder = { Text("+91 98765 43210") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = IndigoSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            repository.signUpWithCredentials(
                                name = signUpName,
                                email = signUpEmail,
                                password = signUpPassword,
                                village = signUpVillage,
                                district = signUpDistrict,
                                phone = signUpPhone
                            )
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signup_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.ENGLISH -> "Create Account & Sync Profile"
                                    AppLanguage.HINDI -> "खाता बनाएं एवं प्रोफ़ाइल जोड़ें"
                                    AppLanguage.MARATHI -> "खाते तयार करा व सिंक करा"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fast Demo / Guest Login
                TextButton(
                    onClick = { repository.loginAsDemo() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("demo_access_button")
                ) {
                    Text(
                        text = "⚡ Continue as Guest (अतिथि प्रवेश)",
                        color = TerracottaPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security & Privacy Notice
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Protected by Firebase Authentication",
                fontSize = 10.sp,
                color = TextSecondaryMuted,
                textAlign = TextAlign.Center
            )
        }
    }

    // Language Picker Dialog
    if (showLanguagePicker) {
        Dialog(onDismissRequest = { showLanguagePicker = false }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Select App Language",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppLanguage.values().forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onLanguageChange(lang)
                                    showLanguagePicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${lang.displayName} (${lang.nativeName})",
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TerracottaPrimary else (if (isDarkMode) DarkTextPrimary else TextPrimaryDark)
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = TerracottaPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
