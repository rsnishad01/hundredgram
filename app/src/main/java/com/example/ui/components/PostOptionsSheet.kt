package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PostEntity
import com.example.ui.theme.HundredGramCardBackground
import com.example.ui.theme.HundredGramLikeRed
import com.example.ui.theme.HundredGramPink
import com.example.ui.theme.HundredGramTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostOptionsSheet(
    post: PostEntity,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onSaveToggle: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = HundredGramCardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            if (isOwner) {
                OptionItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Caption & Location",
                    tint = HundredGramPink,
                    onClick = {
                        onDismiss()
                        onEdit()
                    }
                )
            }

            OptionItem(
                icon = Icons.Default.Bookmark,
                title = if (post.isSaved) "Remove from Saved" else "Save to Collection",
                onClick = {
                    onSaveToggle()
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.Default.Share,
                title = "Share to External Apps",
                onClick = {
                    onShare()
                    onDismiss()
                }
            )

            if (isOwner) {
                OptionItem(
                    icon = Icons.Default.Delete,
                    title = "Delete Post",
                    tint = HundredGramLikeRed,
                    onClick = {
                        onDelete()
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OptionItem(
    icon: ImageVector,
    title: String,
    tint: Color = HundredGramTextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.padding(end = 16.dp))
        Text(title, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

