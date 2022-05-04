package com.example.timereminderapp.utils

import android.app.AlarmManager
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.text.BoringLayout
import android.widget.Toast
import com.example.timereminderapp.MainActivity
import com.example.timereminderapp.R
import com.example.timereminderapp.models.NoteTask
import com.example.timereminderapp.receiver.AlarmReceiver
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


fun showToast(msg:String){
    Toast.makeText(APP_ACTIVITY,msg,Toast.LENGTH_SHORT).show()
}

//Alarm
fun setAlarmFromTask(noteTask: NoteTask){

    val alarmManager = APP_ACTIVITY.getSystemService(ALARM_SERVICE) as AlarmManager

    val sdf = SimpleDateFormat("$DATE_PATTERN $TIME_PATTERN",Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.time = sdf.parse("${noteTask.date} ${noteTask.time}")!!

    val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis,getInfoPendingIntent())
    alarmManager.setAlarmClock(alarmClockInfo, getActionPendingIntent(noteTask))
}

fun cancelAlarmFromTask(noteTask: NoteTask){
    val alarmManager = APP_ACTIVITY.getSystemService(ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(getActionPendingIntent(noteTask))
}

//Pending
private fun getInfoPendingIntent():PendingIntent{
    val infoIntent = Intent(APP_ACTIVITY, MainActivity::class.java)
    infoIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    return PendingIntent.getActivity(APP_ACTIVITY.applicationContext, INFO_PENDING_CODE,infoIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}
private fun getActionPendingIntent(noteTask: NoteTask):PendingIntent{
    val intentNotify = Intent(APP_ACTIVITY.applicationContext,AlarmReceiver::class.java).putExtra("noteTask",noteTask)
    return PendingIntent.getBroadcast(APP_ACTIVITY.applicationContext,noteTask.requestCode,intentNotify,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}

//Google Client
fun getGoogleClient():GoogleSignInClient{
  val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(APP_ACTIVITY.getString(R.string.default_web_client_id))
      .requestEmail().build()
    return GoogleSignIn.getClient(APP_ACTIVITY,gso)
}

//Prefs
fun getReqCodePrefs():Int{
    return APP_ACTIVITY.getSharedPreferences("req_code",MODE_PRIVATE).getInt("code",-1)
}
fun saveReqCodeToPrefs(code:Int){
    val sp = APP_ACTIVITY.getSharedPreferences("req_code",MODE_PRIVATE).edit()
    sp.putInt("code",code)
    sp.apply()
}
fun isAuthUser(isInit:Boolean){
   val sp = APP_ACTIVITY.getSharedPreferences("user_auth",MODE_PRIVATE).edit()
    sp.putBoolean("auth",isInit)
    sp.apply()
}
fun getIsAuthUser():Boolean{
    return APP_ACTIVITY.getSharedPreferences("user_auth",MODE_PRIVATE).getBoolean("auth",false)
}

//File on input
fun getFileFromResolver(filename:String,inputStream:InputStream?):File?{
    inputStream?.let { input ->
        try {
            val file = File(filename)
            val outputStream = FileOutputStream(file)
            var len = 0
            val bytes = ByteArray(1024)
            while (input.read(bytes).also{ len = it }!=-1){
               outputStream.write(bytes,0,len)
            }
            outputStream.flush()
            outputStream.close()
            input.close()
            return file
        }catch (e:IOException){
            input.close()
            e.printStackTrace()
        }
    }
    return null
}

//Download manager
fun downloadFilesManager(filename: String,destinationDir:String,url:String){
    val downloadManager = APP_ACTIVITY.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
    val downloadRequest = DownloadManager.Request(Uri.parse(url))
    downloadRequest.apply {
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalFilesDir(APP_ACTIVITY,destinationDir,filename)
        setTitle("Download a $filename")
    }
    downloadManager.enqueue(downloadRequest)
}

//getFileName
fun Uri.getName(): String? {
    val returnCursor = APP_ACTIVITY.contentResolver.query(this, null, null, null, null)
    val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor?.moveToFirst()
    val fileName = nameIndex?.let { returnCursor.getString(it) }
    returnCursor?.close()
    return fileName
}