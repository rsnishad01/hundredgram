package com.example.ui.chat

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
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallDirection
import com.example.data.CallLogItem
import com.example.data.CallType
import com.example.data.ChatThreadEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DirectMessagesScreen(viewModel: MainViewModel) {
    val threads by viewModel.chatThreads.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchInput by remember { mutableStateOf("") }

    val filteredThreads = remember(searchInput, threads) {
        if (searchInput.isBlank()) threads
        else threads.filter { it.recipientUsername.contains(searchInput, true) }
    }

    val filteredCalls = remember(searchInput, callLogs) {
        if (searchInput.isBlank()) callLogs
        else callLogs.filter { it.callerName.contains(searchInput, true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HundredGramDarkBackground)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.Feed) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HundredGramTextPrimary)
            }
            Text(
                text = currentUser.username,
                color = HundredGramTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (selectedTabIndex == 1 && callLogs.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearCallHistory() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Calls", tint = HundredGramTextSecondary)
                }
            } else {
                IconButton(onClick = { /* New conversation */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "New Chat", tint = HundredGramTextPrimary)
                }
            }
        }

        // Tab Row: Messages vs Calls
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = HundredGramDarkBackground,
            contentColor = HundredGramPink,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = HundredGramPink,
                    height = 2.5.dp
                )
            },
            divider = {
                Divider(color = HundredGramDivider, thickness = 0.5.dp)
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "Messages",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTabIndex == 0) HundredGramTextPrimary else HundredGramTextSecondary,
                        fontSize = 14.sp
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Calls",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == 1) HundredGramTextPrimary else HundredGramTextSecondary,
                            fontSize = 14.sp
                        )
                        if (callLogs.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(HundredGramPink)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${callLogs.size}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            )
        }

        // Search Input Bar
        OutlinedTextField(
            value = searchInput,
            onValueChange = { searchInput = it },
            placeholder = {
                Text(
                    text = if (selectedTabIndex == 0) "Search messages and friends..." else "Search call history...",
                    color = HundredGramTextSecondary,
                    fontSize = 13.sp
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HundredGramTextSecondary) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = HundredGramTextPrimary,
                unfocusedTextColor = HundredGramTextPrimary,
                focusedBorderColor = HundredGramPink,
                unfocusedBorderColor = HundredGramDivider
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )

        if (selectedTabIndex == 0) {
            // Direct Messages Threads List
            if (filteredThreads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = HundredGramTextSecondary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchInput.isBlank()) "No Messages Yet" else "No messages matching \"$searchInput\"",
                            color = HundredGramTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Direct messages with creators and friends will appear here in real time.",
                            color = HundredGramTextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredThreads) { thread ->
                        ChatThreadItem(
                            thread = thread,
                            onClick = {
                                viewModel.navigateTo(
                                    ScreenDestination.ChatDetail(
                                        threadId = thread.threadId,
                                        recipientUsername = thread.recipientUsername,
                                        recipientAvatar = thread.recipientAvatarUrl
                                    )
                                )
                            }
                        )
                        Divider(color = HundredGramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 74.dp))
                    }
                }
            }
        } else {
            // Call History Logs
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (filteredCalls.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = HundredGramTextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No call logs yet",
                                color = HundredGramTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                items(filteredCalls) { callItem ->
                    CallLogItemRow(
                        callItem = callItem,
                        onVoiceCall = {
                            viewModel.initiateCall(callItem.callerName, isVideo = false)
                        },
                        onVideoCall = {
                            viewModel.initiateCall(callItem.callerName, isVideo = true)
                        }
                    )
                    Divider(color = HundredGramDivider, thickness = 0.5.dp, modifier = Modifier.padding(start = 74.dp))
                }
            }
        }
    }
}

@Composable
fun CallLogItemRow(
    callItem: CallLogItem,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeFormatted = dateFormat.format(Date(callItem.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(avatarUrl = callItem.callerAvatar, size = 48.dp)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = callItem.callerName,
                color = if (callItem.direction == CallDirection.MISSED) HundredGramLikeRed else HundredGramTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val (directionIcon, tint) = when (callItem.direction) {
                    CallDirection.INCOMING -> Icons.Default.CallReceived to Color(0xFF10B981)
                    CallDirection.OUTGOING -> Icons.Default.CallMade to HundredGramTextSecondary
                    CallDirection.MISSED -> Icons.Default.CallMissed to HundredGramLikeRed
                }
                Icon(
                    imageVector = directionIcon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when {
                        callItem.direction == CallDirection.MISSED -> "Missed • $timeFormatted"
                        callItem.durationSeconds > 0 -> "${callItem.durationSeconds / 60}m ${callItem.durationSeconds % 60}s • $timeFormatted"
                        else -> timeFormatted
                    },
                    color = HundredGramTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Quick Call Action Icons
        IconButton(onClick = onVoiceCall) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Voice Call",
                tint = HundredGramPink,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onVideoCall) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "Video Call",
                tint = HundredGramPink,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun ChatThreadItem(
    thread: ChatThreadEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            UserAvatar(avatarUrl = thread.recipientAvatarUrl, size = 48.dp)
            if (thread.isOnline) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = thread.recipientUsername,
                color = HundredGramTextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = thread.lastMessageText,
                color = if (thread.unreadCount > 0) HundredGramTextPrimary else HundredGramTextSecondary,
                fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.5.sp,
                maxLines = 1
            )
        }

        if (thread.unreadCount > 0) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(HundredGramBlue)
            )
        }
    }
}

