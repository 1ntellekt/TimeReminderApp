package com.example.timereminderapp.utils

import android.content.SharedPreferences
import com.example.timereminderapp.MainActivity
import com.example.timereminderapp.database.DataRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.model.DocumentCollections


lateinit var APP_ACTIVITY:MainActivity
lateinit var REPOSITORY:DataRepository

lateinit var TASKS:CollectionReference

const val INFO_PENDING_CODE=9999

const val DATE_PATTERN="yyyy-MM-dd"
const val TIME_PATTERN="HH:mm:ss"

var HOURS = 1
var MINS = 20
var DAYS = 1


lateinit var SETTING_Prefs:SharedPreferences