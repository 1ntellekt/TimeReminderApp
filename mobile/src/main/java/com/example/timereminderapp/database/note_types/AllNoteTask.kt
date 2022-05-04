package com.example.timereminderapp.database.note_types

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.timereminderapp.models.CurrentUser
import com.example.timereminderapp.models.NoteTask
import com.example.timereminderapp.utils.DATE_PATTERN
import com.example.timereminderapp.utils.TASKS
import com.example.timereminderapp.utils.cancelAlarmFromTask
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class AllNoteTask : LiveData<List<NoteTask>>() {

    private lateinit var listenerRegistration: ListenerRegistration

    override fun onActive() {
        listenerRegistration = TASKS.whereEqualTo("user_id", CurrentUser.id)
            .addSnapshotListener{ snaps, e ->
                if(e!=null){
                    Log.e("tag","error: ${e.message.toString()}")
                    return@addSnapshotListener
                }
                if (snaps!=null){
                    val listTasks = mutableListOf<NoteTask>()
                    for (doc in snaps){
                        val noteTask = doc.toObject(NoteTask::class.java)
                        listTasks.add(noteTask)
                        if (noteTask.status!=0){
                            cancelAlarmFromTask(noteTask)//test
                        }
                    }
                    value = listTasks.sortedBy{ it.time }.sortedBy{it.date}.sortedBy { it.status }
                }
            }
        super.onActive()
    }

    override fun onInactive() {
        listenerRegistration.remove()
        super.onInactive()
    }


}