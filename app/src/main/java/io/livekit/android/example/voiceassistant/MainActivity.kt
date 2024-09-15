package io.livekit.android.example.voiceassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import app.rive.runtime.kotlin.core.Rive
import io.livekit.android.AudioOptions
import io.livekit.android.LiveKit
import io.livekit.android.LiveKitOverrides
import io.livekit.android.annotations.Beta
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.example.voiceassistant.audio.LocalAudioTrackFlow
import io.livekit.android.example.voiceassistant.audio.rememberLocalVolume
import io.livekit.android.example.voiceassistant.ui.StateIndicator
import io.livekit.android.example.voiceassistant.ui.TranscriptionLogs
import io.livekit.android.example.voiceassistant.ui.Visualizer
import io.livekit.android.example.voiceassistant.ui.theme.LiveKitVoiceAssistantExampleTheme
import io.livekit.android.util.LoggingLevel

// Replace these values with your url and generated token.
const val wsURL = ""
const val token =
    ""

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistant(modifier: Modifier = Modifier) {
    KeepScreenOn()
    // Setup listening to the local microphone if needed.
    val localAudioFlow = remember { LocalAudioTrackFlow() }
    val localVolume = rememberLocalVolume(localAudioTrackFlow = localAudioFlow)
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
        BottomSheetScaffold(sheetContent = {
            TranscriptionLogs(room = room)
        }) {

            // Main screen display
            ConstraintLayout(modifier = modifier) {

                val (visualizer, stateIndicator) = createRefs()

                Visualizer(
                    room = room,
                    localVolume = localVolume,
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
