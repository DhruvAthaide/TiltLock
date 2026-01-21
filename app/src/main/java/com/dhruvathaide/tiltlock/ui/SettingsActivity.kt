package com.dhruvathaide.tiltlock.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dhruvathaide.tiltlock.R
import com.dhruvathaide.tiltlock.data.SettingsRepository
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var settings: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings = SettingsRepository(this)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Ensure "ic_arrow_back" or default exists, standard Up behavior works
        toolbar.setNavigationOnClickListener { finish() }

        val seekSensitivity = findViewById<SeekBar>(R.id.seekSensitivity)
        val valSensitivity = findViewById<TextView>(R.id.valSensitivity)
        val switchPattern = findViewById<SwitchMaterial>(R.id.switchPattern)
        val btnLogs = findViewById<Button>(R.id.btnLogs)

        // Setup Sensitivity
        // Logic: 0 -> 0.5x, 50 -> 1.0x, 150 -> 2.0x
        // Actually lets map 0-100 range to 0.5 - 2.0
        // No, current xml max is 150.
        // Let's stick to simple linear: progress/100 + 0.5 (Wait if progress 50/100 = 0.5 + 0.5 = 1.0)
        // If max 150. 0 -> 0.5, 50 -> 1.0, 150 -> 2.0.
        // Formula: 0.5 + (progress / 100.0) matches perfectly.

        val currentSens = settings.sensitivity
        val progress = ((currentSens - 0.5f) * 100).toInt()
        seekSensitivity.progress = progress
        updateSensitivityLabel(valSensitivity, currentSens)

        seekSensitivity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = 0.5f + (progress / 100f)
                updateSensitivityLabel(valSensitivity, value)
                settings.sensitivity = value
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Setup Switch
        switchPattern.isChecked = settings.showPattern
        switchPattern.setOnCheckedChangeListener { _, isChecked ->
            settings.showPattern = isChecked
        }

        val switchSound = findViewById<SwitchMaterial>(R.id.switchSound)
        switchSound.isChecked = settings.soundEnabled
        switchSound.setOnCheckedChangeListener { _, isChecked -> settings.soundEnabled = isChecked }

        val switchHaptic = findViewById<SwitchMaterial>(R.id.switchHaptic)
        switchHaptic.isChecked = settings.hapticsEnabled
        switchHaptic.setOnCheckedChangeListener { _, isChecked -> settings.hapticsEnabled = isChecked }

        val switchGlitch = findViewById<SwitchMaterial>(R.id.switchGlitch)
        switchGlitch.isChecked = settings.glitchEnabled
        switchGlitch.setOnCheckedChangeListener { _, isChecked -> settings.glitchEnabled = isChecked }

        // Logs
        btnLogs.setOnClickListener {
             startActivity(Intent(this, BreakInLogActivity::class.java))
        }
    }

    private fun updateSensitivityLabel(view: TextView, value: Float) {
        view.text = String.format("%.1fx", value)
    }
}
