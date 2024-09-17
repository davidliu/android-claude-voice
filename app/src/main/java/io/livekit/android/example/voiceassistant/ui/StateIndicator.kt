package io.livekit.android.example.voiceassistant.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import io.livekit.android.example.voiceassistant.R
import io.livekit.android.example.voiceassistant.state.AssistantState
import io.livekit.android.example.voiceassistant.state.rememberAssistantState
import io.livekit.android.example.voiceassistant.token.TokenResponse
import io.livekit.android.room.Room
import io.livekit.android.util.flow


val claudeFont = FontFamily(Font(R.font.tiempos_headline_light))

@Composable
fun StateIndicator(tokenResponse: TokenResponse?, room: Room, modifier: Modifier = Modifier) {
    val roomState by room::state.flow.collectAsState(initial = Room.State.CONNECTING)

    val remoteParticipants by room::remoteParticipants.flow.collectAsState()
    val remoteParticipant by remember(remoteParticipants) {
        derivedStateOf { remoteParticipants.values.firstOrNull() }
    }
    val assistantState = rememberAssistantState(participant = remoteParticipant)

    val stateString =
        if (tokenResponse == null) {
            // Still awaiting token server response.
            "Connecting"
        } else {
            when (roomState) {
                Room.State.CONNECTING -> "Connecting"
                Room.State.RECONNECTING -> "Reconnecting"
                Room.State.DISCONNECTED -> "Disconnected"
                Room.State.CONNECTED -> {
                    when (assistantState) {
                        AssistantState.LISTENING -> "Start speaking"
                        AssistantState.THINKING,
                        AssistantState.SPEAKING,
                        AssistantState.UNKNOWN -> ""
                    }
            }
        }
    }

    val visibility = stateString.isNotBlank()
    AnimatedVisibility(
        visible = visibility,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Text(text = stateString, fontFamily = claudeFont)
    }
}