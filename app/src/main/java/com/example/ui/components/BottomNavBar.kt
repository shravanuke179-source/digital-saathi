package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.NavDestination
import com.example.ui.theme.*

@Composable
fun DigitalSaathiBottomNav(
    currentDestination: NavDestination,
    selectedLanguage: AppLanguage,
    isDarkMode: Boolean,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .testTag("digital_sathi_bottom_nav")
            .border(
                1.dp,
                if (isDarkMode) DarkCardBorder else CardBorderColor
            ),
        containerColor = if (isDarkMode) DarkCardSurface else CardSurfaceWhite,
        tonalElevation = 2.dp
    ) {
        val items = listOf(
            Triple(NavDestination.HOME, Icons.Filled.Home, Icons.Outlined.Home),
            Triple(NavDestination.SERVICES, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
            Triple(NavDestination.PROFILE, Icons.Filled.Person, Icons.Outlined.Person)
        )

        items.forEach { (destination, selectedIcon, unselectedIcon) ->
            val isSelected = if (destination == NavDestination.SERVICES) {
                currentDestination in listOf(
                    NavDestination.SERVICES,
                    NavDestination.SCHEMES,
                    NavDestination.KHETI,
                    NavDestination.SHIKSHA,
                    NavDestination.VILLAGE_SOS
                )
            } else {
                currentDestination == destination
            }

            val label = when (selectedLanguage) {
                AppLanguage.ENGLISH -> when (destination) {
                    NavDestination.HOME -> "Home"
                    NavDestination.SERVICES -> "Services"
                    NavDestination.PROFILE -> "Profile"
                    else -> destination.labelEn
                }
                AppLanguage.HINDI -> when (destination) {
                    NavDestination.HOME -> "मुख्य पृष्ठ"
                    NavDestination.SERVICES -> "सेवाएं"
                    NavDestination.PROFILE -> "प्रोफ़ाइल"
                    else -> destination.labelHi
                }
                AppLanguage.MARATHI -> when (destination) {
                    NavDestination.HOME -> "मुख्यपृष्ठ"
                    NavDestination.SERVICES -> "सेवा"
                    NavDestination.PROFILE -> "प्रोफाइल"
                    else -> destination.labelMr
                }
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isDarkMode) IndigoContainer else IndigoSecondary,
                    selectedTextColor = if (isDarkMode) IndigoContainer else IndigoSecondary,
                    indicatorColor = if (isDarkMode) Color(0xFF2D3250) else ActiveNavPill,
                    unselectedIconColor = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted,
                    unselectedTextColor = if (isDarkMode) DarkTextSecondary else TextSecondaryMuted
                ),
                modifier = Modifier.testTag("bottom_nav_${destination.name.lowercase()}")
            )
        }
    }
}
