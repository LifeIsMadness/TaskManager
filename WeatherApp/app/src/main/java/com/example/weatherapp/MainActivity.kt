package com.example.weatherapp


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Debug
import android.util.DebugUtils
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnCheckout.setOnClickListener {
            var country = editText.text.toString()
            if(country.isNotEmpty()){
                val intent = Intent(this@MainActivity, WeatherActivity::class.java)
                intent.putExtra("country", country);
                startActivity(intent);
            }
        }
    }
}