package com.example.ui.call

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ui.MainViewModel
import com.example.ui.components.UserAvatar
import com.example.ui.theme.HundredGramCardElevated
import com.example.ui.theme.HundredGramLikeRed
import com.example.ui.theme.HundredGramPink
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    viewModel: MainViewModel,
    callName: String,
    isVideo: Boolean
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }
    var isVideoEnabled by remember { mutableStateOf(isVideo) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var durationSeconds by remember { mutableIntStateOf(0) }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (isVideo) permissions.add(Manifest.permission.CAMERA)
        callPermissionLauncher.launch(permissions.toTypedArray())
    }

    val pulseAnim = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        pulseAnim.animateTo(
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            durationSeconds++
        }
    }

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val formattedDuration = String.format("%02d:%02d", minutes, seconds)
    val currentUser by viewModel.currentUser.collectAsState()
    val callerAvatar = ""

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090B10))
    ) {
        if (isVideoEnabled) {
            // Video stream surface representation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                                Color(0xFF020617)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                UserAvatar(
                    avatarUrl = callerAvatar,
                    size = 110.dp
                )
            }
            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Local user PIP preview window (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 20.dp)
                    .size(width = 100.dp, height = 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, HundredGramPink, RoundedCornerShape(16.dp))
                    .background(Color(0xFF131722)),
                contentAlignment = Alignment.Center
            ) {
                if (currentUser.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = currentUser.avatarUrl,
                        contentDescription = "My self preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    UserAvatar(
                        avatarUrl = "",
                        size = 48.dp
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isFrontCamera) "Front" else "Rear",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Header / Caller status Info
        Column(
            modifier = Modifier
                .align(if (isVideoEnabled) Alignment.TopStart else Alignment.Center)
                .padding(
                    top = if (isVideoEnabled) 50.dp else 0.dp,
                    start = if (isVideoEnabled) 24.dp else 0.dp,
                    bottom = if (isVideoEnabled) 0.dp else 80.dp
                ),
            horizontalAlignment = if (isVideoEnabled) Alignment.Start else Alignment.CenterHorizontally
        ) {
            if (!isVideoEnabled) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseAnim.value)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(HundredGramPink.copy(alpha = 0.35f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatar(
                        avatarUrl = callerAvatar,
                        size = 110.dp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = callName,
                color = Color.White,
                fontSize = if (isVideoEnabled) 20.sp else 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$formattedDuration • Connected",
                color = if (isVideoEnabled) Color.White.copy(alpha = 0.8f) else Color(0xFF10B981),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom Controls Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 40.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF1E2433).copy(alpha = 0.95f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute / Unmute Mic
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color.White else Color(0xFF2E384D))
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (isMuted) Color.Black else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Video On / Off
                IconButton(
                    onClick = { isVideoEnabled = !isVideoEnabled },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (!isVideoEnabled) Color.White else Color(0xFF2E384D))
                ) {
                    Icon(
                        imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                        contentDescription = "Video",
                        tint = if (!isVideoEnabled) Color.Black else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Switch Camera (if Video enabled)
                if (isVideoEnabled) {
                    IconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E384D))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Speaker Toggle
                IconButton(
                    onClick = { isSpeakerOn = !isSpeakerOn },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isSpeakerOn) Color(0xFF2E384D) else Color.White)
                ) {
                    Icon(
                        imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Speaker",
                        tint = if (isSpeakerOn) Color.White else Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // End Call Button
                IconButton(
                    onClick = {
                        viewModel.onEndCall(
                            callerName = callName,
                            callerAvatar = callerAvatar,
                            isVideo = isVideo,
                            durationSeconds = durationSeconds,
                            isOutgoing = true
                        )
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(HundredGramLikeRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
