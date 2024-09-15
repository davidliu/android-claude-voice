package io.livekit.android.example.voiceassistant.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.state.transcriptions.rememberParticipantTranscriptions
import io.livekit.android.compose.state.transcriptions.rememberTranscriptions
import io.livekit.android.room.Room

@OptIn(Beta::class)
@Composable
fun TranscriptionLogs(room: Room) {

    // Get and display the transcriptions.
    val segments = rememberTranscriptions()
    val localSegments = rememberParticipantTranscriptions(room.localParticipant)
    val lazyListState = rememberLazyListState()

    // Title
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Logs",
        fontSize = TextUnit(20f, TextUnitType.Sp),
        fontWeight = FontWeight.Bold,
        fontFamily = claudeFont,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    // Logs
    LazyColumn(
        userScrollEnabled = true,
        state = lazyListState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = segments,
            key = { segment -> segment.id },
        ) { segment ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (localSegments.contains(segment)) {
                    UserTranscription(segment = segment, modifier = Modifier.align(Alignment.CenterEnd))
                } else {
                    Text(
                        text = segment.text,
                        fontFamily = claudeFont,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }
    }

    // Scroll to bottom as new transcriptions come in.
    LaunchedEffect(segments) {
        lazyListState.scrollToItem((segments.size - 1).coerceAtLeast(0))
    }
}