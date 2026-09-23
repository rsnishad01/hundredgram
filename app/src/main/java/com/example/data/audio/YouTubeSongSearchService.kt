package com.example.data.audio

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Serializable

data class YouTubeSongItem(
    val songId: String,
    val title: String,
    val artist: String,
    val albumCover: String,
    val durationFormatted: String,
    val streamUrl: String = ""
) : Serializable

class YouTubeSongSearchService(private val context: Context) {
    suspend fun searchSongs(query: String): List<YouTubeSongItem> = withContext(Dispatchers.IO) {
        val authenticLibrary = listOf(
            YouTubeSongItem("trk_1", "Desi Dhol Beats", "Punjab Express", "", "0:32"),
            YouTubeSongItem("trk_2", "Groovy Funk Vibes", "Metro Groove", "", "0:45"),
            YouTubeSongItem("trk_3", "Home Relaxed Lo-fi", "Chill Sessions", "", "1:00"),
            YouTubeSongItem("trk_4", "Festive Dhak Dhamaal", "Kolkata Rhythms", "", "0:50"),
            YouTubeSongItem("trk_5", "Sitar Melody & Flute", "Indian Classical Trio", "", "0:40"),
            YouTubeSongItem("trk_6", "Energetic Dhol Trance", "Club Dholaks", "", "0:35")
        )
        if (query.isBlank()) return@withContext authenticLibrary
        authenticLibrary.filter { it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true) }
    }
}

