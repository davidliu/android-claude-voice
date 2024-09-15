package io.livekit.android.example.voiceassistant.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.rive.runtime.kotlin.core.Loop
import io.livekit.android.example.voiceassistant.R
import io.livekit.android.example.voiceassistant.state.AssistantState
import io.livekit.android.example.voiceassistant.state.rememberAssistantState
import io.livekit.android.room.Room
import io.livekit.android.util.LKLog
import io.livekit.android.util.flow


@Composable
fun Visualizer(room: Room, localVolume: Float, modifier: Modifier) {
    val roomState by room::state.flow.collectAsState(initial = Room.State.CONNECTING)

    val remoteParticipants by room::remoteParticipants.flow.collectAsState()
    val remoteParticipant by remember(remoteParticipants) {
        derivedStateOf { remoteParticipants.values.firstOrNull() }
    }
    val assistantState = rememberAssistantState(participant = remoteParticipant)

    val isLocalSpeaking = localVolume > 10000

    ComposableRiveAnimationView(
        modifier = modifier,
        animation = R.raw.avatar,
        update = {
            val animationName = when (roomState) {
                Room.State.CONNECTING -> "open"
                Room.State.RECONNECTING -> "waiting"
                Room.State.DISCONNECTED -> null
                Room.State.CONNECTED -> {
                    when (assistantState) {
                        AssistantState.LISTENING -> {
                            if (isLocalSpeaking)
                                "tickle"
                            else
                                "waiting"
                        }

                        AssistantState.THINKING -> "thinking"
                        AssistantState.SPEAKING -> "writing"
                        AssistantState.UNKNOWN -> null
                    }
                }
            }

            it.stop()

            if (animationName != null) {
                val loop = when (animationName) {
                    "tickle" -> {
                        Loop.PINGPONG
                    }

                    "waiting",
                    "thinking",
                    "writing" -> {
                        Loop.LOOP
                    }

                    else -> {
                        Loop.ONESHOT
                    }
                }

                LKLog.e { "playing: $animationName, $loop" }
                it.play(animationName, loop)
            }
        }
    )
}