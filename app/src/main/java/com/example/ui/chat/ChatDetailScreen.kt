package com.example.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ChatMessageEntity
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.UserAvatar
import com.example.ui.theme.HundredGramBlue
import com.example.ui.theme.HundredGramCardElevated
import com.example.ui.theme.HundredGramDarkBackground
import com.example.ui.theme.HundredGramDivider
import com.example.ui.theme.HundredGramLikeRed
import com.example.ui.theme.HundredGramPink
import com.example.ui.theme.HundredGramTextPrimary
import com.example.ui.theme.HundredGramTextSecondary

@Composable
fun ChatDetailScreen(
    viewModel: MainViewModel,
    threadId: String,
    recipientUsername: String,
    recipientAvatar: String
) {
    val context = LocalContext.current
    val allMessages by viewModel.chatMessages.collectAsState()
    val messages = allMessages[threadId] ?: emptyList()
    var messageInput by remember { mutableStateOf("") }
    
    val recordingState by viewModel.voiceRecordingState.collectAsState()
    val playbackState by viewModel.voicePlaybackState.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopVoicePlayback()
            viewModel.cancelVoiceRecording()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val started = viewModel.startVoiceRecording()
            if (!started) {
                Toast.makeText(context, "Could not start voice recording", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission required for voice message (माइक की अनुमति आवश्यक है)", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HundredGramDarkBackground)
    ) {
        // Chat Header with Voice & Video Call Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HundredGramDarkBackground)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.DirectMessages) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HundredGramTextPrimary)
            }
            UserAvatar(avatarUrl = recipientAvatar, size = 36.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipientUsername,
                    color = HundredGramTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Active now",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.Call(recipientUsername, isVideo = false)) }) {
                Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = HundredGramTextPrimary)
            }
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.Call(recipientUsername, isVideo = true)) }) {
                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = HundredGramTextPrimary)
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.messageId }) { message ->
                MessageBubble(
                    message = message,
                    playbackState = playbackState,
                    onPlayVoice = { viewModel.playVoiceMessage(message.messageId, message.mediaUrl) },
                    onReact = { emoji -> viewModel.onReactToMessage(threadId, message.messageId, emoji) }
                )
            }
        }

        // Voice Recording Banner or Message Input Bottom Bar
        if (recordingState.isRecording) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E2230))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.cancelVoiceRecording() },
                    modifier = Modifier.size(36.dp).background(Color.DarkGray, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel Recording", tint = Color.White)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(HundredGramLikeRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recording... ${String.format("%02d:%02d", recordingState.durationSeconds / 60, recordingState.durationSeconds % 60)}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.stopVoiceRecordingAndSend(threadId) },
                    modifier = Modifier.size(42.dp).background(HundredGramPink, CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send Voice Message", tint = Color.White)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Attach photo */ }) {
                    Icon(Icons.Default.Image, contentDescription = "Attach Media", tint = HundredGramTextSecondary)
                }

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Message...", color = HundredGramTextSecondary, fontSize = 13.sp) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = HundredGramTextPrimary,
                        unfocusedTextColor = HundredGramTextPrimary,
                        focusedBorderColor = HundredGramPink,
                        unfocusedBorderColor = HundredGramDivider
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(6.dp))

                if (messageInput.isNotBlank()) {
                    IconButton(
                        onClick = {
                            viewModel.onSendChatMessage(threadId, messageInput)
                            messageInput = ""
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = HundredGramPink)
                    }
                } else {
                    // Voice Record Button
                    IconButton(
                        onClick = {
                            val perm = Manifest.permission.RECORD_AUDIO
                            if (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED) {
                                val started = viewModel.startVoiceRecording()
                                if (!started) {
                                    Toast.makeText(context, "Could not start voice recording", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                permissionLauncher.launch(perm)
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(HundredGramPink.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Message (वॉयस मैसेज रिकॉर्ड करें)",
                            tint = HundredGramPink,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessageEntity,
    playbackState: com.example.data.audio.VoicePlaybackState,
    onPlayVoice: () -> Unit,
    onReact: (String) -> Unit
) {
    val isOutgoing = message.isOutgoing
    val isThisPlaying = playbackState.isPlaying && playbackState.activeMessageId == message.messageId

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        if (message.isVoiceMessage) {
            // Voice Message Audio Player Bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isOutgoing) 16.dp else 4.dp,
                            bottomEnd = if (isOutgoing) 4.dp else 16.dp
                        )
                    )
                    .background(if (isOutgoing) Color(0xFF6366F1) else HundredGramCardElevated)
                    .clickable { onReact("❤️") }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(200.dp)
                ) {
                    IconButton(
                        onClick = onPlayVoice,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isThisPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Voice Message",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${message.voiceDurationSeconds}s",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val progress = if (isThisPlaying && playbackState.durationMs > 0) {
                            (playbackState.currentPositionMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        } else {
            // Regular Text Message Bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isOutgoing) 16.dp else 4.dp,
                            bottomEnd = if (isOutgoing) 4.dp else 16.dp
                        )
                    )
                    .background(if (isOutgoing) HundredGramBlue else HundredGramCardElevated)
                    .clickable { onReact("❤️") }
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 13.5.sp
                )
            }
        }

        if (message.reaction.isNotBlank()) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .clip(CircleShape)
                    .background(HundredGramCardElevated)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(message.reaction, fontSize = 11.sp)
            }
        }
    }
}
