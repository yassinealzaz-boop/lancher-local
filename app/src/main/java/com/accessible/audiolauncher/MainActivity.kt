package com.accessible.audiolauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private var isTtsReady = false
    private lateinit var recyclerView: RecyclerView

    private val VPN_PACKAGE = "" 
    private val AUTO_LAUNCH_APP = ""
    private val AUTO_LAUNCH_DELAY_MS = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tts = TextToSpeech(this, this)

        if (VPN_PACKAGE.isNotEmpty()) {
            startVpnAutomatically()
        }

        recyclerView = findViewById(R.id.appsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val installedApps = getInstalledApps()
        recyclerView.adapter = AppsAdapter(installedApps)

        if (AUTO_LAUNCH_APP.isNotEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                launchApp(AUTO_LAUNCH_APP, "Application par défaut")
            }, AUTO_LAUNCH_DELAY_MS)
        }
    }

    private fun startVpnAutomatically() {
        val vpnIntent = packageManager.getLaunchIntentForPackage(VPN_PACKAGE)
        if (vpnIntent != null) {
            startActivity(vpnIntent)
        }
    }

    private fun getInstalledApps(): List<AppItem> {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        var pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)

        if (pkgAppsList.isEmpty()) {
            mainIntent.removeCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
            pkgAppsList = packageManager.queryIntentActivities(mainIntent, 0)
        }

        return pkgAppsList.map {
            AppItem(
                label = it.loadLabel(packageManager).toString(),
                packageName = it.activityInfo.packageName
            )
        }.sortedBy { it.label }
    }

    fun speak(text: String) {
        if (isTtsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AudioLauncherTTS")
        }
    }

    fun launchApp(packageName: String, label: String) {
        speak("Ouverture de $label")
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            speak("Impossible d'ouvrir l'application")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.FRANCE)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                speak("Menu principal. Utilisez les flèches haut et bas pour naviguer.")
            }
        }
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    data class AppItem(val label: String, val packageName: String)

    inner class AppsAdapter(private val appsList: List<AppItem>) :
        RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

        inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvLabel: TextView = itemView.findViewById(android.R.id.text1)

            init {
                itemView.isFocusable = true
                itemView.isFocusableInTouchMode = true

                itemView.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus && adapterPosition != RecyclerView.NO_POSITION) {
                        val app = appsList[adapterPosition]
                        speak(app.label)
                    }
                }

                itemView.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        val app = appsList[adapterPosition]
                        launchApp(app.packageName, app.label)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            holder.tvLabel.text = appsList[position].label
        }

        override fun getItemCount(): Int = appsList.size
    }
}
