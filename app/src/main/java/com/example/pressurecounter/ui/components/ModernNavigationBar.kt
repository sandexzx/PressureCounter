package com.example.pressurecounter.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.example.pressurecounter.ui.navigation.Screen

@Composable
fun ModernNavigationBar(
    items: List<Screen>,
    currentDestination: NavDestination?,
    onItemClick: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
            tonalElevation = 8.dp,
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    ModernNavigationItem(
                        screen = screen,
                        selected = selected,
                        onClick = { onItemClick(screen) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernNavigationItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        label = "color"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val containerAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        label = "alpha"
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Selection background highlight
        Box(
            modifier = Modifier
                .fillMaxWidth(if (selected) 0.9f else 0f)
                .height(44.dp)
                .alpha(containerAlpha)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .scale(scale)
        ) {
            screen.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = screen.title,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = screen.title,
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
