package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.NetworkStatus
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.ProfileSetupSheet
import com.example.ui.call.CallScreen
import com.example.ui.camera.CameraScreen
import com.example.ui.chat.ChatDetailScreen
import com.example.ui.chat.DirectMessagesScreen
import com.example.ui.components.GlobalUploadOverlay
import com.example.ui.components.UpdateCheckerDialog
import com.example.ui.explore.ExploreScreen
import com.example.ui.feed.FeedScreen
import com.example.ui.filters.FilterStudioScreen
import com.example.ui.live.LiveRoomScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.reels.CreateReelScreen
import com.example.ui.reels.ReelsScreen
import com.example.ui.stories.StoryViewerScreen
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import com.example.ui.theme.HundredGramBorderSubtle
import com.example.ui.theme.HundredGramButtonGradient
import com.example.ui.theme.HundredGramCardBackground
import com.example.ui.theme.HundredGramCardElevated
import com.example.ui.theme.HundredGramCardGlass
import com.example.ui.theme.HundredGramDarkBackground
import com.example.ui.theme.HundredGramDivider
import com.example.ui.theme.HundredGramPink
import com.example.ui.theme.HundredGramPurple
import com.example.ui.theme.HundredGramTextPrimary
import com.example.ui.theme.HundredGramTextSecondary
import com.example.ui.theme.HundredGramVibrantGradient
import com.example.ui.theme.HundredGramTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle results:
        // Location, Audio, Contacts, Camera, SMS permissions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check and request runtime permissions on app launch
        checkAndRequestPermissions()

        setContent {
            HundredGramTheme {
                HundredGramApp(viewModel)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_SMS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
        }

        val ungrantedPermissions = permissionsToRequest.filter { perm ->
            ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED
        }

        if (ungrantedPermissions.isNotEmpty()) {
            permissionLauncher.launch(ungrantedPermissions.toTypedArray())
        }
    }
}

@Composable
fun HundredGramApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    
    // Intercept back presses to navigate back to the Home (Feed) screen instead of exiting
    BackHandler(enabled = currentScreen != ScreenDestination.Feed) {
        viewModel.navigateTo(ScreenDestination.Feed)
    }

    val isUploading by viewModel.isUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val uploadMessage by viewModel.uploadMessage.collectAsState()
    val uploadError by viewModel.uploadError.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val networkSpeed by viewModel.networkSpeed.collectAsState()
    val showLoginPrompt by viewModel.showLoginPrompt.collectAsState()
    val loginPromptReason by viewModel.loginPromptReason.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showUpdateDialog by remember { mutableStateOf(false) }
    var showProfileSetup by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        if (currentUser.isLoggedIn && !currentUser.isTermsAccepted) {
            showProfileSetup = true
        }
    }

    // Proactively request runtime permissions on Compose launch (only once)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val hasRequested = prefs.getBoolean("permissions_requested", false)
        
        if (!hasRequested) {
            val permissions = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            
            permissionLauncher.launch(permissions.toTypedArray())
            prefs.edit().putBoolean("permissions_requested", true).apply()
        }
    }

    val showBottomBar = when (currentScreen) {
        is ScreenDestination.Feed,
        is ScreenDestination.Explore,
        is ScreenDestination.Reels,
        is ScreenDestination.Profile -> true
        else -> false
    }

    val showTopBar = when (currentScreen) {
        is ScreenDestination.Feed -> true
        else -> false
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBarContent(
                    networkStatus = networkStatus,
                    networkSpeed = networkSpeed,
                    onNotificationsClick = { viewModel.navigateTo(ScreenDestination.Notifications) },
                    onDirectMessagesClick = { viewModel.navigateTo(ScreenDestination.DirectMessages) },
                    onAuthClick = { viewModel.navigateTo(ScreenDestination.Auth) }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentDestination = currentScreen,
                    onNavigate = { dest -> viewModel.navigateTo(dest) }
                )
            }
        },
        containerColor = HundredGramDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is ScreenDestination.Auth -> AuthScreen(viewModel)
                    is ScreenDestination.Feed -> FeedScreen(viewModel)
                    is ScreenDestination.Explore -> ExploreScreen(viewModel)
                    is ScreenDestination.Reels -> ReelsScreen(viewModel)
                    is ScreenDestination.Profile -> ProfileScreen(viewModel)
                    is ScreenDestination.DirectMessages -> DirectMessagesScreen(viewModel)
                    is ScreenDestination.ChatDetail -> ChatDetailScreen(
                        viewModel = viewModel,
                        threadId = screen.threadId,
                        recipientUsername = screen.recipientUsername,
                        recipientAvatar = screen.recipientAvatar
                    )
                    is ScreenDestination.StoryViewer -> StoryViewerScreen(viewModel, screen.storyIndex)
                    is ScreenDestination.Camera -> CameraScreen(viewModel)
                    is ScreenDestination.FilterStudio -> FilterStudioScreen(viewModel)
                    is ScreenDestination.CreateReel -> CreateReelScreen(viewModel)
                    is ScreenDestination.Call -> CallScreen(viewModel, screen.callName, screen.isVideo)
                    is ScreenDestination.LiveRoom -> LiveRoomScreen(viewModel)
                    is ScreenDestination.Notifications -> com.example.ui.notifications.NotificationsScreen(viewModel)
                    is ScreenDestination.CreatorProfile -> com.example.ui.profile.CreatorProfileScreen(viewModel, screen.userId)
                }
            }

            // Top Internet Status Indicator Dot when TopAppBar is not displayed
            if (!showTopBar && currentScreen !is ScreenDestination.Camera && currentScreen !is ScreenDestination.StoryViewer && currentScreen !is ScreenDestination.Call && currentScreen !is ScreenDestination.LiveRoom) {
                val dotColor = when (networkStatus) {
                    NetworkStatus.Available -> Color(0xFF22C55E) // Green (हरा)
                    NetworkStatus.Weak, NetworkStatus.Losing -> Color(0xFFFBBF24) // Yellow (पीला)
                    NetworkStatus.Unavailable, NetworkStatus.Lost -> Color(0xFFEF4444) // Red (लाल)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 12.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(1.5.dp, dotColor.copy(alpha = 0.35f), CircleShape)
                )
            }

            // Global Upload Progress Banner & Error Feedback
            GlobalUploadOverlay(
                isUploading = isUploading,
                uploadProgress = uploadProgress,
                statusMessage = uploadMessage,
                errorMessage = uploadError,
                onDismissError = { viewModel.clearUploadError() }
            )

            if (showProfileSetup) {
                ProfileSetupSheet(
                    viewModel = viewModel,
                    user = currentUser,
                    onDismiss = { showProfileSetup = false }
                )
            }

            if (showLoginPrompt) {
                com.example.ui.components.LoginRequiredDialog(
                    reason = loginPromptReason,
                    onDismiss = { viewModel.dismissLoginPrompt() },
                    onLoginClick = { viewModel.openLoginScreen() }
                )
            }
        }
    }

    UpdateCheckerDialog(
        show = showUpdateDialog,
        onDismiss = { showUpdateDialog = false },
        onUpdateConfirm = { /* check update */ }
    )
}

