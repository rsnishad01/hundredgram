package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CallLogItem
import com.example.data.ChatMessageEntity
import com.example.data.ChatThreadEntity
import com.example.data.CommentEntity
import com.example.data.PostEntity
import com.example.data.ReelVideo
import com.example.data.SocialRepository
import com.example.data.StoryEntity
import com.example.data.UserData
import com.example.data.audio.ComprehensiveAudioTrack
import com.example.data.nearby.NearbyEvent
import com.example.data.nearby.NearbyUser
import com.example.data.network.NetworkStatus
import com.example.data.storage.UploadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class ScreenDestination {
    object Auth : ScreenDestination()
    object Feed : ScreenDestination()
    object Explore : ScreenDestination()
    object Reels : ScreenDestination()
    object DirectMessages : ScreenDestination()
    data class ChatDetail(val threadId: String, val recipientUsername: String, val recipientAvatar: String) : ScreenDestination()
    data class StoryViewer(val storyIndex: Int) : ScreenDestination()
    object Camera : ScreenDestination()
    object FilterStudio : ScreenDestination()
    object CreateReel : ScreenDestination()
    object Profile : ScreenDestination()
    data class Call(val callName: String, val isVideo: Boolean) : ScreenDestination()
    object LiveRoom : ScreenDestination()
    object Notifications : ScreenDestination()
    data class CreatorProfile(val userId: String) : ScreenDestination()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository = SocialRepository(application)

    val isLoggedIn: StateFlow<Boolean> = repository.isLoggedIn
    val currentUser: StateFlow<UserData> = repository.currentUser
    val posts: StateFlow<List<PostEntity>> = repository.posts
    val stories: StateFlow<List<StoryEntity>> = repository.stories
    val reels: StateFlow<List<ReelVideo>> = repository.reels
    val chatThreads: StateFlow<List<ChatThreadEntity>> = repository.chatThreads
    val chatMessages: StateFlow<Map<String, List<ChatMessageEntity>>> = repository.chatMessages
    val comments: StateFlow<Map<String, List<CommentEntity>>> = repository.comments
    val callLogs: StateFlow<List<CallLogItem>> = repository.callLogs
    val audioPlaybackInfo = repository.reelAudioService.playbackInfo
    val voiceRecordingState = repository.voiceMessageManager.recordingState
    val voicePlaybackState = repository.voiceMessageManager.playbackState

    val networkStatus: StateFlow<NetworkStatus> = repository.connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkStatus.Available)

    private val _networkSpeed = MutableStateFlow("0.0 KB/s")
    val networkSpeed: StateFlow<String> = _networkSpeed.asStateFlow()

    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Feed)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _uploadMessage = MutableStateFlow("Uploading...")
    val uploadMessage: StateFlow<String> = _uploadMessage.asStateFlow()

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError.asStateFlow()

    private val _showLoginPrompt = MutableStateFlow(false)
    val showLoginPrompt: StateFlow<Boolean> = _showLoginPrompt.asStateFlow()

    private val _loginPromptReason = MutableStateFlow("इस एक्शन के लिए पहले लॉगिन करें")
    val loginPromptReason: StateFlow<String> = _loginPromptReason.asStateFlow()

    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers.asStateFlow()

    private val _nearbyEvents = MutableStateFlow<List<NearbyEvent>>(emptyList())
    val nearbyEvents: StateFlow<List<NearbyEvent>> = _nearbyEvents.asStateFlow()

    private val _searchedUsers = MutableStateFlow<List<com.example.data.UserData>>(emptyList())
    val searchedUsers: StateFlow<List<com.example.data.UserData>> = _searchedUsers.asStateFlow()

    private val _isSearchingUsers = MutableStateFlow(false)
    val isSearchingUsers: StateFlow<Boolean> = _isSearchingUsers.asStateFlow()

    private val _recommendedCreators = MutableStateFlow<List<com.example.data.UserData>>(emptyList())
    val recommendedCreators: StateFlow<List<com.example.data.UserData>> = _recommendedCreators.asStateFlow()

    private val _selectedFilterImageUri = MutableStateFlow<Uri?>(null)
    val selectedFilterImageUri: StateFlow<Uri?> = _selectedFilterImageUri.asStateFlow()

    val autoStartReelRecording = MutableStateFlow(false)
    val selectedReelUri = MutableStateFlow<Uri?>(null)

    init {
        loadNearbyData()
        loadRecommendedCreators()
        


        // Real-time network speed monitor (KB/MB) using standard Android TrafficStats
        viewModelScope.launch {
            var lastRxBytes = android.net.TrafficStats.getUidRxBytes(android.os.Process.myUid())
            var lastTxBytes = android.net.TrafficStats.getUidTxBytes(android.os.Process.myUid())
            if (lastRxBytes == android.net.TrafficStats.UNSUPPORTED.toLong()) lastRxBytes = 0
            if (lastTxBytes == android.net.TrafficStats.UNSUPPORTED.toLong()) lastTxBytes = 0
            
            while (true) {
                delay(1000)
                var currentRxBytes = android.net.TrafficStats.getUidRxBytes(android.os.Process.myUid())
                var currentTxBytes = android.net.TrafficStats.getUidTxBytes(android.os.Process.myUid())
                if (currentRxBytes == android.net.TrafficStats.UNSUPPORTED.toLong()) currentRxBytes = 0
                if (currentTxBytes == android.net.TrafficStats.UNSUPPORTED.toLong()) currentTxBytes = 0
                
                val rxSpeed = currentRxBytes - lastRxBytes
                val txSpeed = currentTxBytes - lastTxBytes
                val totalSpeedBytes = (if (rxSpeed > 0) rxSpeed else 0) + (if (txSpeed > 0) txSpeed else 0)
                
                _networkSpeed.value = when {
                    totalSpeedBytes < 1024 -> "$totalSpeedBytes B/s"
                    totalSpeedBytes < 1024 * 1024 -> String.format("%.1f KB/s", totalSpeedBytes / 1024.0)
                    else -> String.format("%.1f MB/s", totalSpeedBytes / (1024.0 * 1024.0))
                }
                
                lastRxBytes = currentRxBytes
                lastTxBytes = currentTxBytes
            }
        }
    }

    fun requireAuth(reason: String = "इस फीचर के लिए पहले लॉगिन करें", onAuthorized: () -> Unit) {
        if (isLoggedIn.value) {
            onAuthorized()
        } else {
            _loginPromptReason.value = reason
            _showLoginPrompt.value = true
        }
    }

    fun dismissLoginPrompt() {
        _showLoginPrompt.value = false
    }

    fun openLoginScreen() {
        _showLoginPrompt.value = false
        navigateTo(ScreenDestination.Auth)
    }

    fun logout() {
        repository.logoutUser()
        navigateTo(ScreenDestination.Auth)
    }

    fun clearUploadError() {
        _uploadError.value = null
    }

    fun navigateTo(destination: ScreenDestination) {
        _currentScreen.value = destination
    }

    fun onLikePost(postId: String) {
        requireAuth("पोस्ट को लाइक / रिएक्ट करने के लिए पहले लॉगिन करें") {
            repository.toggleLikePost(postId)
            val post = posts.value.find { it.id == postId }
            if (post != null) {
                val isCurrentlyLiked = post.isLiked
                val newStatus = !isCurrentlyLiked
                if (newStatus) {
                    addNotification(
                        type = com.example.data.NotificationType.LIKE,
                        title = "Liked Post",
                        message = "You liked @${post.authorUsername}'s post.",
                        fromUser = post.authorUsername,
                        avatar = post.authorAvatarUrl
                    )
                }
            }
        }
    }

    fun onBookmarkPost(postId: String) {
        requireAuth("पोस्ट को सेव करने के लिए पहले लॉगिन करें") {
            repository.toggleBookmarkPost(postId)
            val post = posts.value.find { it.id == postId }
            if (post != null) {
                val isCurrentlySaved = post.isSaved
                val newStatus = !isCurrentlySaved
                if (newStatus) {
                    addNotification(
                        type = com.example.data.NotificationType.TAG,
                        title = "Saved Post",
                        message = "You saved @${post.authorUsername}'s post to bookmarks.",
                        fromUser = post.authorUsername,
                        avatar = post.authorAvatarUrl
                    )
                }
            }
        }
    }

    fun onAddComment(postId: String, text: String) {
        requireAuth("कमेंट करने के लिए पहले लॉगिन करें") {
            repository.addComment(postId, text)
            val post = posts.value.find { it.id == postId }
            if (post != null) {
                addNotification(
                    type = com.example.data.NotificationType.COMMENT,
                    title = "New Comment",
                    message = "You commented on @${post.authorUsername}'s post: '$text'",
                    fromUser = post.authorUsername,
                    avatar = post.authorAvatarUrl
                )
            }
        }
    }

    fun onLikeReel(reelId: String) {
        requireAuth("रील को लाइक / रिएक्ट करने के लिए पहले लॉगिन करें") {
            repository.toggleLikeReel(reelId)
            val reel = reels.value.find { it.id == reelId }
            if (reel != null) {
                val isCurrentlyLiked = reel.isLiked
                val newStatus = !isCurrentlyLiked
                if (newStatus) {
                    addNotification(
                        type = com.example.data.NotificationType.LIKE,
                        title = "Liked Reel",
                        message = "You liked @${reel.authorUsername}'s reel.",
                        fromUser = reel.authorUsername,
                        avatar = reel.authorAvatarUrl
                    )
                }
            }
        }
    }

    fun onBookmarkReel(reelId: String) {
        requireAuth("रील को सेव करने के लिए पहले लॉगिन करें") {
            repository.toggleBookmarkReel(reelId)
            val reel = reels.value.find { it.id == reelId }
            if (reel != null) {
                val isCurrentlySaved = reel.isSaved
                val newStatus = !isCurrentlySaved
                if (newStatus) {
                    addNotification(
                        type = com.example.data.NotificationType.TAG,
                        title = "Saved Reel",
                        message = "You saved @${reel.authorUsername}'s reel to bookmarks.",
                        fromUser = reel.authorUsername,
                        avatar = reel.authorAvatarUrl
                    )
                }
            }
        }
    }

    fun onSendChatMessage(threadId: String, text: String, mediaUrl: String = "") {
        requireAuth("मैसेज भेजने के लिए पहले लॉगिन करें") {
            repository.sendChatMessage(threadId, text, mediaUrl)
        }
    }

    fun startVoiceRecording(): Boolean {
        return repository.voiceMessageManager.startRecording()
    }

    fun stopVoiceRecordingAndSend(threadId: String) {
        val duration = repository.voiceMessageManager.recordingState.value.durationSeconds
        val filePath = repository.voiceMessageManager.stopRecording(save = true)
        if (!filePath.isNullOrBlank()) {
            requireAuth("वॉयस मैसेज भेजने के लिए पहले लॉगिन करें") {
                repository.sendVoiceMessage(threadId, filePath, duration.coerceAtLeast(1))
            }
        }
    }

    fun cancelVoiceRecording() {
        repository.voiceMessageManager.stopRecording(save = false)
    }

    fun playVoiceMessage(messageId: String, audioUrl: String) {
        repository.voiceMessageManager.playVoiceMessage(messageId, audioUrl)
    }

    fun stopVoicePlayback() {
        repository.voiceMessageManager.stopPlayback()
    }

    fun onReactToMessage(threadId: String, messageId: String, emoji: String) {
        requireAuth("रिएक्शन देने के लिए पहले लॉगिन करें") {
            repository.reactToMessage(threadId, messageId, emoji)
        }
    }

    fun onDeletePost(postId: String) {
        repository.deletePost(postId)
    }

    fun onEditPost(postId: String, newCaption: String, newLocation: String) {
        repository.editPost(postId, newCaption, newLocation)
    }

    fun onEndCall(
        callerName: String,
        callerAvatar: String = "",
        isVideo: Boolean,
        durationSeconds: Int,
        isOutgoing: Boolean = true
    ) {
        repository.addCallLog(
            callerName = callerName,
            callerAvatar = callerAvatar,
            isVideo = isVideo,
            durationSeconds = durationSeconds,
            isOutgoing = isOutgoing
        )
        navigateTo(ScreenDestination.DirectMessages)
    }

    fun initiateCall(name: String, isVideo: Boolean) {
        navigateTo(ScreenDestination.Call(callName = name, isVideo = isVideo))
    }

    fun clearCallHistory() {
        repository.clearCallLogs()
    }

    suspend fun getPopularLocations(): List<String> {
        return repository.locationManager.getPopularLocations()
    }

    suspend fun getCurrentLocationName(): String {
        return repository.locationManager.getCurrentLocationName()
    }

    fun onUpdateProfile(displayName: String, bio: String, website: String, isPrivate: Boolean, avatarUrl: String? = null) {
        repository.updateUserProfile(displayName, bio, website, isPrivate, avatarUrl)
    }

    fun onUpdateAvatar(uri: Uri) {
        viewModelScope.launch {
            _uploadError.value = null
            _isUploading.value = true
            _uploadProgress.value = 0.05f
            _uploadMessage.value = "Updating profile picture..."
            val currentUserVal = repository.currentUser.value
            repository.storageManager.uploadMediaFlow(
                uri = uri,
                folder = "avatars",
                contentType = "image/jpeg",
                userId = currentUserVal.userId
            ).collect { status ->
                when (status) {
                    is UploadStatus.Progress -> {
                        _isUploading.value = true
                        _uploadProgress.value = status.progressPercent
                        _uploadMessage.value = status.statusMessage
                    }
                    is UploadStatus.Error -> {
                        _isUploading.value = false
                        _uploadError.value = status.errorMessage
                        _uploadMessage.value = status.errorMessage
                    }
                    is UploadStatus.Success -> {
                        _uploadProgress.value = 1f
                        _uploadMessage.value = "Profile picture updated!"
                        repository.updateUserProfile(
                            displayName = currentUserVal.displayName,
                            bio = currentUserVal.bio,
                            website = currentUserVal.website,
                            isPrivate = currentUserVal.isPrivate,
                            avatarUrl = status.downloadUrl
                        )
                        _isUploading.value = false
                    }
                    is UploadStatus.Idle -> {}
                }
            }
        }
    }

    fun loginUser(user: UserData) {
        repository.loginUser(user)
    }

    fun completeProfileSetup(updatedUser: UserData) {
        repository.updateUserProfile(
            displayName = updatedUser.displayName,
            bio = updatedUser.bio,
            website = updatedUser.website,
            isPrivate = updatedUser.isPrivate,
            avatarUrl = updatedUser.avatarUrl
        )
        // Note: For full persistence of new fields, SocialRepository needs an update, 
        // but for now, we will handle the state in the repository/Room.
        repository.loginUser(updatedUser.copy(isTermsAccepted = true))
    }

    fun onRefreshFeed() {
        viewModelScope.launch {
            _isUploading.value = true
            _uploadMessage.value = "Syncing live feed..."
            _uploadProgress.value = 0.5f
            repository.syncOnlinePosts()
            _uploadProgress.value = 1f
            _isUploading.value = false
        }
    }

    fun createPost(uri: Uri, caption: String, location: String) {
        requireAuth("पोस्ट अपलोड करने के लिए पहले लॉगिन करें") {
            viewModelScope.launch {
                _uploadError.value = null
                _isUploading.value = true
                _uploadProgress.value = 0.05f
                _uploadMessage.value = "Uploading..."

                val authorId = currentUser.value.userId
                repository.storageManager.uploadMediaFlow(
                    uri = uri,
                    folder = "posts",
                    contentType = "image/jpeg",
                    userId = authorId
                ).collect { status ->
                    when (status) {
                        is UploadStatus.Progress -> {
                            _isUploading.value = true
                            _uploadProgress.value = status.progressPercent
                            _uploadMessage.value = status.statusMessage
                        }
                        is UploadStatus.Error -> {
                            _isUploading.value = false
                            _uploadError.value = status.errorMessage
                            _uploadMessage.value = status.errorMessage
                        }
                        is UploadStatus.Success -> {
                            _uploadProgress.value = 1f
                            _uploadMessage.value = "Published to HundredGram!"
                            repository.publishPostAfterUpload(status.downloadUrl, caption, location)
                            _isUploading.value = false
                            navigateTo(ScreenDestination.Feed)
                        }
                        is UploadStatus.Idle -> {}
                    }
                }
            }
        }
    }

    fun createStory(uri: Uri, caption: String) {
        requireAuth("स्टोरी अपलोड करने के लिए पहले लॉगिन करें") {
            viewModelScope.launch {
                _uploadError.value = null
                _isUploading.value = true
                _uploadProgress.value = 0.05f
                _uploadMessage.value = "Uploading..."

                val authorId = currentUser.value.userId
                repository.storageManager.uploadMediaFlow(
                    uri = uri,
                    folder = "stories",
                    contentType = "image/jpeg",
                    userId = authorId
                ).collect { status ->
                    when (status) {
                        is UploadStatus.Progress -> {
                            _isUploading.value = true
                            _uploadProgress.value = status.progressPercent
                            _uploadMessage.value = status.statusMessage
                        }
                        is UploadStatus.Error -> {
                            _isUploading.value = false
                            _uploadError.value = status.errorMessage
                            _uploadMessage.value = status.errorMessage
                        }
                        is UploadStatus.Success -> {
                            _uploadProgress.value = 1f
                            _uploadMessage.value = "Story added!"
                            repository.publishStoryAfterUpload(status.downloadUrl, caption)
                            _isUploading.value = false
                            navigateTo(ScreenDestination.Feed)
                        }
                        is UploadStatus.Idle -> {}
                    }
                }
            }
        }
    }

    fun createReel(uri: Uri, caption: String, audioTitle: String, audioArtist: String) {
        requireAuth("रील अपलोड करने के लिए पहले लॉगिन करें") {
            viewModelScope.launch {
                _uploadError.value = null
                _isUploading.value = true
                _uploadProgress.value = 0.05f
                _uploadMessage.value = "Uploading..."

                val authorId = currentUser.value.userId
                repository.storageManager.uploadMediaFlow(
                    uri = uri,
                    folder = "reels",
                    contentType = "video/mp4",
                    userId = authorId
                ).collect { status ->
                    when (status) {
                        is UploadStatus.Progress -> {
                            _isUploading.value = true
                            _uploadProgress.value = status.progressPercent
                            _uploadMessage.value = status.statusMessage
                        }
                        is UploadStatus.Error -> {
                            _isUploading.value = false
                            _uploadError.value = status.errorMessage
                            _uploadMessage.value = status.errorMessage
                        }
                        is UploadStatus.Success -> {
                            _uploadProgress.value = 1f
                            _uploadMessage.value = "Reel published!"
                            repository.publishReelAfterUpload(status.downloadUrl, caption, audioTitle, audioArtist)
                            _isUploading.value = false
                            navigateTo(ScreenDestination.Reels)
                        }
                        is UploadStatus.Idle -> {}
                    }
                }
            }
        }
    }

    fun setSelectedFilterImage(uri: Uri) {
        _selectedFilterImageUri.value = uri
        navigateTo(ScreenDestination.FilterStudio)
    }

    fun playAudioTrack(track: ComprehensiveAudioTrack) {
        repository.reelAudioService.playAudioTrack(track)
    }

    fun playReelAudio(reel: ReelVideo) {
        if (reel.audioTitle.isBlank() && reel.audioArtist.isBlank()) {
            // No custom audio soundtrack attached; video's own audio will play through VideoPlayer
            repository.reelAudioService.stop()
            return
        }
        val availableTracks = repository.getAvailableAudioTracks()
        if (availableTracks.isEmpty()) {
            repository.reelAudioService.stop()
            return
        }
        // Try matching by track title
        val matchedTrack = availableTracks.find {
            it.title.equals(reel.audioTitle, ignoreCase = true) ||
            reel.audioTitle.contains(it.title, ignoreCase = true) ||
            it.title.contains(reel.audioTitle, ignoreCase = true)
        }
        if (matchedTrack != null) {
            repository.reelAudioService.playAudioTrack(matchedTrack)
        } else {
            repository.reelAudioService.stop()
        }
    }

    fun stopAudio() {
        repository.reelAudioService.stop()
    }

    fun toggleMuteAudio() {
        repository.reelAudioService.toggleMute()
    }

    fun togglePlayPauseAudio() {
        repository.reelAudioService.togglePlayPause()
    }

    private val _notifications = MutableStateFlow<List<com.example.data.NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<com.example.data.NotificationEntity>> = _notifications.asStateFlow()

    fun addNotification(type: com.example.data.NotificationType, title: String, message: String, fromUser: String, avatar: String) {
        val newNotif = com.example.data.NotificationEntity(
            id = System.currentTimeMillis().toString(),
            type = type,
            title = title,
            message = message,
            fromUser = fromUser,
            fromUserAvatar = avatar,
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    private fun generateMockNotifications() {
        _notifications.value = emptyList()
    }

    private fun loadNearbyData() {
        viewModelScope.launch {
            val result = repository.nearbyService.searchNearby()
            _nearbyUsers.value = result.users
            _nearbyEvents.value = result.events
            generateMockNotifications()
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            val trimmed = query.trim()
            if (trimmed.isBlank()) {
                _searchedUsers.value = emptyList()
                return@launch
            }
            _isSearchingUsers.value = true
            try {
                val results = repository.searchUsersInFirestore(trimmed)
                _searchedUsers.value = results
            } catch (e: Exception) {
                _searchedUsers.value = emptyList()
            } finally {
                _isSearchingUsers.value = false
            }
        }
    }

    fun loadRecommendedCreators() {
        viewModelScope.launch {
            try {
                val results = repository.getRecommendedCreators()
                _recommendedCreators.value = results
            } catch (e: Exception) {
                _recommendedCreators.value = emptyList()
            }
        }
    }

    fun toggleFollowUser(targetUserId: String, onSuccess: (Boolean) -> Unit = {}) {
        requireAuth("क्रिएटर को फ़ॉलो करने के लिए पहले लॉगिन करें") {
            viewModelScope.launch {
                val isFollowingNow = repository.toggleFollowUser(targetUserId)
                onSuccess(isFollowingNow)
                
                // Add a local notification for follow if followed
                if (isFollowingNow) {
                    addNotification(
                        type = com.example.data.NotificationType.FOLLOW,
                        title = "New Follow",
                        message = "You started following creator.",
                        fromUser = "System",
                        avatar = ""
                    )
                }
            }
        }
    }

    private val _isFollowingMap = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isFollowingMap: StateFlow<Map<String, Boolean>> = _isFollowingMap.asStateFlow()

    fun checkIfFollowing(targetUserId: String) {
        viewModelScope.launch {
            val following = repository.isFollowingUser(targetUserId)
            _isFollowingMap.value = _isFollowingMap.value + (targetUserId to following)
        }
    }

    val blockedUsers: StateFlow<List<com.example.data.BlockedUser>> = repository.blockedList
    val reportedItems: StateFlow<List<com.example.data.ReportedItem>> = repository.reportedList

    fun blockUser(userId: String, username: String, displayName: String = "", avatarUrl: String = "") {
        repository.blockUser(userId, username, displayName, avatarUrl)
    }

    fun unblockUser(userId: String) {
        repository.unblockUser(userId)
    }

    fun reportContent(targetId: String, targetType: String, authorUsername: String, reason: String) {
        repository.reportContent(targetId, targetType, authorUsername, reason)
    }

    suspend fun refreshFeed() {
        try {
            repository.syncOnlineData()
            loadRecommendedCreators()
            loadNearbyData()
        } catch (_: Exception) {}
    }
}
