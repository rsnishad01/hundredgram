package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserData(
    @PrimaryKey val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val isVerified: Boolean = false,
    val isFollowing: Boolean = false,
    val website: String = "",
    val location: String = "",
    val isLoggedIn: Boolean = false,
    val fullName: String = "",
    val gender: String = "",
    val dob: String = "",
    val isTermsAccepted: Boolean = false,
    val isPrivate: Boolean = false
) : Serializable

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = "",
    val isSaved: Boolean = false,
    val tags: List<String> = emptyList()
) : Serializable

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatarUrl: String = "",
    val mediaUrl: String = "",
    val isVideo: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false,
    val caption: String = ""
) : Serializable

@Entity(tableName = "reels")
data class ReelVideo(
    @PrimaryKey val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val videoUrl: String = "",
    val thumbnailUri: String = "",
    val caption: String = "",
    val audioTitle: String = "Original Audio",
    val audioArtist: String = "HundredGram Creator",
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorAvatarUrl: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false
) : Serializable

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey val threadId: String = "",
    val recipientId: String = "",
    val recipientUsername: String = "",
    val recipientAvatarUrl: String = "",
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
) : Serializable

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val messageId: String = "",
    val threadId: String = "",
    val senderId: String = "",
    val senderUsername: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val isMedia: Boolean = false,
    val isVoiceMessage: Boolean = false,
    val voiceDurationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = false,
    val isRead: Boolean = true,
    val reaction: String = ""
) : Serializable

data class LiveViewer(
    val id: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val joinedAt: Long = System.currentTimeMillis()
) : Serializable

enum class NotificationType {
    FOLLOW, LIKE, COMMENT, MENTION, TAG, SYSTEM_ALERT
}

data class NotificationEntity(
    val id: String = "",
    val type: NotificationType = NotificationType.SYSTEM_ALERT,
    val title: String = "",
    val message: String = "",
    val fromUser: String = "",
    val fromUserAvatar: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedId: String = "" // can be post id, user id etc.
) : Serializable

data class ReportedItem(
    val id: String = "",
    val targetId: String = "",
    val targetType: String = "Post", // "Post", "Reel", "Story", "User"
    val authorUsername: String = "",
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Under Review (समीक्षाधीन)"
) : Serializable

data class BlockedUser(
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val blockedAt: Long = System.currentTimeMillis()
) : Serializable

