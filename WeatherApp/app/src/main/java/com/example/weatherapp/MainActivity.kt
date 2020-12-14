package com.example.weatherapp


import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.receivers.ConnectivityReceiver
import com.example.weatherapp.storage.PreferencesStorage
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    private var snackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(ConnectivityReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        btnCheckout.setOnClickListener {
            var country = editText.text.toString()
            if(country.isNotEmpty()){
                val intent = Intent(this@MainActivity, WeatherActivity::class.java)
                intent.putExtra("country", country);
                startActivity(intent);
            }
        }

        btnClear.setOnClickListener {
            PreferencesStorage(this).remove(PreferencesStorage.RESPONSE)
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showNetworkMessage(isConnected)
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {
            showSnackbar()

            disableCheckout()

        } else {
            snackbar?.dismiss()
            enableCheckout()
        }
    }

    private fun disableCheckout() {
        btnCheckout.isEnabled = false;
        btnCheckout.isClickable = false;
        btnCheckout.setBackgroundColor(resources.getColor(R.color.design_default_color_error))
    }

    private fun enableCheckout() {
        btnCheckout.isEnabled = true;
        btnCheckout.isClickable = true;
        btnCheckout.setBackgroundColor(resources.getColor(R.color.teal_200))
    }

    private fun showSnackbar() {
        snackbar = Snackbar.make(
            findViewById(R.id.btnCheckout),
            "You are offline",
            Snackbar.LENGTH_LONG
        )
        snackbar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
        snackbar?.show()
    }
}