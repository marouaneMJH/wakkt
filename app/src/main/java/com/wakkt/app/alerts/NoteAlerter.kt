package com.wakkt.app.alerts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.data.NoteSound
import com.wakkt.app.data.NoteVibration
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays a label's attention alerts: vibration, a synthesized tone, then the text read aloud.
 * Audio uses notification usage (follows notification volume and silent mode), or alarm usage
 * when the label is in loud mode.
 */
@Singleton
class NoteAlerter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null
    private var track: AudioTrack? = null

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingSpeech: String? = null

    private fun attributes(loud: Boolean): AudioAttributes = AudioAttributes.Builder()
        .setUsage(if (loud) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    /** Plays [label]'s alerts now and, if it repeats, every repeatSeconds until [stop]. */
    fun start(label: LabelEntity) {
        stop()
        if (!label.hasAlerts) return
        job = scope.launch {
            while (isActive) {
                playOnce(label)
                if (label.repeatSeconds <= 0) break
                delay(label.repeatSeconds * 1000L)
            }
        }
    }

    /** One-shot preview, used by the editor's Test button. */
    fun test(label: LabelEntity) = start(label.copy(repeatSeconds = 0))

    fun stop() {
        job?.cancel()
        job = null
        releaseTrack()
        pendingSpeech = null
        tts?.stop()
    }

    private suspend fun playOnce(label: LabelEntity) {
        vibrate(label.vibration)
        if (label.sound != NoteSound.NONE) {
            playTone(label.sound, label.loud)
            delay(ToneSynth.durationMs(label.sound) + 120)
        }
        if (label.voice && label.text.isNotBlank()) speak(label.text, label.loud)
    }

    private fun playTone(sound: NoteSound, loud: Boolean) {
        releaseTrack()
        val pcm = toneCache.getOrPut(sound) { ToneSynth.pcm(sound) }
        if (pcm.isEmpty()) return
        runCatching {
            val t = AudioTrack.Builder()
                .setAudioAttributes(attributes(loud))
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(ToneSynth.SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(pcm.size * 2)
                .build()
            t.write(pcm, 0, pcm.size)
            t.play()
            track = t
        }
    }

    private fun releaseTrack() {
        track?.let { runCatching { it.stop() }; it.release() }
        track = null
    }

    private fun speak(text: String, loud: Boolean) {
        speechLoud = loud
        if (tts == null) {
            // Init is async; the text is spoken once the engine is ready.
            tts = TextToSpeech(context) { status ->
                ttsReady = status == TextToSpeech.SUCCESS
                if (ttsReady) pendingSpeech?.let(::say)
                pendingSpeech = null
            }
        }
        if (ttsReady) say(text) else pendingSpeech = text
    }

    private var speechLoud = false

    private fun say(text: String) {
        tts?.setAudioAttributes(attributes(speechLoud))
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wakkt-note")
    }

    private fun vibrate(pattern: NoteVibration) {
        val timings = when (pattern) {
            NoteVibration.NONE -> return
            NoteVibration.TAP -> longArrayOf(0, 40)
            NoteVibration.DOUBLE -> longArrayOf(0, 70, 110, 70)
            NoteVibration.LONG -> longArrayOf(0, 450, 180, 450, 180, 450)
        }
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator?.hasVibrator() != true) return
        runCatching { vibrator.vibrate(VibrationEffect.createWaveform(timings, -1)) }
    }

    private companion object {
        val toneCache = mutableMapOf<NoteSound, ShortArray>()
    }
}
