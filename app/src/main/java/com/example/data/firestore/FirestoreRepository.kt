package com.example.data.firestore

import android.content.Context
import com.example.data.ChatMessageEntity
import com.example.data.PostEntity
import com.example.data.ReelVideo
import com.example.data.StoryEntity
import com.example.data.UserData
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreRepository(private val context: Context) {
    private val firestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _realtimeMessages = MutableStateFlow<Map<String, List<ChatMessageEntity>>>(emptyMap())
    val realtimeMessages: Flow<Map<String, List<ChatMessageEntity>>> = _realtimeMessages.asStateFlow()

    /**
     * Saves user login profile to Firebase Firestore at path: /users/{userId}
     */
    suspend fun syncUserToFirestore(user: UserData) = withContext(Dispatchers.IO) {
        try {
            val userMap = hashMapOf<String, Any>(
                "userId" to user.userId,
                "username" to user.username,
                "displayName" to user.displayName,
                "bio" to user.bio,
                "avatarUrl" to user.avatarUrl,
                "followersCount" to user.followersCount,
                "followingCount" to user.followingCount,
                "postsCount" to user.postsCount,
                "isVerified" to user.isVerified,
                "website" to user.website,
                "location" to user.location,
                "lastLoginAt" to System.currentTimeMillis()
            )
            firestore?.collection("users")?.document(user.userId)?.set(userMap, SetOptions.merge())?.await()
        } catch (e: Exception) {
            // Firestore offline or permission issue
        }
    }

    /**
     * Retrieves saved user from Firebase Firestore at path: /users/{userId}
     */
    suspend fun getUserFromFirestore(userId: String): UserData? = withContext(Dispatchers.IO) {
        try {
            val doc = firestore?.collection("users")?.document(userId)?.get()?.await()
            if (doc != null && doc.exists()) {
                val data = doc.data ?: return@withContext null
                return@withContext UserData(
                    userId = doc.id,
                    username = (data["username"] as? String) ?: "user",
                    displayName = (data["displayName"] as? String) ?: "Creator",
                    bio = (data["bio"] as? String) ?: "",
                    avatarUrl = (data["avatarUrl"] as? String) ?: "",
                    followersCount = (data["followersCount"] as? Number)?.toInt() ?: 0,
                    followingCount = (data["followingCount"] as? Number)?.toInt() ?: 0,
                    postsCount = (data["postsCount"] as? Number)?.toInt() ?: 0,
                    isVerified = (data["isVerified"] as? Boolean) ?: false,
                    website = (data["website"] as? String) ?: "",
                    location = (data["location"] as? String) ?: ""
                )
            }
        } catch (e: Exception) {
            // Offline fallback
        }
        null
    }

    suspend fun findUserBySearchQuery(query: String): UserData? = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext null
            val snapshot = db.collection("users").get().await()
            val cleanQuery = query.trim().lowercase()
            if (cleanQuery.isBlank()) return@withContext null

            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                val uId = doc.id.lowercase()
                val uname = (data["username"] as? String)?.lowercase() ?: ""
                val dname = (data["displayName"] as? String)?.lowercase() ?: ""
                val fName = (data["fullName"] as? String)?.lowercase() ?: ""

                if (uId == cleanQuery || 
                    uname == cleanQuery || 
                    uname.contains(cleanQuery) || 
                    dname == cleanQuery ||
                    dname.contains(cleanQuery) ||
                    fName == cleanQuery ||
                    fName.contains(cleanQuery)) {
                    
                    return@withContext UserData(
                        userId = doc.id,
                        username = (data["username"] as? String) ?: "user",
                        displayName = (data["displayName"] as? String) ?: "Creator",
                        bio = (data["bio"] as? String) ?: "",
                        avatarUrl = (data["avatarUrl"] as? String) ?: "",
                        followersCount = (data["followersCount"] as? Number)?.toInt() ?: 0,
                        followingCount = (data["followingCount"] as? Number)?.toInt() ?: 0,
                        postsCount = (data["postsCount"] as? Number)?.toInt() ?: 0,
                        isVerified = (data["isVerified"] as? Boolean) ?: false,
                        website = (data["website"] as? String) ?: "",
                        location = (data["location"] as? String) ?: ""
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    /**
     * Publishes Post to Firebase Firestore at:
     * 1. /posts/{post.id}
     * 2. /users/{authorId}/posts/{post.id}
     * And increments postsCount on /users/{authorId}
     */
    suspend fun publishPost(post: PostEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext false
            // Primary global posts collection
            db.collection("posts").document(post.id).set(post).await()

            // User subcollection for posts
            if (post.authorId.isNotBlank()) {
                db.collection("users")
                    .document(post.authorId)
                    .collection("posts")
                    .document(post.id)
                    .set(post)
                    .await()

                // Increment user postsCount
                db.collection("users")
                    .document(post.authorId)
                    .update("postsCount", FieldValue.increment(1))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Publishes Story to Firebase Firestore at:
     * 1. /stories/{story.id}
     * 2. /users/{userId}/stories/{story.id}
     */
    suspend fun publishStory(story: StoryEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext false
            db.collection("stories").document(story.id).set(story).await()
            if (story.userId.isNotBlank()) {
                db.collection("users")
                    .document(story.userId)
                    .collection("stories")
                    .document(story.id)
                    .set(story)
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Publishes Reel to Firebase Firestore at:
     * 1. /reels/{reel.id}
     * 2. /users/{authorId}/reels/{reel.id}
     */
    suspend fun publishReel(reel: ReelVideo): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext false
            // Primary global reels collection
            db.collection("reels").document(reel.id).set(reel).await()

            // User subcollection for reels
            if (reel.authorId.isNotBlank()) {
                db.collection("users")
                    .document(reel.authorId)
                    .collection("reels")
                    .document(reel.id)
                    .set(reel)
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendChatMessage(message: ChatMessageEntity) = withContext(Dispatchers.IO) {
        val currentList = _realtimeMessages.value[message.threadId]?.toMutableList() ?: mutableListOf()
        currentList.add(message)
        _realtimeMessages.value = _realtimeMessages.value + (message.threadId to currentList)

        try {
            firestore?.collection("chats")
                ?.document(message.threadId)
                ?.collection("messages")
                ?.document(message.messageId)
                ?.set(message)?.await()
        } catch (e: Exception) {
            // Offline or network error
        }
    }

    suspend fun getRecommendedCreators(): List<UserData> = withContext(Dispatchers.IO) {
        val list = mutableListOf<UserData>()
        try {
            val db = firestore ?: return@withContext emptyList()
            val snapshot = db.collection("users").limit(10).get().await()
            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                list.add(UserData(
                    userId = doc.id,
                    username = (data["username"] as? String) ?: "user",
                    displayName = (data["displayName"] as? String) ?: "Creator",
                    bio = (data["bio"] as? String) ?: "",
                    avatarUrl = (data["avatarUrl"] as? String) ?: "",
                    followersCount = (data["followersCount"] as? Number)?.toInt() ?: 0,
                    followingCount = (data["followingCount"] as? Number)?.toInt() ?: 0,
                    postsCount = (data["postsCount"] as? Number)?.toInt() ?: 0,
                    isVerified = (data["isVerified"] as? Boolean) ?: false,
                    website = (data["website"] as? String) ?: "",
                    location = (data["location"] as? String) ?: ""
                ))
            }
        } catch (e: Exception) {
            // Fallback
        }
        list
    }

    suspend fun searchUsersByQuery(query: String): List<UserData> = withContext(Dispatchers.IO) {
        val list = mutableListOf<UserData>()
        try {
            val db = firestore ?: return@withContext emptyList()
            val snapshot = db.collection("users").get().await()
            val cleanQuery = query.trim().lowercase()
            if (cleanQuery.isBlank()) return@withContext emptyList()

            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                val uId = doc.id.lowercase()
                val uname = (data["username"] as? String)?.lowercase() ?: ""
                val dname = (data["displayName"] as? String)?.lowercase() ?: ""
                val fName = (data["fullName"] as? String)?.lowercase() ?: ""

                if (uId.contains(cleanQuery) || 
                    uname.contains(cleanQuery) || 
                    dname.contains(cleanQuery) ||
                    fName.contains(cleanQuery)) {
                    
                    list.add(UserData(
                        userId = doc.id,
                        username = (data["username"] as? String) ?: "user",
                        displayName = (data["displayName"] as? String) ?: "Creator",
                        bio = (data["bio"] as? String) ?: "",
                        avatarUrl = (data["avatarUrl"] as? String) ?: "",
                        followersCount = (data["followersCount"] as? Number)?.toInt() ?: 0,
                        followingCount = (data["followingCount"] as? Number)?.toInt() ?: 0,
                        postsCount = (data["postsCount"] as? Number)?.toInt() ?: 0,
                        isVerified = (data["isVerified"] as? Boolean) ?: false,
                        website = (data["website"] as? String) ?: "",
                        location = (data["location"] as? String) ?: ""
                    ))
                }
            }
        } catch (e: Exception) {
            // Offline fallback
        }
        list
    }

    suspend fun toggleFollowUserInFirestore(targetUserId: String, currentUserId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext false
            val userRef = db.collection("users").document(targetUserId)
            val doc = userRef.get().await()
            if (doc.exists()) {
                val data = doc.data ?: return@withContext false
                val followers = (data["followersCount"] as? Number)?.toInt() ?: 0
                val followingList = doc.get("followedBy") as? List<String> ?: emptyList()
                
                val isFollowingNow = if (followingList.contains(currentUserId)) {
                    // Unfollow
                    userRef.update(
                        "followersCount", (followers - 1).coerceAtLeast(0),
                        "followedBy", followingList - currentUserId
                    ).await()
                    false
                } else {
                    // Follow
                    userRef.update(
                        "followersCount", followers + 1,
                        "followedBy", followingList + currentUserId
                    ).await()
                    true
                }
                
                // Also update current user's followingCount
                val currentUserRef = db.collection("users").document(currentUserId)
                val currDoc = currentUserRef.get().await()
                if (currDoc.exists()) {
                    val currFollowing = (currDoc.data?.get("followingCount") as? Number)?.toInt() ?: 0
                    currentUserRef.update("followingCount", if (isFollowingNow) currFollowing + 1 else (currFollowing - 1).coerceAtLeast(0))
                }
                
                return@withContext isFollowingNow
            }
        } catch (e: Exception) {
            // Ignore
        }
        false
    }

    suspend fun isUserFollowing(targetUserId: String, currentUserId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext false
            val doc = db.collection("users").document(targetUserId).get().await()
            if (doc.exists()) {
                val followingList = doc.get("followedBy") as? List<String> ?: emptyList()
                return@withContext followingList.contains(currentUserId)
            }
        } catch (e: Exception) {
            // Ignore
        }
        false
    }

    suspend fun updatePostLikeStatusInFirestore(postId: String, isLiked: Boolean, likesCount: Int) = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("posts").document(postId).update(
                "isLiked", isLiked,
                "likesCount", likesCount
            ).await()
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun updateReelLikeStatusInFirestore(reelId: String, isLiked: Boolean, likesCount: Int) = withContext(Dispatchers.IO) {
        try {
            val db = firestore ?: return@withContext
            db.collection("reels").document(reelId).update(
                "isLiked", isLiked,
                "likesCount", likesCount
            ).await()
        } catch (e: Exception) {
            // Ignore
        }
    }
}

