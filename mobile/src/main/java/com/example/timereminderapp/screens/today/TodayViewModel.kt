package com.example.timereminderapp.screens.today

import android.app.Application
import android.os.Environment.DIRECTORY_DOWNLOADS
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.timereminderapp.models.NoteTask
import com.example.timereminderapp.utils.*
import java.io.File

class TodayViewModel(application: Application) : AndroidViewModel(application) {


    val todayTasks = REPOSITORY.todayTaskList

    fun addNoteTask(noteTask: NoteTask,onSuccess:(String)->Unit){
        REPOSITORY.addNoteTask(noteTask,{
            saveReqCodeToPrefs(getReqCodePrefs() +1)
            setAlarmFromTask(noteTask)
            onSuccess(it)
        },{
            showToast(it)
        })
    }

    fun deleteTask(noteTask: NoteTask,onSuccess:()->Unit){
        REPOSITORY.deleteNoteTask(noteTask,{
            cancelAlarmFromTask(noteTask)
            onSuccess()
        },{
            showToast(it)
        })
    }

    fun editTask(isUpdateTime:Boolean,noteTask: NoteTask,onSuccess:()->Unit){
        REPOSITORY.updateNoteTask(noteTask,{
            if (isUpdateTime){
                saveReqCodeToPrefs(getReqCodePrefs() +1)
                setAlarmFromTask(noteTask)
            }
            onSuccess()
        },{
            showToast(it)
        })
    }

    fun doneTask(noteTask: NoteTask,onSuccess:()->Unit){
        REPOSITORY.updateNoteTask(noteTask,{
            cancelAlarmFromTask(noteTask)
            onSuccess()
        },{
            showToast(it)
        })
    }

    fun uploadFile(noteTaskId:String,file: File){
        REPOSITORY.uploadFile(noteTaskId, file)
    }

    fun deleteFile(noteTaskId: String,fileName:String){
        REPOSITORY.deleteFile(noteTaskId,fileName)
    }

    fun downloadFile(noteTaskId: String, filename: String) {
        REPOSITORY.downloadFile(noteTaskId,filename)
    }

}