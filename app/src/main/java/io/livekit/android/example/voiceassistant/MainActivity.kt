@file:OptIn(Beta::class)

package io.livekit.android.example.voiceassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import app.rive.runtime.kotlin.core.Loop
import app.rive.runtime.kotlin.core.Rive
import io.livekit.android.AudioOptions
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.example.voiceassistant.audio.LocalAudioTrackFlow
import io.livekit.android.example.voiceassistant.state.AssistantState
import io.livekit.android.example.voiceassistant.state.rememberAssistantState
import io.livekit.android.example.voiceassistant.ui.ComposableRiveAnimationView
import io.livekit.android.example.voiceassistant.ui.theme.LiveKitVoiceAssistantExampleTheme
import io.livekit.android.room.Room
import io.livekit.android.util.LoggingLevel
import io.livekit.android.util.flow

// Replace these values with your url and generated token.
const val wsURL = "ws://192.168.11.2:7880"
const val token =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MjYyMjI2NjIsImlzcyI6IkFQSVRMV3JLOHRid3I0NyIsIm5iZiI6MTcyMzYzMDY2Miwic3ViIjoicGhvbmUiLCJ2aWRlbyI6eyJyb29tIjoibXlyb29tIiwicm9vbUpvaW4iOnRydWV9fQ.61oC0qB3cOxIv-MUp89e05Pelw-G_thqg5G7UMEmAXw"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Rive.init(this)
        LiveKit.loggingLevel = LoggingLevel.DEBUG
        requireNeededPermissions {
            setContent {
                LiveKitVoiceAssistantExampleTheme {
                    VoiceAssistant(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun VoiceAssistant(modifier: Modifier = Modifier) {
    KeepScreenOn()
    ConstraintLayout(modifier = modifier) {
        // Setup listening to the local microphone if needed.
        val localAudioFlow = remember { LocalAudioTrackFlow() }
        val overrides = remember {
            LiveKitOverrides(
                audioOptions = AudioOptions(
                    javaAudioDeviceModuleCustomizer = { builder ->
                        builder.setSamplesReadyCallback(localAudioFlow)
                    }
                )
            )
        }

        RoomScope(
            url = wsURL,
            token = token,
            audio = true,
            connect = true,
            liveKitOverrides = overrides
        ) { room ->
            val (visualizer, stateIndicator) = createRefs()

            Visualizer(
                room = room,
                modifier = Modifier
                    .constrainAs(visualizer) {
                        height = Dimension.value(100.dp)
                        width = Dimension.value(100.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
            )

            StateIndicator(
                room = room,
                modifier = Modifier.constrainAs(stateIndicator) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(visualizer.bottom, 40.dp)
                }
            )
        }
    }
}

val claudeFont = FontFamily(Font(R.font.tiempos_headline_light))

@Composable
fun StateIndicator(room: Room, modifier: Modifier = Modifier) {
    val roomState by room::state.flow.collectAsState(initial = Room.State.CONNECTING)

    val remoteParticipants by room::remoteParticipants.flow.collectAsState()
    val remoteParticipant by remember(remoteParticipants) {
        derivedStateOf { remoteParticipants.values.firstOrNull() }
    }
    val assistantState = rememberAssistantState(participant = remoteParticipant)

    val stateString = when (roomState) {
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

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

@Composable
fun Visualizer(room: Room, modifier: Modifier) {
    val roomState by room::state.flow.collectAsState(initial = Room.State.CONNECTING)

    val remoteParticipants by room::remoteParticipants.flow.collectAsState()
    val remoteParticipant by remember(remoteParticipants) {
        derivedStateOf { remoteParticipants.values.firstOrNull() }
    }
    val assistantState = rememberAssistantState(participant = remoteParticipant)


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
                        AssistantState.LISTENING -> "waiting"
                        AssistantState.THINKING -> "thinking"
                        AssistantState.SPEAKING -> "writing"
                        AssistantState.UNKNOWN -> null
                    }
                }
            }

            it.stop()

            if (animationName != null) {
                val loop = when (animationName) {
                    "waiting",
                    "thinking",
                    "writing" -> {
                        Loop.LOOP
                    }

                    else -> {
                        Loop.ONESHOT
                    }
                }

                it.play(animationName, loop)
            }
        }
    )
}