package com.example.ui.filters

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.MainViewModel
import com.example.ui.ScreenDestination
import com.example.ui.theme.HundredGramCardBackground
import com.example.ui.theme.HundredGramCardElevated
import com.example.ui.theme.HundredGramDarkBackground
import com.example.ui.theme.HundredGramDivider
import com.example.ui.theme.HundredGramPink
import com.example.ui.theme.HundredGramTextPrimary
import com.example.ui.theme.HundredGramTextSecondary
import kotlinx.coroutines.launch

@Composable
fun FilterStudioScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val selectedImageUri by viewModel.selectedFilterImageUri.collectAsState()
    val filters = remember { PhotoFilter.getAllFilters() }
    var selectedFilter by remember { mutableStateOf(filters.first()) }
    var captionInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }
    var suggestedLocations by remember { mutableStateOf<List<String>>(emptyList()) }

    fun fetchCurrentLocation() {
        coroutineScope.launch {
            isLocating = true
            val loc = viewModel.getCurrentLocationName()
            if (loc.isNotBlank() && loc != "Location unavailable") {
                locationInput = loc
            }
            suggestedLocations = viewModel.getPopularLocations()
            isLocating = false
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            fetchCurrentLocation()
        } else {
            isLocating = false
        }
    }

    fun requestLocationWithPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchCurrentLocation()
        } else {
            isLocating = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.setSelectedFilterImage(uri)
        }
    }

    LaunchedEffect(Unit) {
        suggestedLocations = viewModel.getPopularLocations()
    }

    val imageModel = selectedImageUri

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HundredGramDarkBackground)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo(ScreenDestination.Camera) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HundredGramTextPrimary)
            }
            Text(
                text = "Filter Studio",
                color = HundredGramTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (imageModel != null) {
                        viewModel.createPost(imageModel, captionInput, locationInput)
                    }
                },
                enabled = imageModel != null,
                colors = ButtonDefaults.buttonColors(containerColor = HundredGramPink),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Share Post", fontWeight = FontWeight.Bold)
            }
        }

        // Filtered Image Preview Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(HundredGramCardElevated)
                .clickable {
                    photoPickerLauncher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = "Filtered Preview",
                    colorFilter = ColorFilter.colorMatrix(selectedFilter.colorMatrix),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Change Photo Button Overlay
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .clickable { photoPickerLauncher.launch("image/*") }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Change photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Change",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(HundredGramPink.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Select Photo",
                            tint = HundredGramPink,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Select Photo to Edit",
                        color = HundredGramTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Choose any photo from your gallery to apply custom studio filters and effects",
                        color = HundredGramTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = HundredGramPink),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Filters Carousel
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter.id == filter.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedFilter = filter }
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) HundredGramPink else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        if (imageModel != null) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = filter.name,
                                colorFilter = ColorFilter.colorMatrix(filter.colorMatrix),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val filterGradient = when (filter.type) {
                                FilterPresetType.NORMAL -> Brush.linearGradient(listOf(Color(0xFF2C2F3A), Color(0xFF1E2028)))
                                FilterPresetType.VINTAGE -> Brush.linearGradient(listOf(Color(0xFF8D6E63), Color(0xFF4E342E)))
                                FilterPresetType.SEPIA -> Brush.linearGradient(listOf(Color(0xFFD7CCC8), Color(0xFF795548)))
                                FilterPresetType.CYBERPUNK -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF007F)))
                                FilterPresetType.MONOCHROME -> Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFF212121)))
                                FilterPresetType.SUNSET -> Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFE91E63)))
                                else -> Brush.linearGradient(listOf(Color(0xFF673AB7), Color(0xFF3F51B5)))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(filterGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = filter.name,
                                    tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = filter.name,
                        color = if (isSelected) HundredGramPink else HundredGramTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Caption & Location Inputs
        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            OutlinedTextField(
                value = captionInput,
                onValueChange = { captionInput = it },
                label = { Text("Write a caption...") },
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
            OutlinedTextField(
                value = locationInput,
                onValueChange = { locationInput = it },
                label = { Text("Add Location") },
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
                            requestLocationWithPermission()
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

            if (suggestedLocations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestedLocations) { loc ->
                        SuggestionChip(
                            onClick = { locationInput = loc },
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
            }
        }
    }
}
