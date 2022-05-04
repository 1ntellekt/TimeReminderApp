package com.example.timereminderapp

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.example.timereminderapp.database.FirebaseRepository
import com.example.timereminderapp.utils.*

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    lateinit var mNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        APP_ACTIVITY = this
        REPOSITORY = FirebaseRepository()
        SETTING_Prefs = PreferenceManager.getDefaultSharedPreferences(this)
        mNavController = Navigation.findNavController(this,R.id.fragmentHostNavMenu)


        HOURS = SETTING_Prefs.getString("hours_remind","1")?.toInt() ?: 1
        MINS = SETTING_Prefs.getString("min_remind","20")?.toInt() ?: 20
        DAYS = SETTING_Prefs.getString("day_remind","1")?.toInt() ?: 1


        SETTING_Prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        sharedPreferences?.let { preferences ->
               when(key){
                   "hours_remind"->{
                       HOURS = preferences.getString(key,"1")?.toInt() ?: 1
                   }
                   "min_remind"->{
                       MINS = preferences.getString(key,"20")?.toInt() ?: 20
                   }
                   "day_remind"->{
                       DAYS = preferences.getString(key,"1")?.toInt() ?: 1
                   }
               }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        SETTING_Prefs.unregisterOnSharedPreferenceChangeListener(this)
    }


}