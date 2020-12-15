package com.example.weatherapp

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.storage.PreferencesStorage
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

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
         inner class ResponseValues {
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
        }

        var parsedResponse: ResponseValues = ResponseValues()

        override fun onPreExecute() {
            super.onPreExecute()
            // Progress bar
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?

            val prevResponse: String? = parsedResponse.storage.getString(PreferencesStorage.RESPONSE);

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
                parsedResponse.storage.save(PreferencesStorage.RESPONSE, result!!)

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
            findViewById<TextView>(R.id.address).text = parsedResponse.address
            findViewById<TextView>(R.id.updated_at).text = parsedResponse.updatedAtText
            findViewById<TextView>(R.id.status).text = parsedResponse.weatherDescription?.capitalize()
            findViewById<TextView>(R.id.temp).text = parsedResponse.temp
            findViewById<TextView>(R.id.temp_min).text = parsedResponse.tempMin
            findViewById<TextView>(R.id.temp_max).text = parsedResponse.tempMax
            findViewById<TextView>(R.id.sunrise).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(parsedResponse.sunrise * 1000))
            findViewById<TextView>(R.id.sunset).text =
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(parsedResponse.sunset * 1000))
            findViewById<TextView>(R.id.wind).text = "${parsedResponse.windSpeed} m/s"
            findViewById<TextView>(R.id.humidity).text = "${parsedResponse.humidity}%"
        }

        private fun parseWeatherResponse(result: String?) {
            getMainKeys(result)
            getWind()
            getWeather()
            getUpdateDate()
            getTemperature()
            getHumidity()
            getSunUpDown()
            getAddress()
        }

        private fun getUpdateDate() {
            val updatedAt: Long = parsedResponse.jsonObj.getLong("dt")
            parsedResponse.updatedAtText =
                    "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                            Date(updatedAt * 1000)
                    )
        }

        private fun getAddress() {
            parsedResponse.address = parsedResponse.jsonObj.getString("name") + ", " +
                    parsedResponse.sys.getString("country")
        }

        private fun getSunUpDown() {
            parsedResponse.sunrise = parsedResponse.sys.getLong("sunrise")
            parsedResponse.sunset = parsedResponse.sys.getLong("sunset")
        }

        private fun getHumidity() {
            parsedResponse.humidity = parsedResponse.main.getString("humidity")
        }

        private fun getTemperature() {
            parsedResponse.temp = parsedResponse.main.getString("temp") + "°C"
            parsedResponse.tempMin = "Min Temp: " + parsedResponse.main.getString("temp_min") + "°C"
            parsedResponse.tempMax = "Max Temp: " + parsedResponse.main.getString("temp_max") + "°C"
        }

        private fun getWeather() {
            val weather = parsedResponse.jsonObj.getJSONArray("weather").getJSONObject(0)
            parsedResponse.weatherDescription = weather.getString("description")
        }

        private fun getWind() {
            parsedResponse.wind = parsedResponse.jsonObj.getJSONObject("wind")
            parsedResponse.windSpeed = parsedResponse.wind.getString("speed")
        }

        private fun getMainKeys(result: String?) {
            parsedResponse.jsonObj = JSONObject(result)
            parsedResponse.main = parsedResponse.jsonObj.getJSONObject("main")
            parsedResponse.sys = parsedResponse.jsonObj.getJSONObject("sys")
        }
    }
}
