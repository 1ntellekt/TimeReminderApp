package com.example.timereminderapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.example.timereminderapp.adapters.AdapterNoteTask
import com.example.timereminderapp.data.NoteTask
import com.example.timereminderapp.data.TodayTasks
import com.example.timereminderapp.databinding.ActivityTodayBinding
import com.example.timereminderapp.utils.TASKS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class TodayActivity : AppCompatActivity() {

    private val DATE_PATTERN="yyyy-MM-dd"
   private val TIME_PATTERN="HH:mm:ss"

    private lateinit var binding: ActivityTodayBinding

    private lateinit var adapterNoteTask: AdapterNoteTask

    private var tasks = FirebaseFirestore.getInstance().collection("tasks")

    private var currentUser = FirebaseAuth.getInstance().currentUser


    private lateinit var mObserver:Observer<List<NoteTask>>

    private lateinit var tasksFromDB: TodayTasks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TASKS = tasks
        tasksFromDB = TodayTasks()


        adapterNoteTask = AdapterNoteTask({ pos->
            val noteTask = adapterNoteTask.listNoteTasks[pos]
            doneTask(pos,noteTask)
        },{ pos->
            val noteTask = adapterNoteTask.listNoteTasks[pos]
            deleteTask(noteTask)
        })

        binding.apply {
            tvHead.text = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapterNoteTask

        }

        mObserver = Observer {
            adapterNoteTask.setData(it)
        }

        tasksFromDB.observe(this,mObserver)

    }

    private fun doneTask(pos: Int, noteTask: NoteTask) {
        tasks.document(noteTask.id).update("status",1).addOnSuccessListener {
            adapterNoteTask.updateItem(pos,noteTask.copy(status = 1))
        }
    }

    override fun onStart() {
        super.onStart()
    }



    private fun deleteTask(noteTask: NoteTask){
        tasks.document(noteTask.id).delete().addOnSuccessListener {

            noteTask.fileList.forEach { file->
                val refFile = FirebaseStorage.getInstance().reference.child("${currentUser?.uid}/${noteTask.id}/$file")
                refFile.delete()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tasksFromDB.removeObserver(mObserver)
    }


}