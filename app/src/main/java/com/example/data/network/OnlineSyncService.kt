package com.example.data.network

import com.example.data.PostEntity
import com.example.data.ReelVideo
import com.example.data.StoryEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OnlineSyncService {
    suspend fun fetchLiveOnlinePosts(): List<PostEntity> = withContext(Dispatchers.IO) {
        val fetchedList = mutableListOf<PostEntity>()
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("posts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(40)
                .get()
                .await()
            
            for (doc in snapshot.documents) {
                val post = doc.toObject(PostEntity::class.java)
                if (post != null) {
                    fetchedList.add(post)
                }
            }
        } catch (e: Exception) {
            // Offline or Firebase not initialized
        }
        fetchedList
    }

    suspend fun fetchLiveOnlineReels(): List<ReelVideo> = withContext(Dispatchers.IO) {
        val fetchedList = mutableListOf<ReelVideo>()
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("reels")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(40)
                .get()
                .await()

            for (doc in snapshot.documents) {
                val reel = doc.toObject(ReelVideo::class.java)
                if (reel != null) {
                    fetchedList.add(reel)
                }
            }
        } catch (e: Exception) {
            // Offline or Firebase not initialized
        }
        fetchedList
    }

    suspend fun fetchLiveOnlineStories(): List<StoryEntity> = withContext(Dispatchers.IO) {
        val fetchedList = mutableListOf<StoryEntity>()
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("stories")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .await()

            for (doc in snapshot.documents) {
                val story = doc.toObject(StoryEntity::class.java)
                if (story != null) {
                    fetchedList.add(story)
                }
            }
        } catch (e: Exception) {
            // Offline or Firebase not initialized
        }
        fetchedList
    }
}

