package nl.rekaz.faaah

import com.intellij.openapi.components.*

@Service(Service.Level.APP)
@State(
    name = "SoundSettings",
    storages = [Storage("test_failure_sound.xml")]
)
class SoundSettings : PersistentStateComponent<SoundSettings.State> {
    class State {
        var failureSettings: SoundConfig = SoundConfig(true, 1.0f, "fail/faaah.wav")
        var successSettings: SoundConfig = SoundConfig(true, 1.0f, "pass/yeah-boy.wav")
    }

    data class SoundConfig(
        var isEnabled: Boolean = true,
        var volume: Float = 1.0f,
        var soundPath: String? = null
    ) {
        // Required for persistent state serialization
        constructor() : this(true, 1.0f, null)
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): SoundSettings = service()
    }
}
