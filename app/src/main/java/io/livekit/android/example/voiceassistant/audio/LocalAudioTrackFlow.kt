package io.livekit.android.example.voiceassistant.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import livekit.org.webrtc.audio.JavaAudioDeviceModule
import kotlin.experimental.and
import kotlin.math.round
import kotlin.math.sqrt

class LocalAudioTrackFlow : JavaAudioDeviceModule.SamplesReadyCallback {
    val flow = MutableSharedFlow<JavaAudioDeviceModule.AudioSamples>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    override fun onWebRtcAudioRecordSamplesReady(samples: JavaAudioDeviceModule.AudioSamples) {
        flow.tryEmit(samples)
    }
}

@Composable
fun rememberLocalVolume(localAudioTrackFlow: LocalAudioTrackFlow): Float {
    var volume by remember { mutableFloatStateOf(0.0f) }

    LaunchedEffect(localAudioTrackFlow) {
        launch(Dispatchers.IO) {
            localAudioTrackFlow.flow.collect { samples ->
                volume = calculateVolume(samples.data).toFloat()
            }
        }
    }

    return volume
}

/**
 * Determines volume of input using the root mean squared of the amplitude.
 */
fun calculateVolume(input: ByteArray): Double {
    var average = 0L
    for (i in input.indices step 2) {
        val value = input.getShort(i).toLong()
        average += value * value
    }

    average /= (input.size / 2)

    return round(sqrt(average.toDouble()))
}


fun ByteArray.getShort(byteIndex: Int): Short {
    val b1 = (get(byteIndex) and 0xFF.toByte()).toInt()
    val b2 = (get(byteIndex + 1) and 0xFF.toByte()).toInt()

    return ((b1 shl 8) or (b2)).toShort()
}