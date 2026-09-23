package com.example.ui.reels

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.data.audio.ComprehensiveAudioTrack
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.theme.HundredGramButtonGradient
import com.example.ui.theme.HundredGramCardBackground
import com.example.ui.theme.HundredGramCardElevated
import com.example.ui.theme.HundredGramDarkBackground
import com.example.ui.theme.HundredGramDivider
import com.example.ui.theme.HundredGramPink
import com.example.ui.theme.HundredGramTextPrimary
import com.example.ui.theme.HundredGramTextSecondary
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream

@Composable
fun CreateReelScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val vmSelectedReelUri by viewModel.selectedReelUri.collectAsState()
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(vmSelectedReelUri) {
        if (vmSelectedReelUri != null) {
            selectedVideoUri = vmSelectedReelUri
        }
    }

    var caption by remember { mutableStateOf("") }
    var selectedAudioTrack by remember { mutableStateOf<ComprehensiveAudioTrack?>(null) }
    var showMusicPicker by remember { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
            viewModel.selectedReelUri.value = uri
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HundredGramDarkBackground)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                viewModel.selectedReelUri.value = null
                viewModel.navigateTo(ScreenDestination.Reels)
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = HundredGramTextPrimary
                )
            }
            Text(
                text = "New Reel",
                color = HundredGramTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    val uri = selectedVideoUri ?: return@Button
                    val audioTitle = selectedAudioTrack?.title ?: ""
                    val audioArtist = selectedAudioTrack?.artist ?: ""
                    viewModel.createReel(uri, caption, audioTitle, audioArtist)
                },
                enabled = selectedVideoUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = HundredGramPink),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Share Reel", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Video Viewfinder / Editing Area
        if (selectedVideoUri != null) {
            // Video Thumbnail Box (Ready to share!)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(HundredGramCardElevated)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedVideoUri,
                    contentDescription = "Video preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Clear / Retake button
                IconButton(
                    onClick = { 
                        selectedVideoUri = null
                        viewModel.selectedReelUri.value = null
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Discard Video",
                        tint = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🎬 Video Clip Ready",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Big button to pick from files
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clickable { videoPickerLauncher.launch("video/*") },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = HundredGramCardBackground)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Select video",
                        tint = HundredGramPink,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Select a Video from Gallery",
                        color = HundredGramTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Or record using Camera modes",
                        color = HundredGramTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Music Soundtrack Selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showMusicPicker = true },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = HundredGramCardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(HundredGramButtonGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Audio",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedAudioTrack?.title ?: "Add Background Music & Sound (Optional)",
                        color = HundredGramTextPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = selectedAudioTrack?.artist ?: "Tap to choose audio file or beat",
                        color = HundredGramTextSecondary,
                        fontSize = 12.sp
                    )
                }
                if (selectedAudioTrack != null) {
                    IconButton(
                        onClick = { selectedAudioTrack = null },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Audio",
                            tint = HundredGramTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Caption Input
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            placeholder = { Text("Write a caption... #reels #trending") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = HundredGramTextPrimary,
                unfocusedTextColor = HundredGramTextPrimary,
                focusedBorderColor = HundredGramPink,
                unfocusedBorderColor = HundredGramDivider,
                focusedContainerColor = HundredGramCardBackground,
                unfocusedContainerColor = HundredGramCardBackground
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showMusicPicker) {
        MusicSearchPickerSheet(
            viewModel = viewModel,
            onDismiss = { showMusicPicker = false },
            onTrackSelected = { track ->
                selectedAudioTrack = track
                showMusicPicker = false
            }
        )
    }
}
