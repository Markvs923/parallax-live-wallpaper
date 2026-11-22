package com.parallax.wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val seekBar = findViewById<SeekBar>(R.id.depthSeekBar)
        seekBar.progress = 50 // 1.0x default

        // In a full version you'd save to SharedPreferences and read in the Engine

        findViewById<Button>(R.id.setWallpaperButton).setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this, ParallaxWallpaperService::class.java)
            )
            startActivity(intent)
        }
    }
}
