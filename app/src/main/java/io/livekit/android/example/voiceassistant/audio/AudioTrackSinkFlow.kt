package io.livekit.android.example.voiceassistant.audio

import io.livekit.android.room.track.RemoteAudioTrack
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import livekit.org.webrtc.AudioTrackSink
import java.nio.ByteBuffer
import kotlin.math.round
import kotlin.math.sqrt

/**
 * Gathers the audio data from a [RemoteAudioTrack] and emits through a flow.
 */
class AudioTrackSinkFlow : AudioTrackSink {
    val audioFormat = MutableStateFlow(AudioFormat(16, 48000, 1))
    val audioFlow = MutableSharedFlow<Pair<ByteBuffer, Int>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun onData(
        audioData: ByteBuffer,
        bitsPerSample: Int,
        sampleRate: Int,
        numberOfChannels: Int,
        numberOfFrames: Int,
        absoluteCaptureTimestampMs: Long
    ) {
        val curAudioFormat = audioFormat.value
        if (curAudioFormat.bitsPerSample != bitsPerSample ||
            curAudioFormat.sampleRate != sampleRate ||
            curAudioFormat.numberOfChannels != numberOfChannels
        ) {
            audioFormat.tryEmit(AudioFormat(bitsPerSample, sampleRate, numberOfChannels))
        }
        audioFlow.tryEmit(audioData to numberOfFrames)
    }
}

data class AudioFormat(val bitsPerSample: Int, val sampleRate: Int, val numberOfChannels: Int)

/**
 * Determines volume of input using the root mean squared of the amplitude.
 */
fun calculateVolume(input: ByteBuffer): Double {
    var average = 0L

    try {
        input.mark()

        val bytesPerSample = 2
        val size = input.remaining() / bytesPerSample
        while (input.remaining() >= bytesPerSample) {
            val value = input.getShort()
            average += value * value
        }

        average /= size

        return round(sqrt(average.toDouble()))
    } finally {
        input.reset()
    }
}

