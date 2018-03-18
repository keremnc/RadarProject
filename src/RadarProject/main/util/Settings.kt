package main.util

import com.badlogic.gdx.graphics.Color
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import java.io.File

class Settings {

    data class jsonsettings(
            val hubFont_size: Int = 30,
            val hubFont_color: Color = Color.WHITE,

            val espFont_size: Int = 16,
            val espFont_color: Color = Color.WHITE,

            val largeFont_size: Int = 38,
            val largeFont_color: Color = Color.WHITE,

            val littleFont_size: Int = 15,
            val littleFont_color: Color = Color.WHITE,

            val nameFont_size: Int = 10,
            val nameFont_color: Color = Color.WHITE,

            val itemFont_size: Int = 6,
            val itemFont_color: Color = Color.WHITE
            )

    val settingsname = "settings.json"

    fun loadsettings(): jsonsettings {
        checkifsettingsexists()
        val f = File(settingsname)
        if (!f.canRead()) {
            throw SecurityException("Can't read settings.json")
        }


        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(jsonsettings::class.java)
        val set = adapter.fromJson(f.readText())
        if (set != null) {
            return set
        }else{
            throw NullPointerException()
        }
    }

    fun savesettings(settings: jsonsettings) {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(jsonsettings::class.java)
        val json = adapter.toJson(settings)
        val f = File(settingsname)
        f.writeText(json)
    }

    fun checkifsettingsexists() {
        val f = File(settingsname)
        if (!f.exists()) {
            savesettings(jsonsettings())
        }
    }

}