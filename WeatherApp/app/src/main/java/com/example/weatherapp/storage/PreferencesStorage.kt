package com.example.weatherapp.storage

import android.content.Context
import android.content.SharedPreferences
import java.lang.Exception
import kotlin.reflect.KProperty1

class PreferencesStorage(val context: Context) {
    private val name = "weather"

    companion object{
        const val RESPONSE: String = "response"
    }
    private val preferences: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    fun save(key: String, value: String){
        val editor = preferences.edit()
        try {
            editor.putString(key, value)
            editor.commit()
        }
        catch (e: Exception){
            println("Error while saving preference.")
        }
    }

    fun getString(key: String): String?{
        return preferences.getString(key, null);
    }

    fun remove(key: String){
        val editor = preferences.edit()
        try {
            editor.remove(key)
            editor.commit()
        }
        catch (e: Exception){
            println("Error while removing key.")
        }
    }
}