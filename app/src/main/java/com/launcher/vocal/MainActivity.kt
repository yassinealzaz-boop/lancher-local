package com.launcher.vocal

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.FRENCH
            speak("Bienvenue sur votre écran d'accueil vocal. Utilisez la télécommande pour naviguer.")
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> speak("Haut")
            KeyEvent.KEYCODE_DPAD_DOWN -> speak("Bas")
            KeyEvent.KEYCODE_DPAD_LEFT -> speak("Gauche")
            KeyEvent.KEYCODE_DPAD_RIGHT -> speak("Droite")
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> speak("Sélectionné")
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroy()
    }
}
