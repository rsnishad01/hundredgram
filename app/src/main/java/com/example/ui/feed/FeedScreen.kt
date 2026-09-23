package com.example.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Block
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.delay
import androidx.compose.material3.ripple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CommentEntity
import com.example.data.PostEntity
import com.example.data.StoryEntity
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.GradientActionButton
import com.example.ui.components.PostOptionsSheet
import com.example.ui.components.SecondaryActionButton
import com.example.ui.components.UserAvatar
import com.example.ui.components.AnimatedLoadingDots
import com.example.ui.theme.HundredGramBlue
import com.example.ui.theme.HundredGramBorderSubtle
import com.example.ui.theme.HundredGramButtonGradient
import com.example.ui.theme.HundredGramCardBackground
import com.example.ui.theme.HundredGramCardElevated
import com.example.ui.theme.HundredGramCardGlass
import com.example.ui.theme.HundredGramCardGradient
import com.example.ui.theme.HundredGramDarkBackground
import com.example.ui.theme.HundredGramDivider
import com.example.ui.theme.HundredGramLikeRed
import com.example.ui.theme.HundredGramPink
import com.example.ui.theme.HundredGramTextPrimary
import com.example.ui.theme.HundredGramTextSecondary
import com.example.ui.theme.InstagramStoryGradient
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: MainViewModel) {
    val posts by viewModel.posts.collectAsState()
    val stories by viewModel.stories.collectAsState()
    val commentsMap by viewModel.comments.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeCommentPostId by remember { mutableStateOf<String?>(null) }
    var activeOptionsPost by remember { mutableStateOf<PostEntity?>(null) }
    var activeEditPost by remember { mutableStateOf<PostEntity?>(null) }
    var reportTargetPost by remember { mutableStateOf<PostEntity?>(null) }
    var blockTargetPost by remember { mutableStateOf<PostEntity?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                viewModel.refreshFeed()
                delay(600)
                isRefreshing = false
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(HundredGramDarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Stories Row
            StoriesRow(
                stories = stories,
                currentUserAvatar = currentUser.avatarUrl,
                onAddStoryClick = { viewModel.navigateTo(ScreenDestination.Camera) },
                onStoryClick = { index -> viewModel.navigateTo(ScreenDestination.StoryViewer(index)) }
            )

            Divider(color = HundredGramDivider, thickness = 0.8.dp)

            // Nearby User Suggestions (आसपास के यूज़र्स का सुझाव)
            NearbySuggestionsRow(viewModel = viewModel)

            Divider(color = HundredGramDivider, thickness = 0.8.dp)

            if (posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .shadow(elevation = 12.dp, shape = RoundedCornerShape(22.dp), spotColor = Color(0xFFFA7E1E))
                                .clip(RoundedCornerShape(22.dp))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.4f),
                                            Color(0xFFFF007F).copy(alpha = 0.2f),
                                            Color(0xFFFA7E1E).copy(alpha = 0.2f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                        ) {
                            AsyncImage(
                                model = com.example.R.drawable.img_welcome_logo,
                                contentDescription = "Welcome Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Welcome to HundredGram",
                            color = HundredGramTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your circle of creativity and moments.\nPull down to refresh or capture your first post!",
                            color = HundredGramTextSecondary,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.navigateTo(ScreenDestination.Camera) },
                            colors = ButtonDefaults.buttonColors(containerColor = HundredGramPink),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Create Post", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Posts Feed
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(posts, key = { it.id }) { post ->
                        var showFullScreenDetail by remember { mutableStateOf(false) }
                        PostCard(
                            post = post,
                            isCurrentUser = post.authorId == currentUser.userId,
                            onLike = { viewModel.onLikePost(post.id) },
                            onBookmark = { viewModel.onBookmarkPost(post.id) },
                            onCommentClick = { activeCommentPostId = post.id },
                            onOptionsClick = { activeOptionsPost = post },
                            onAuthorClick = { viewModel.navigateTo(ScreenDestination.CreatorProfile(post.authorId)) },
                            onPostClick = { showFullScreenDetail = true }
                        )

                        if (showFullScreenDetail) {
                            FullScreenPostDialog(
                                post = post,
                                onDismiss = { showFullScreenDetail = false },
                                viewModel = viewModel,
                                currentUser = currentUser
                            )
                        }
                    }
                }
            }
        }
    }

    // Comments Sheet
    activeCommentPostId?.let { postId ->
        val postComments = commentsMap[postId] ?: emptyList()
        CommentsBottomSheet(
            comments = postComments,
            onDismiss = { activeCommentPostId = null },
            onSendComment = { text -> viewModel.onAddComment(postId, text) }
        )
    }

    // Post Options Sheet
    activeOptionsPost?.let { post ->
        PostOptionsSheet(
            post = post,
            isOwner = post.authorId == currentUser.userId,
            onDismiss = { activeOptionsPost = null },
            onSaveToggle = { viewModel.onBookmarkPost(post.id) },
            onShare = { /* Share via Android intent */ },
            onEdit = {
                activeOptionsPost = null
                activeEditPost = post
            },
            onDelete = { viewModel.onDeletePost(post.id) },
            onReport = {
                reportTargetPost = post
                activeOptionsPost = null
            },
            onBlock = {
                blockTargetPost = post
                activeOptionsPost = null
            }
        )
    }

    // Edit Post Sheet
    activeEditPost?.let { post ->
        EditPostBottomSheet(
            post = post,
            viewModel = viewModel,
            onDismiss = { activeEditPost = null },
            onSave = { newCaption, newLocation ->
                viewModel.onEditPost(post.id, newCaption, newLocation)
                activeEditPost = null
            }
        )
    }

    // Report Post Dialog
    reportTargetPost?.let { post ->
        val reasons = listOf(
            "Spam or misleading (स्पैम या भ्रामक)",
            "Hate speech or harassment (घृणास्पद भाषण / उत्पीड़न)",
            "Inappropriate imagery (अनुचित छवि / दृश्य)",
            "Violence or harmful content (हिंसा / नुकसानदेह)",
            "Intellectual property violation (कॉपीराइट उल्लंघन)"
        )
        var selectedReason by remember { mutableStateOf(reasons[0]) }

        AlertDialog(
            onDismissRequest = { reportTargetPost = null },
            containerColor = HundredGramCardBackground,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Report Post (पोस्ट रिपोर्ट करें)",
                    color = HundredGramTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Why are you reporting this post by @${post.authorUsername}?", color = HundredGramTextSecondary, fontSize = 13.sp)
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
                        viewModel.reportContent(
                            targetId = post.id,
                            targetType = "Post",
                            authorUsername = post.authorUsername,
                            reason = selectedReason
                        )
                        reportTargetPost = null
                        android.widget.Toast.makeText(
                            context,
                            "Report submitted successfully (रिपोर्ट दर्ज कर ली गई)",
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
                TextButton(onClick = { reportTargetPost = null }) {
                    Text("Cancel", color = HundredGramTextSecondary)
                }
            }
        )
    }

    // Block Creator Confirmation Dialog
    blockTargetPost?.let { post ->
        AlertDialog(
            onDismissRequest = { blockTargetPost = null },
            containerColor = HundredGramCardBackground,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Block @${post.authorUsername}?",
                    color = HundredGramTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "You will no longer see posts, reels, or stories from @${post.authorUsername}. They will be immediately removed from your feed.",
                    color = HundredGramTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.blockUser(
                            userId = post.authorId,
                            username = post.authorUsername,
                            displayName = "",
                            avatarUrl = post.authorAvatarUrl
                        )
                        blockTargetPost = null
                        android.widget.Toast.makeText(
                            context,
                            "Creator @${post.authorUsername} blocked (ब्लॉक कर दिया गया)",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.HundredGramLikeRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Block (ब्लॉक करें)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { blockTargetPost = null }) {
                    Text("Cancel", color = HundredGramTextSecondary)
                }
            }
        )
    }
}

