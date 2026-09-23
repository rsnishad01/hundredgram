package com.example.ui.reels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.data.ReelVideo
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.feed.CommentsBottomSheet
import com.example.ui.components.UserAvatar
import com.example.ui.components.AnimatedLoadingDots
import com.example.ui.theme.HundredGramCardElevated
import com.example.ui.theme.HundredGramLikeRed
import com.example.ui.theme.HundredGramPink
import com.example.ui.theme.HundredGramTextPrimary

@Composable
fun ReelsScreen(viewModel: MainViewModel) {
    val reels by viewModel.reels.collectAsState()
    val playbackInfo by viewModel.audioPlaybackInfo.collectAsState()
    val commentsMap by viewModel.comments.collectAsState()
    var activeCommentReelId by remember { mutableStateOf<String?>(null) }
    var activeShareReel by remember { mutableStateOf<ReelVideo?>(null) }
    val context = LocalContext.current

    if (reels.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0F14)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(HundredGramPink.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = HundredGramPink,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Reels Yet",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Share short video clips with music and trending audio.",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { viewModel.navigateTo(ScreenDestination.CreateReel) },
                    colors = ButtonDefaults.buttonColors(containerColor = HundredGramPink),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Your First Reel", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { reels.size })

    // Auto-play original audio of the active reel on page change
    LaunchedEffect(pagerState.currentPage, reels) {
        if (reels.isNotEmpty() && pagerState.currentPage in reels.indices) {
            val activeReel = reels[pagerState.currentPage]
            viewModel.playReelAudio(activeReel)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopAudio()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val reel = reels[page]
            ReelItem(
                reel = reel,
                isMuted = playbackInfo.isMuted,
                onLike = { viewModel.onLikeReel(reel.id) },
                onComment = { activeCommentReelId = reel.id },
                onBookmark = { viewModel.onBookmarkReel(reel.id) },
                onShare = { activeShareReel = reel },
                onMuteToggle = { viewModel.toggleMuteAudio() },
                onAuthorClick = { viewModel.navigateTo(ScreenDestination.CreatorProfile(reel.authorId)) },
                onTogglePlayAudio = { viewModel.togglePlayPauseAudio() }
            )
        }

        // Top Create Reel Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reels",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { viewModel.navigateTo(ScreenDestination.CreateReel) },
                colors = ButtonDefaults.buttonColors(containerColor = HundredGramPink),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Create +", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Comments Sheet
        activeCommentReelId?.let { reelId ->
            val reelComments = commentsMap[reelId] ?: emptyList()
            CommentsBottomSheet(
                comments = reelComments,
                onDismiss = { activeCommentReelId = null },
                onSendComment = { text -> viewModel.onAddComment(reelId, text) }
            )
        }

        // Reel Share & Options Sheet
        activeShareReel?.let { reel ->
            ReelShareBottomSheet(
                reel = reel,
                onDismiss = { activeShareReel = null },
                onCopyLink = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Reel Link", "https://hundredgram.app/reel/${reel.id}")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Link copied to clipboard (लिंक कॉपी कर लिया गया)", Toast.LENGTH_SHORT).show()
                    activeShareReel = null
                },
                onShareSystem = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out this reel by @${reel.authorUsername} on HundredGram: https://hundredgram.app/reel/${reel.id}")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Reel")
                    context.startActivity(shareIntent)
                    activeShareReel = null
                },
                onBookmark = {
                    viewModel.onBookmarkReel(reel.id)
                    Toast.makeText(context, if (reel.isSaved) "Removed from saved" else "Saved to collection", Toast.LENGTH_SHORT).show()
                    activeShareReel = null
                },
                onReport = {
                    viewModel.reportContent(reel.id, "Reel", reel.authorUsername, "Inappropriate Content")
                    Toast.makeText(context, "Report submitted (रिपोर्ट दर्ज कर ली गई)", Toast.LENGTH_SHORT).show()
                    activeShareReel = null
                }
            )
        }
    }
}

@Composable
fun ReelItem(
    reel: ReelVideo,
    isMuted: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit = {},
    onMuteToggle: () -> Unit,
    onAuthorClick: () -> Unit = {},
    onTogglePlayAudio: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(true) }
    var showPauseIndicator by remember { mutableStateOf(false) }

    // Auto-hide indicator if resumed
    LaunchedEffect(isPlaying, showPauseIndicator) {
        if (isPlaying && showPauseIndicator) {
            kotlinx.coroutines.delay(650)
            showPauseIndicator = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                isPlaying = !isPlaying
                showPauseIndicator = true
                onTogglePlayAudio()
            }
    ) {
        // Reel Video / Thumbnail Background
        val mediaToPlay = reel.videoUrl.ifBlank { reel.thumbnailUri }
        val fallbackThumb = reel.thumbnailUri.ifBlank { reel.videoUrl }

        if (mediaToPlay.isNotBlank()) {
            com.example.ui.components.VideoPlayer(
                videoUrl = mediaToPlay,
                isMuted = isMuted,
                isPlaying = isPlaying,
                fallbackImageUrl = fallbackThumb,
                modifier = Modifier.fillMaxSize()
            )
        } else {
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

        // Center Play / Pause Indicator Button
        if (!isPlaying || showPauseIndicator) {
            IconButton(
                onClick = {
                    isPlaying = !isPlaying
                    onTogglePlayAudio()
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(68.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause Reel" else "Play Reel",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        // Top-right Mute / Unmute Button
        IconButton(
            onClick = onMuteToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 16.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                contentDescription = "Mute Toggle",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Right Action Bar (Likes, Comments, Shares, Bookmarks, Audio)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (reel.isLiked) HundredGramLikeRed else Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Text("${reel.likesCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onComment) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text("${reel.commentsCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            IconButton(onClick = onBookmark) {
                Icon(
                    imageVector = if (reel.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Audio Disc",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom Left Details (Author, Caption, Audio Title)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.8f)
                .padding(start = 16.dp, bottom = 90.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onAuthorClick() }
            ) {
                Text(
                    text = "@${reel.authorUsername}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (reel.caption.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reel.caption,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio track ticker
            if (reel.audioTitle.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (reel.audioArtist.isNotBlank()) "${reel.audioTitle} • ${reel.audioArtist}" else reel.audioTitle,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelShareBottomSheet(
    reel: ReelVideo,
    onDismiss: () -> Unit,
    onCopyLink: () -> Unit,
    onShareSystem: () -> Unit,
    onBookmark: () -> Unit,
    onReport: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1F29)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Share Reel",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Copy Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCopyLink() }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Link",
                    tint = HundredGramPink,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Copy Link (लिंक कॉपी करें)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "https://hundredgram.app/reel/${reel.id}",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            // Share via Apps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onShareSystem() }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Share to other Apps (शेयर करें)",
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            // Save Reel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onBookmark() }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (reel.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save Reel",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (reel.isSaved) "Remove from Saved" else "Save Reel to Collection",
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            // Report Reel
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
                    contentDescription = "Report",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Report Reel (रील रिपोर्ट करें)",
                    color = Color(0xFFFF9800),
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
