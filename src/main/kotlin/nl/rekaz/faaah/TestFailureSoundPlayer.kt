package nl.rekaz.faaah

import com.intellij.openapi.diagnostic.Logger
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl
import kotlin.math.log10

object TestFailureSoundPlayer {
    private val LOG = Logger.getInstance(TestFailureSoundPlayer::class.java)

    fun playFailureSound() {
        val settings = SoundSettings.getInstance().state.failureSettings
        if (!settings.isEnabled) return
        play(settings.soundPath, settings.volume)
    }

    fun playSuccessSound() {
        val settings = SoundSettings.getInstance().state.successSettings
        if (!settings.isEnabled) return
        play(settings.soundPath, settings.volume)
    }

    fun play(path: String?, volume: Float) {
        try {
            val resourceStream = getInputStream(path) ?: return
            val audioInputStream = AudioSystem.getAudioInputStream(BufferedInputStream(resourceStream))
            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                val dB = (log10(volume.toDouble().coerceIn(0.0001, 1.0)) * 20.0).toFloat()
                gainControl.value = dB
            }

            clip.start()
        } catch (e: Exception) {
            LOG.error(SoundBundle.message("error.failed.to.play.sound"), e)
        }
    }

    private fun getInputStream(path: String?): InputStream? {
        if (path.isNullOrEmpty()) {
            return javaClass.classLoader.getResourceAsStream("fail/faaah.wav")
        }
        if (path.startsWith("fail/") || path.startsWith("pass/")) {
            return javaClass.classLoader.getResourceAsStream(path)
        }
        val file = File(path)
        if (!file.exists()) {
            LOG.warn("Custom sound file not found: $path")
            return null
        }
        return FileInputStream(file)
    }

    fun isValidSoundFile(path: String?): Boolean {
        if (path.isNullOrEmpty()) return true
        return try {
            val resourceStream = if (path.startsWith("fail/") || path.startsWith("pass/")) {
                javaClass.classLoader.getResourceAsStream(path)
            } else {
                val file = File(path)
                if (!file.exists()) return false
                FileInputStream(file)
            } ?: return false
            val audioInputStream = AudioSystem.getAudioInputStream(BufferedInputStream(resourceStream))
            audioInputStream.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getDefaultSounds(folder: String): Map<String, String> {
        // Since we can't easily list resources in a JAR/ClassLoader, we'll hardcode them based on the file structure provided
        return when (folder) {
            "fail" -> mapOf(
                "FAAAH" to "fail/faaah.wav",
                "Bomboclaat" to "fail/bomboclaat.wav",
                "Emotional Damage" to "fail/emotional-damage-meme.wav",
                "Nemesis" to "fail/tf_nemesis.wav",
                "Vine Boom" to "fail/vine-boom.wav"
            )
            "pass" -> mapOf(
                "Yeah Boy" to "pass/yeah-boymp4.wav"
            )
            else -> emptyMap()
        }
    }
}