@Composable
fun TopAppBarContent(
    networkStatus: NetworkStatus,
    networkSpeed: String,
    onNotificationsClick: () -> Unit,
    onDirectMessagesClick: () -> Unit,
    onAuthClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HundredGramDarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo with dynamic connection status dot (Green: Connected, Yellow: Weak, Red: Disconnected)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAuthClick
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HundredGram",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = HundredGramTextPrimary,
                    letterSpacing = (-0.5).sp
                )
                
                // Status dot: Green when connected, Yellow when weak, Red when disconnected
                val dotColor = when (networkStatus) {
                    NetworkStatus.Available -> Color(0xFF22C55E) // Green (हरा)
                    NetworkStatus.Weak, NetworkStatus.Losing -> Color(0xFFFBBF24) // Yellow (पीला)
                    NetworkStatus.Unavailable, NetworkStatus.Lost -> Color(0xFFEF4444) // Red (लाल)
                }
                Box(
                    modifier = Modifier
                        .padding(start = 7.dp, top = 2.dp)
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(1.5.dp, dotColor.copy(alpha = 0.35f), CircleShape)
                )
            }

            // Real-time network speed box (⚡ 14.5 KB/s)
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .background(HundredGramCardElevated, RoundedCornerShape(8.dp))
                    .border(0.5.dp, HundredGramBorderSubtle.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 10.sp,
                        color = Color(0xFFFBBF24)
                    )
                    Text(
                        text = networkSpeed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = HundredGramTextPrimary
                    )
                }
            }

            // Notifications with notification badge dot
            // Notifications Icon (📢)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(HundredGramCardElevated)
                    .border(1.dp, HundredGramBorderSubtle.copy(alpha = 0.5f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 21.dp),
                        onClick = onNotificationsClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📢",
                    fontSize = 20.sp
                )
                // Red unread notification indicator dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HundredGramPink)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Direct Messages Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(HundredGramCardElevated)
                    .border(1.dp, HundredGramBorderSubtle.copy(alpha = 0.5f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 21.dp),
                        onClick = onDirectMessagesClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Direct Messages",
                    tint = HundredGramTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Divider(color = HundredGramDivider, thickness = 0.8.dp)
    }
}

@Composable
fun BottomNavigationBar(
    currentDestination: ScreenDestination,
    onNavigate: (ScreenDestination) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(color = HundredGramDivider, thickness = 0.8.dp)
        NavigationBar(
            containerColor = HundredGramCardBackground,
            contentColor = HundredGramPink,
            tonalElevation = 4.dp,
            modifier = Modifier.height(128.dp)
        ) {
            NavigationBarItem(
                selected = currentDestination is ScreenDestination.Feed,
                onClick = { onNavigate(ScreenDestination.Feed) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(25.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HundredGramPink,
                    unselectedIconColor = HundredGramTextSecondary,
                    indicatorColor = HundredGramPink.copy(alpha = 0.15f)
                )
            )

            NavigationBarItem(
                selected = currentDestination is ScreenDestination.Explore,
                onClick = { onNavigate(ScreenDestination.Explore) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Explore",
                        modifier = Modifier.size(25.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HundredGramPink,
                    unselectedIconColor = HundredGramTextSecondary,
                    indicatorColor = HundredGramPink.copy(alpha = 0.15f)
                )
            )

            // Radiant Central Create Button
            NavigationBarItem(
                selected = false,
                onClick = { onNavigate(ScreenDestination.Camera) },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(HundredGramButtonGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White,
                    indicatorColor = Color.Transparent
                )
            )

            NavigationBarItem(
                selected = currentDestination is ScreenDestination.Reels,
                onClick = { onNavigate(ScreenDestination.Reels) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Reels",
                        modifier = Modifier.size(25.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HundredGramPink,
                    unselectedIconColor = HundredGramTextSecondary,
                    indicatorColor = HundredGramPink.copy(alpha = 0.15f)
                )
            )

            NavigationBarItem(
                selected = currentDestination is ScreenDestination.Profile,
                onClick = { onNavigate(ScreenDestination.Profile) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(25.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HundredGramPink,
                    unselectedIconColor = HundredGramTextSecondary,
                    indicatorColor = HundredGramPink.copy(alpha = 0.15f)
                )
            )
        }
    }
}

