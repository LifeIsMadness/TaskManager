package com.example.weatherapp

import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.util.DebugUtils
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.storage.PreferencesStorage
import org.json.JSONObject
import java.net.URL
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import java.util.prefs.Preferences
import kotlin.concurrent.thread

class WeatherActivity : AppCompatActivity() {

    lateinit var CITY: String
    val API: String = BuildConfig.TOKEN
    lateinit var API_URL: String

    fun getAPIUrl(city: String, api: String):String{
        return "https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$api"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CITY = intent.getStringExtra("country").toString()
        API_URL = getAPIUrl(CITY, API);

        setContentView(R.layout.country_input)
        weatherTask().execute()
    }



    inner class weatherTask() : AsyncTask<String, Void, String>() {
        // some parsed properties
        lateinit var jsonObj: JSONObject
        lateinit var main: JSONObject
        lateinit var sys: JSONObject
        lateinit var wind: JSONObject
        var address: String? = null
        var updatedAtText: String? = null
        var weatherDescription: String? = null
        var temp: String? = null
        var tempMin: String? = null
        var tempMax: String? = null
        var sunrise: Long = 0
        var sunset: Long = 0
        var windSpeed: String? = null
        var humidity: String? = null
        val storage: PreferencesStorage = PreferencesStorage(applicationContext)

        override fun onPreExecute() {
            super.onPreExecute()
            // Progress bar
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?

            val prevResponse: String? = storage.getString(PreferencesStorage.RESPONSE);

            try{
                response = URL(API_URL).readText(
                    Charsets.UTF_8
                )
                println(response);
            }
            catch (e: Exception){
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Nothing Found! Retrieving previous data...",
                        Toast.LENGTH_SHORT).show()
                }

                response = null
            }
            return response ?: prevResponse
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                //Extracting JSON returns from the API
                parseWeatherResponse(result)

                //Populating our views
                setViewsData()

                // Showing the main design
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
                storage.save(PreferencesStorage.RESPONSE, result!!)

            } catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE

                Toast.makeText(
                    applicationContext,
                    "No saved responses. Try to search a new city.",
                    Toast.LENGTH_SHORT).show()
            }
        }

        private fun setViewsData() {
            findViewById<TextView>(R.id.address).text = address
            findViewById<TextView>(R.id.updated_at).text = updatedAtText
            findViewById<TextView>(R.id.status).text = weatherDescription?.capitalize()
            findViewById<TextView>(R.id.temp).text = temp
            findViewById<TextView>(R.id.temp_min).text = tempMin
            findViewById<TextView>(R.id.temp_max).text = tempMax
            findViewById<TextView>(R.id.sunrise).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
            findViewById<TextView>(R.id.sunset).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
            findViewById<TextView>(R.id.wind).text = "$windSpeed m/s"
            findViewById<TextView>(R.id.humidity).text = "$humidity%"
        }

        private fun parseWeatherResponse(result: String?) {
            jsonObj = JSONObject(result)
            main = jsonObj.getJSONObject("main")
            sys = jsonObj.getJSONObject("sys")
            wind = jsonObj.getJSONObject("wind")

            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
            val updatedAt: Long = jsonObj.getLong("dt")
            updatedAtText =
                "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                    Date(updatedAt * 1000)
                )

            temp = main.getString("temp") + "°C"
            tempMin = "Min Temp: " + main.getString("temp_min") + "°C"
            tempMax = "Max Temp: " + main.getString("temp_max") + "°C"
            humidity = main.getString("humidity")
            sunrise = sys.getLong("sunrise")
            sunset = sys.getLong("sunset")
            windSpeed = wind.getString("speed")
            weatherDescription = weather.getString("description")
            address = jsonObj.getString("name") + ", " + sys.getString("country")
        }
    }
}
