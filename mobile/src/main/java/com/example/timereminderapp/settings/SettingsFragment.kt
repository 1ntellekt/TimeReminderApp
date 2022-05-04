package com.example.timereminderapp.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.timereminderapp.R
import com.example.timereminderapp.utils.showToast
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener

class SettingsFragment : PreferenceFragmentCompat(),Preference.OnPreferenceChangeListener,SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val myPrefDay = findPreference<EditTextPreference>("day_remind")
        val myPrefHour = findPreference<EditTextPreference>("hours_remind")
        val myPrefMin = findPreference<EditTextPreference>("min_remind")


        myPrefDay?.onPreferenceChangeListener = this
        myPrefHour?.onPreferenceChangeListener = this
        myPrefMin?.onPreferenceChangeListener = this

    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
            when(preference?.key){
                "day_remind" -> {
                    try {
                        val days = newValue.toString().toInt()

                    }catch (e:NumberFormatException){
                        showToast("Введен некорректный формат!")
                        return false
                    }
                }
                "hours_remind" -> {
                    try {
                        val hours = newValue.toString().toInt()

                    }catch (e:NumberFormatException){
                        showToast("Введен некорректный формат!")
                        return false
                    }
                }
                "min_remind" -> {
                    try {
                        val min = newValue.toString().toInt()
                    }catch (e:NumberFormatException){
                        showToast("Введен некорректный формат!")
                        return false
                    }
                }
            }
            return true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            /*val pref = key?.let { findPreference<EditTextPreference>(it) }

            val value = sharedPreferences?.getString(key,"")

            if (pref!=null && value!=null){
                pref.summary = value
            }*/
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }


}