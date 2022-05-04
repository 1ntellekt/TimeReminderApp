package com.example.timereminderapp.database

import android.net.Uri
import androidx.lifecycle.LiveData
import com.example.timereminderapp.models.NoteTask
import com.google.firebase.auth.FirebaseUser
import java.io.File

interface DataRepository {

    val todayTaskList:LiveData<List<NoteTask>>
    val allTaskList:LiveData<List<NoteTask>>
    val completedTaskList:LiveData<List<NoteTask>>

    fun initDatabase()

    fun signInEmail(email:String,password:String,onSuccess:()->Unit,onFail:(String)->Unit)

    fun singUpEmail(email:String,password:String,onSuccess:()->Unit,onFail:(String)->Unit)

    fun sendVerifyEmail(user:FirebaseUser, onSuccess: () -> Unit,onFail: (String) -> Unit)

    fun linkEmailToGoogle(email: String,password: String,onSuccess: () -> Unit,onFail: (String) -> Unit)

    fun signGoogle(isSignUp:Boolean,token:String,onSuccess: () -> Unit,onFail: (String) -> Unit)

    fun updateEmailAuth(email: String,password: String,onSuccess: () -> Unit,onFail: (String) -> Unit)

    fun signOut()


    fun getCurrentUser(onSuccess:()->Unit,onFail:(String)->Unit)

    fun setUser(onSuccess:()->Unit,onFail:(String)->Unit)

    fun editCurrentUser(onSuccess:()->Unit,onFail:(String)->Unit)


    fun addNoteTask(noteTask: NoteTask, onSuccess: (String) -> Unit,onFail: (String) -> Unit)

    fun updateNoteTask(noteTask: NoteTask,onSuccess: () -> Unit,onFail: (String) -> Unit)

    fun deleteNoteTask(noteTask: NoteTask,onSuccess: () -> Unit,onFail: (String) -> Unit)

    fun checkNoteTasks(onSuccess: () -> Unit,onFail: (String) -> Unit)


    fun uploadFile(noteTaskId:String,file:File)

    fun deleteFile(noteTaskId: String,filename:String)

    fun downloadFile(noteTaskId: String,filename: String)

}