@Composable
fun StoriesRow(
    stories: List<StoryEntity>,
    currentUserAvatar: String,
    onAddStoryClick: () -> Unit,
    onStoryClick: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Add Story item
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAddStoryClick
                )
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    UserAvatar(avatarUrl = currentUserAvatar, size = 64.dp)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(HundredGramButtonGradient)
                            .border(2.dp, HundredGramDarkBackground, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your Story",
                    color = HundredGramTextPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Friends' stories
        itemsIndexed(stories) { index, story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onStoryClick(index) }
                )
            ) {
                UserAvatar(
                    avatarUrl = story.userAvatarUrl,
                    size = 64.dp,
                    hasActiveStory = true,
                    isSeen = story.isSeen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = story.username,
                    color = if (story.isSeen) HundredGramTextSecondary else HundredGramTextPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = if (story.isSeen) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: PostEntity,
    isCurrentUser: Boolean,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onCommentClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onAuthorClick: () -> Unit = {},
    onPostClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var showHeartAnim by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (showHeartAnim) 1.25f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heartScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(HundredGramCardBackground)
            .border(1.dp, HundredGramBorderSubtle.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(bottom = 12.dp)
    ) {
        // Author Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAuthorClick
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(avatarUrl = post.authorAvatarUrl, size = 38.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorUsername,
                            color = HundredGramTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    if (post.location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = HundredGramPink,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = post.location,
                                color = HundredGramTextSecondary,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, radius = 16.dp),
                        onClick = onOptionsClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = HundredGramTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Post Image with Double-Tap to Like
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(HundredGramCardElevated)
                .combinedClickable(
                    onClick = onPostClick,
                    onDoubleClick = {
                        if (!post.isLiked) onLike()
                        coroutineScope.launch {
                            showHeartAnim = true
                            delay(700)
                            showHeartAnim = false
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            val isVideo = post.imageUrl.endsWith(".mp4", ignoreCase = true) ||
                    post.imageUrl.contains(".mp4", ignoreCase = true) ||
                    post.imageUrl.contains("video", ignoreCase = true)
            if (isVideo) {
                com.example.ui.components.VideoPlayer(
                    videoUrl = post.imageUrl,
                    isMuted = true,
                    fallbackImageUrl = post.imageUrl,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                coil.compose.SubcomposeAsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.caption,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            AnimatedLoadingDots(
                                dotColor = HundredGramPink,
                                dotSize = 8.dp,
                                spacing = 5.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF14151B)),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedLoadingDots(
                                dotColor = Color.White.copy(alpha = 0.4f),
                                dotSize = 8.dp,
                                spacing = 5.dp
                            )
                        }
                    }
                )
            }

            // Animated Heart Pop
            androidx.compose.animation.AnimatedVisibility(
                visible = showHeartAnim,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Liked",
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier
                        .size(110.dp)
                        .scale(scale)
                )
            }
        }

        // Post Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like Action with Bounce Feel
            IconButton(
                onClick = onLike,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) HundredGramLikeRed else HundredGramTextPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Comment Action
            IconButton(
                onClick = onCommentClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = HundredGramTextPrimary,
                    modifier = Modifier.size(23.dp)
                )
            }

            // Share Action
            IconButton(
                onClick = { /* Share */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = HundredGramTextPrimary,
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bookmark Action
            IconButton(
                onClick = onBookmark,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (post.isSaved) HundredGramPink else HundredGramTextPrimary,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        // Likes Count Pill
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${post.likesCount} likes",
                color = HundredGramTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
            )
        }

        // Caption
        if (post.caption.isNotBlank()) {
            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp)) {
                Text(
                    text = "${post.authorUsername} ",
                    color = HundredGramTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
                Text(
                    text = post.caption,
                    color = HundredGramTextPrimary,
                    fontSize = 13.5.sp
                )
            }
        }

        // Comments Count Callout
        if (post.commentsCount > 0) {
            Text(
                text = "View all ${post.commentsCount} comments",
                color = HundredGramTextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onCommentClick() }
                    .padding(horizontal = 14.dp, vertical = 3.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    comments: List<CommentEntity>,
    onDismiss: () -> Unit,
    onSendComment: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var commentInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HundredGramCardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .padding(16.dp)
        ) {
            Text(
                text = "Comments",
                color = HundredGramTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (comments.isEmpty()) {
                    item {
                        Text(
                            text = "No comments yet. Be the first to share your thoughts!",
                            color = HundredGramTextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }
                items(comments) { comment ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        UserAvatar(avatarUrl = comment.authorAvatarUrl, size = 32.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = comment.authorUsername,
                                color = HundredGramTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                            Text(
                                text = comment.text,
                                color = HundredGramTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Comment input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    placeholder = { Text("Add a comment...", color = HundredGramTextSecondary, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HundredGramTextPrimary,
                        unfocusedTextColor = HundredGramTextPrimary,
                        focusedBorderColor = HundredGramPink,
                        unfocusedBorderColor = HundredGramDivider
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentInput.isNotBlank()) {
                            onSendComment(commentInput)
                            commentInput = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Post Comment",
                        tint = HundredGramPink
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostBottomSheet(
    post: PostEntity,
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onSave: (newCaption: String, newLocation: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var captionText by remember { mutableStateOf(post.caption) }
    var locationText by remember { mutableStateOf(post.location) }
    var isLocating by remember { mutableStateOf(false) }

    val popularLocations = listOf(
        "Connaught Place, New Delhi",
        "Marine Drive, Mumbai",
        "Old Manali, HP",
        "Anjuna Beach, Goa",
        "Koramangala, Bengaluru"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HundredGramCardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Post",
                    color = HundredGramTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        onSave(captionText.trim(), locationText.trim())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HundredGramPink),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Post Thumbnail Preview Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HundredGramCardElevated)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Editing as @${post.authorUsername}",
                        color = HundredGramTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Update your caption or tag location",
                        color = HundredGramTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Caption Field
            OutlinedTextField(
                value = captionText,
                onValueChange = { captionText = it },
                label = { Text("Caption") },
                placeholder = { Text("Write something catchy...", color = HundredGramTextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = HundredGramTextPrimary,
                    unfocusedTextColor = HundredGramTextPrimary,
                    focusedBorderColor = HundredGramPink,
                    unfocusedBorderColor = HundredGramDivider
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Location Field with GPS Button
            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
                label = { Text("Location Tag") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = HundredGramPink
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isLocating = true
                                val loc = viewModel.getCurrentLocationName()
                                locationText = loc
                                isLocating = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Current GPS Location",
                            tint = if (isLocating) HundredGramPink else HundredGramTextSecondary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = HundredGramTextPrimary,
                    unfocusedTextColor = HundredGramTextPrimary,
                    focusedBorderColor = HundredGramPink,
                    unfocusedBorderColor = HundredGramDivider
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Popular location chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(popularLocations) { loc ->
                    SuggestionChip(
                        onClick = { locationText = loc },
                        label = { Text(loc, fontSize = 11.sp, color = HundredGramTextPrimary) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = HundredGramCardElevated
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = HundredGramDivider
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostOptionsSheet(
    post: PostEntity,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onSaveToggle: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit = {},
    onBlock: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HundredGramCardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Post Options",
                color = HundredGramTextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // If user is author, show Edit and Delete options prominently
            if (isOwner) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEdit() }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Post",
                        tint = HundredGramPink,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Edit Caption & Location",
                            color = HundredGramTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Update description or GPS tag",
                            color = HundredGramTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onDelete()
                            onDismiss()
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Post",
                        tint = com.example.ui.theme.HundredGramLikeRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Delete Post",
                        color = com.example.ui.theme.HundredGramLikeRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                Divider(
                    color = HundredGramDivider,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onSaveToggle()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save Post",
                    tint = HundredGramTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (post.isSaved) "Remove from Saved" else "Save to Collection",
                    color = HundredGramTextPrimary,
                    fontSize = 15.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onShare()
                        onDismiss()
                    }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = HundredGramTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Share Post Link",
                    color = HundredGramTextPrimary,
                    fontSize = 15.sp
                )
            }

            if (!isOwner) {
                Divider(
                    color = HundredGramDivider,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Report Post Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onReport() }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = "Report Post",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Report Post (पोस्ट रिपोर्ट करें)",
                            color = Color(0xFFFF9800),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Flag inappropriate or spam content",
                            color = HundredGramTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Block Creator Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onBlock() }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Block Creator",
                        tint = com.example.ui.theme.HundredGramLikeRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Block @${post.authorUsername} (क्रिएटर को ब्लॉक करें)",
                            color = com.example.ui.theme.HundredGramLikeRed,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Hide all posts, reels, and stories from creator",
                            color = HundredGramTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun NearbySuggestionsRow(viewModel: MainViewModel) {
    // Collect the dynamic nearby users list from the ViewModel
    val nearbyUsers by viewModel.nearbyUsers.collectAsState()
    
    // Track local follow states for suggestions
    var followedUsers by remember { mutableStateOf(setOf<String>()) }

    if (nearbyUsers.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(com.example.ui.theme.HundredGramCardBackground)
                .padding(vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nearby Creators Suggestions (आसपास के लोग)",
                    color = com.example.ui.theme.HundredGramTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "See All",
                    color = com.example.ui.theme.HundredGramPink,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { viewModel.navigateTo(ScreenDestination.Explore) }
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(nearbyUsers) { user ->
                    val isFollowed = followedUsers.contains(user.id)
                    
                    Card(
                        modifier = Modifier
                            .width(135.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.HundredGramCardElevated),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(com.example.ui.theme.HundredGramCardGlass),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = user.avatarUrl.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde" },
                                    contentDescription = user.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = user.name,
                                color = com.example.ui.theme.HundredGramTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "${user.distanceMeters}m away • ${user.bio}",
                                color = com.example.ui.theme.HundredGramTextSecondary,
                                fontSize = 10.sp, maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Button(
                                onClick = {
                                    if (isFollowed) {
                                        followedUsers = followedUsers - user.id
                                    } else {
                                        followedUsers = followedUsers + user.id
                                        // Live notification in our new Center (📢)
                                        viewModel.addNotification(
                                            type = com.example.data.NotificationType.FOLLOW,
                                            title = "Started Following",
                                            message = "You started following ${user.name} (@${user.username})",
                                            fromUser = user.username,
                                            avatar = user.avatarUrl
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowed) com.example.ui.theme.HundredGramCardGlass else com.example.ui.theme.HundredGramPink
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                            ) {
                                Text(
                                    text = if (isFollowed) "Following" else "Follow",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenPostDialog(
    post: PostEntity,
    onDismiss: () -> Unit,
    viewModel: MainViewModel,
    currentUser: com.example.data.UserData
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val isVideo = post.imageUrl.endsWith(".mp4", ignoreCase = true) ||
            post.imageUrl.contains(".mp4", ignoreCase = true) ||
            post.imageUrl.contains("video", ignoreCase = true)

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main Media Content
            if (isVideo) {
                // 9:16 full immersive mobile size
                com.example.ui.components.VideoPlayer(
                    videoUrl = post.imageUrl,
                    isMuted = false,
                    useController = true,
                    fallbackImageUrl = post.imageUrl,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Photo with Pinch-to-Zoom and Pan
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                scale = newScale
                                if (newScale > 1f) {
                                    val maxOffset = 400.dp.toPx() * (newScale - 1f)
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                        y = (offset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1.2f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    coil.compose.SubcomposeAsyncImage(
                        model = post.imageUrl,
                        contentDescription = post.caption,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                AnimatedLoadingDots(
                                    dotColor = HundredGramPink,
                                    dotSize = 9.dp,
                                    spacing = 6.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                AnimatedLoadingDots(
                                    dotColor = Color.White.copy(alpha = 0.5f),
                                    dotSize = 9.dp,
                                    spacing = 6.dp
                                )
                            }
                        }
                    )
                }
            }

            // Top Overlay (Header)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.75f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onDismiss()
                            viewModel.navigateTo(ScreenDestination.CreatorProfile(post.authorId))
                        }
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        UserAvatar(avatarUrl = post.authorAvatarUrl, size = 34.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "@${post.authorUsername}",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (post.location.isNotBlank()) {
                                Text(
                                    text = post.location,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Zoom reset indicator badge if zoomed
                    if (scale > 1.05f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable {
                                    scale = 1f
                                    offset = Offset.Zero
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Zoom ${(scale * 100).toInt()}% • Reset",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Bottom Overlay (Actions & Caption)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Column {
                    if (post.caption.isNotBlank()) {
                        Text(
                            text = post.caption,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.onLikePost(post.id) }) {
                                Icon(
                                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (post.isLiked) com.example.ui.theme.HundredGramLikeRed else Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "${post.likesCount} likes",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(onClick = { viewModel.onBookmarkPost(post.id) }) {
                            Icon(
                                imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


