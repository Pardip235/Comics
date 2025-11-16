package com.bpn.comics.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 * Composable function for bottom navigation bar
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    currentRoute: String?,
    items: List<BottomNavItem> = NavigationConfig.bottomNavItems
) {
    NavigationBar {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.contentDescription) },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    navController.navigateToTab(item.route)
                }
            )
        }
    }
}

