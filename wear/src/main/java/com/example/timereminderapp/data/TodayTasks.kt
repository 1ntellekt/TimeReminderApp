package com.example.timereminderapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.timereminderapp.utils.TASKS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

class TodayTasks:LiveData<List<NoteTask>>() {


    private lateinit var listenerRegistration: ListenerRegistration

    override fun onActive() {
        listenerRegistration = TASKS.whereEqualTo("user_id",FirebaseAuth.getInstance().currentUser?.uid).addSnapshotListener { snaps, error ->
            if (error !=null){
                Log.e("tag", "error get tasks error: ${error.message.toString()}")
                return@addSnapshotListener
            }
            if (snaps!=null){
                val listTasks = mutableListOf<NoteTask>()
                for (doc in snaps){
                    val noteTask = doc.toObject(NoteTask::class.java)
                    listTasks.add(noteTask)
                }
                value = listTasks
            }
        }
        super.onActive()
    }

    override fun onInactive() {
        super.onInactive()
    }

}