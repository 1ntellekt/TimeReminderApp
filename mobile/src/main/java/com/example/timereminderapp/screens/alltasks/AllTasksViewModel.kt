package com.example.timereminderapp.screens.alltasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.timereminderapp.models.NoteTask
import com.example.timereminderapp.utils.*
import java.io.File

class AllTasksViewModel(application: Application) : AndroidViewModel(application) {

    val allTaskList = REPOSITORY.allTaskList

    fun addTask(noteTask: NoteTask,onSuccess:(String)->Unit){
        REPOSITORY.addNoteTask(noteTask,{
            saveReqCodeToPrefs(getReqCodePrefs()+1)
            setAlarmFromTask(noteTask)
            onSuccess(it)
        },{
            showToast(it)
        })
    }

    fun editTask(isTimeUpdate:Boolean,noteTask: NoteTask,onSuccess: () -> Unit){
        REPOSITORY.updateNoteTask(noteTask,{
            if (isTimeUpdate){
                saveReqCodeToPrefs(getReqCodePrefs()+1)
                setAlarmFromTask(noteTask)
            }
            onSuccess()
        },{
            showToast(it)
        })
    }

    fun doneTask(noteTask: NoteTask,onSuccess: () -> Unit){
        REPOSITORY.updateNoteTask(noteTask,{
            cancelAlarmFromTask(noteTask)
            onSuccess()
        },{
            showToast(it)
        })
    }

    fun deleteTask(noteTask: NoteTask, onSuccess: () -> Unit){
        REPOSITORY.deleteNoteTask(noteTask,{
           cancelAlarmFromTask(noteTask)
           onSuccess()
        },{
            showToast(it)
        })
    }

    fun downloadFileTask(noteTaskId:String, filename:String){
        REPOSITORY.downloadFile(noteTaskId, filename)
    }

    fun deleteFileTask(noteTaskId: String,filename: String){
        REPOSITORY.deleteFile(noteTaskId, filename)
    }

    fun uploadFileTask(noteTaskId: String,file:File){
        REPOSITORY.uploadFile(noteTaskId, file)
    }

}