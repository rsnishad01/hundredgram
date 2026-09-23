package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PostEntity
import com.example.data.ReelVideo
import com.example.data.UserData
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.UserAvatar
import com.example.ui.feed.FeedScreen
import com.example.ui.feed.PostCard
import com.example.ui.theme.HundredGramBorderSubtle
import com.example.ui.theme.HundredGramCardBackground
import com.example.ui.theme.HundredGramCardElevated
import com.example.ui.theme.HundredGramDarkBackground
import com.example.ui.theme.HundredGramPink
import com.example.ui.theme.HundredGramTextPrimary
import com.example.ui.theme.HundredGramTextSecondary
import kotlinx.coroutines.launch

@Composable
fun CreatorProfileScreen(viewModel: MainViewModel, userId: String) {
    var creator by remember { mutableStateOf<UserData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val posts by viewModel.posts.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val isFollowingMap by viewModel.isFollowingMap.collectAsState()
    val isFollowing = isFollowingMap[userId] ?: false

    // Load creator profile & check following status
    LaunchedEffect(userId) {
        isLoading = true
        val loaded = viewModel.repository.firestoreRepository.getUserFromFirestore(userId)
        if (loaded != null) {
            creator = loaded
        } else {
            // Offline fallback from local posts authors
            val matchingPost = posts.firstOrNull { it.authorId == userId }
            if (matchingPost != null) {
                creator = UserData(
                    userId = userId,
                    username = matchingPost.authorUsername,
                    displayName = matchingPost.authorUsername,
                    avatarUrl = matchingPost.authorAvatarUrl,
                    bio = "Creator on HundredGram",
                    followersCount = 0,
                    followingCount = 0,
                    postsCount = 0
                )
            }
        }
        viewModel.checkIfFollowing(userId)
        isLoading = false
    }

    val creatorPosts = remember(posts, userId) {
        posts.filter { it.authorId == userId }
    }

    val creatorReels = remember(reels, userId) {
        reels.filter { it.authorId == userId }
    }

    var selectedPreviewPost by remember { mutableStateOf<PostEntity?>(null) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showBlockConfirmDialog by remember { mutableStateOf(false) }
    var showReportCreatorDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HundredGramDarkBackground)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateTo(ScreenDestination.Feed) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HundredGramTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = creator?.username ?: "Profile",
                        color = HundredGramTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Three line / menu system on top right
                Box {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                            contentDescription = "Options Menu",
                            tint = HundredGramTextPrimary
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false },
                        modifier = Modifier.background(HundredGramCardBackground)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Block Creator (क्रिएटर को ब्लॉक करें)",
                                    color = com.example.ui.theme.HundredGramLikeRed,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                showOptionsMenu = false
                                showBlockConfirmDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Report Creator (क्रिएटर को रिपोर्ट करें)",
                                    color = HundredGramTextPrimary
                                )
                            },
                            onClick = {
                                showOptionsMenu = false
                                showReportCreatorDialog = true
                            }
                        )
                    }
                }
            }
        },
        containerColor = HundredGramDarkBackground
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = HundredGramPink)
            }
        } else if (creator == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Creator profile not found.", color = HundredGramTextSecondary)
            }
        } else {
            val user = creator!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Profile stats panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(HundredGramCardBackground)
                        .border(1.dp, HundredGramBorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            avatarUrl = user.avatarUrl,
                            size = 76.dp,
                            isVerified = user.isVerified
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            CreatorProfileStatColumn(count = creatorPosts.size, label = "Posts")
                            CreatorProfileStatColumn(count = user.followersCount, label = "Followers")
                            CreatorProfileStatColumn(count = user.followingCount, label = "Following")
                        }
                    }
                }

                // Profile Info
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.displayName,
                            color = HundredGramTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF3897F0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (user.bio.isNotBlank()) {
                        Text(
                            text = user.bio,
                            color = HundredGramTextPrimary,
                            fontSize = 13.5.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    if (user.website.isNotBlank()) {
                        Text(
                            text = user.website,
                            color = Color(0xFF3897F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Action Buttons: Follow and Message
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.toggleFollowUser(user.userId) { nowFollowing ->
                                creator = creator?.copy(
                                    followersCount = if (nowFollowing) user.followersCount + 1 else (user.followersCount - 1).coerceAtLeast(0)
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color.DarkGray else HundredGramPink
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Following" else "Follow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.navigateTo(
                                ScreenDestination.ChatDetail(
                                    threadId = user.userId,
                                    recipientUsername = user.displayName.ifBlank { user.username },
                                    recipientAvatar = user.avatarUrl
                                )
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HundredGramCardElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HundredGramBorderSubtle),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Message",
                            tint = HundredGramTextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Message",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = HundredGramTextPrimary
                        )
                    }
                }

                Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 0.8.dp)

                // Grid tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = HundredGramPink,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = HundredGramPink
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Posts")
                        }},
                        selectedContentColor = HundredGramPink,
                        unselectedContentColor = HundredGramTextSecondary
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reels")
                        }},
                        selectedContentColor = HundredGramPink,
                        unselectedContentColor = HundredGramTextSecondary
                    )
                }

                // Grid content
                if (selectedTabIndex == 0) {
                    if (creatorPosts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No posts shared yet", color = HundredGramTextSecondary)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(1.dp)
                        ) {
                            items(creatorPosts) { post ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .padding(1.dp)
                                        .background(HundredGramCardElevated)
                                        .clickable { selectedPreviewPost = post }
                                ) {
                                    AsyncImage(
                                        model = post.imageUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    if (creatorReels.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No reels shared yet", color = HundredGramTextSecondary)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(1.dp)
                        ) {
                            items(creatorReels) { reel ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(0.6f)
                                        .padding(1.dp)
                                        .background(HundredGramCardElevated)
                                        .clickable {
                                            viewModel.navigateTo(ScreenDestination.Reels)
                                        }
                                ) {
                                    AsyncImage(
                                        model = reel.thumbnailUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Block Confirmation Dialog
    if (showBlockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBlockConfirmDialog = false },
            containerColor = HundredGramCardBackground,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Block @${creator?.username ?: "Creator"}?",
                    color = HundredGramTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "They will not be able to interact with you, and their posts, reels, and stories will be removed from your feed.",
                    color = HundredGramTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBlockConfirmDialog = false
                        viewModel.blockUser(
                            userId = userId,
                            username = creator?.username ?: "Creator",
                            displayName = creator?.displayName ?: "",
                            avatarUrl = creator?.avatarUrl ?: ""
                        )
                        android.widget.Toast.makeText(
                            context,
                            "User @${creator?.username ?: "creator"} blocked (ब्लॉक कर दिया गया)",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        viewModel.navigateTo(ScreenDestination.Feed)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.HundredGramLikeRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Block (ब्लॉक करें)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockConfirmDialog = false }) {
                    Text("Cancel", color = HundredGramTextSecondary)
                }
            }
        )
    }

    // Report Creator Dialog
    if (showReportCreatorDialog) {
        val reasons = listOf(
            "Spam or fake profile (स्पैम या फर्जी प्रोफ़ाइल)",
            "Hate speech or harassment (घृणास्पद भाषण / उत्पीड़न)",
            "Inappropriate content (अनुचित सामग्री)",
            "Impersonation (किसी अन्य व्यक्ति की नकल)",
            "Violence or dangerous content (हिंसा / खतरनाक सामग्री)"
        )
        var selectedReason by remember { mutableStateOf(reasons[0]) }

        AlertDialog(
            onDismissRequest = { showReportCreatorDialog = false },
            containerColor = HundredGramCardBackground,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Report @${creator?.username ?: "Creator"}",
                    color = HundredGramTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select a reason for reporting:", color = HundredGramTextSecondary, fontSize = 13.sp)
                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedReason == reason) HundredGramPink.copy(alpha = 0.2f) else HundredGramCardElevated)
                                .clickable { selectedReason = reason }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                colors = RadioButtonDefaults.colors(selectedColor = HundredGramPink)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(reason, color = HundredGramTextPrimary, fontSize = 12.5.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportCreatorDialog = false
                        viewModel.reportContent(
                            targetId = userId,
                            targetType = "Creator",
                            authorUsername = creator?.username ?: "Creator",
                            reason = selectedReason
                        )
                        android.widget.Toast.makeText(
                            context,
                            "Report submitted for review (रिपोर्ट दर्ज कर ली गई)",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HundredGramPink),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Report", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportCreatorDialog = false }) {
                    Text("Cancel", color = HundredGramTextSecondary)
                }
            }
        )
    }
}

@Composable
fun CreatorProfileStatColumn(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count.toString(),
            color = HundredGramTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = HundredGramTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
