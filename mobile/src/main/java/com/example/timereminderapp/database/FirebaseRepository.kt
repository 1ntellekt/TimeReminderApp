package com.example.timereminderapp.database

import android.net.Uri
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.timereminderapp.database.note_types.AllNoteTask
import com.example.timereminderapp.database.note_types.CompletedNoteTask
import com.example.timereminderapp.database.note_types.TodayNoteTask
import com.example.timereminderapp.models.CurrentUser
import com.example.timereminderapp.models.NoteTask
import com.example.timereminderapp.utils.*
import com.google.firebase.auth.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class FirebaseRepository : DataRepository {

    private val auth = FirebaseAuth.getInstance()
    private val dbFireStore = FirebaseFirestore.getInstance()

    private var users:CollectionReference = dbFireStore.collection("users")
    private var tasks:CollectionReference = dbFireStore.collection("tasks")
    private val fireStorage = FirebaseStorage.getInstance()
    private lateinit var currentUserDoc:DocumentReference

    init {
        TASKS = tasks
    }

    override val todayTaskList: LiveData<List<NoteTask>>
        get() = TodayNoteTask()
    override val allTaskList: LiveData<List<NoteTask>>
        get() = AllNoteTask()
    override val completedTaskList: LiveData<List<NoteTask>>
        get() = CompletedNoteTask()

    override fun initDatabase() {
        CurrentUser.id = auth.currentUser?.uid.toString()
        CurrentUser.email = auth.currentUser?.email.toString()
        currentUserDoc = users.document(CurrentUser.id)
    }

    override fun signInEmail(email: String, password: String, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                initDatabase()
                onSuccess()
                //setUser({onSuccess()}, {onFail(it)})
            }
            .addOnFailureListener { onFail(it.message.toString()) }
    }

    override fun singUpEmail(email: String, password: String, onSuccess: () -> Unit, onFail: (String) -> Unit) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult-> sendVerifyEmail(authResult.user!!,{onSuccess()},{onFail(it)}) }
                .addOnFailureListener { exception->
                    if (exception is FirebaseAuthUserCollisionException)
                    {
                        if (exception.errorCode=="ERROR_EMAIL_ALREADY_IN_USE"){
                            linkEmailToGoogle(email,password, {onSuccess()}, {onFail(it)})
                        }
                    }
                    else onFail(exception.message.toString())
                }
    }

    override fun sendVerifyEmail(user: FirebaseUser, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        user.sendEmailVerification()
            .addOnSuccessListener {
                initDatabase()
                setUser({onSuccess()}, {onFail(it)})
            }
            .addOnFailureListener { onFail(it.message.toString()) }
    }

    override fun linkEmailToGoogle(email: String,password: String, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        val credential = EmailAuthProvider.getCredential(email,password)
        auth.currentUser?.linkWithCredential(credential)
            ?.addOnSuccessListener {
                auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                    initDatabase()
                    setUser({onSuccess()}, {onFail(it)})
                }?.addOnFailureListener { onFail(it.message.toString()) }
            }
            ?.addOnFailureListener { onFail(it.message.toString()) }
    }

    override fun signGoogle(isSignUp:Boolean,token: String, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(token,null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                initDatabase()
                if (isSignUp) setUser({onSuccess()},{onFail(it)})
                else onSuccess()
            }
            .addOnFailureListener { onFail(it.message.toString()) }
    }

    override fun updateEmailAuth(email: String, password: String, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        val credential = EmailAuthProvider.getCredential(CurrentUser.email,password)
        val user = auth.currentUser
        user?.reauthenticate(credential)
            ?.addOnSuccessListener {
            user.updateEmail(email).addOnSuccessListener {
                CurrentUser.email = email
                user.sendEmailVerification()
                editCurrentUser({},{})
                onSuccess() }
                .addOnFailureListener { onFail(it.message.toString()) }
        }?.addOnFailureListener { onFail(it.message.toString()) }
    }

    override fun signOut() {
        if (getIsAuthUser()){
            isAuthUser(false)
        }
    }



    override fun getCurrentUser(onSuccess: () -> Unit, onFail: (String) -> Unit) {
        currentUserDoc.get().addOnSuccessListener { doc->
            CurrentUser.id = doc["id"] as String
            CurrentUser.nickname = doc["nickname"] as String
            CurrentUser.email = doc["email"] as String
            CurrentUser.password = doc["password"] as String
            CurrentUser.lastname = doc["lastname"] as String
            CurrentUser.firstname= doc["firstname"] as String
            if (CurrentUser.email != auth.currentUser!!.email){
                CurrentUser.email = auth.currentUser!!.email.toString()
                editCurrentUser({onSuccess()},{onFail(it)})
            } else onSuccess()
        }.addOnFailureListener {
            onFail(it.message.toString())
        }
    }

    override fun setUser(onSuccess: () -> Unit, onFail: (String) -> Unit) {
        currentUserDoc.set(
            mapOf(
                "id" to CurrentUser.id,
                "email" to CurrentUser.email,
                "nickname" to CurrentUser.nickname,
                "firstname" to CurrentUser.firstname,
                "lastname" to CurrentUser.lastname,
                "password" to CurrentUser.password
            )
        ).addOnSuccessListener { onSuccess() }
         .addOnFailureListener { onFail(it.message.toString())}
    }

    override fun editCurrentUser(onSuccess: () -> Unit, onFail: (String) -> Unit) {
        currentUserDoc.update(mapOf(
            "id" to CurrentUser.id,
            "email" to CurrentUser.email,
            "nickname" to CurrentUser.nickname,
            "firstname" to CurrentUser.firstname,
            "lastname" to CurrentUser.lastname,
            "password" to CurrentUser.password
        )).addOnSuccessListener {onSuccess()}
          .addOnFailureListener {onFail(it.message.toString())}
    }



    override fun addNoteTask(noteTask: NoteTask, onSuccess: (String) -> Unit, onFail: (String) -> Unit) {
        val docId = tasks.document().id
        tasks.document(docId).set(mapOf(
            "id" to docId,
            "user_id" to CurrentUser.id,
            "name" to noteTask.name,
            "description" to noteTask.description,
            "fileList" to noteTask.fileList,
            "isFavorite" to noteTask.isFavorite,
            "tag" to noteTask.tag,
            "time" to noteTask.time,
            "date" to noteTask.date,
            "requestCode" to noteTask.requestCode,
            "status" to noteTask.status
        )).addOnSuccessListener { onSuccess(docId) }
            .addOnFailureListener { onFail(it.message.toString()) }
    }

    override fun updateNoteTask(noteTask: NoteTask, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        tasks.document(noteTask.id).update(
            mapOf(
                "id" to noteTask.id,
                "user_id" to CurrentUser.id,
                "name" to noteTask.name,
                "description" to noteTask.description,
                "fileList" to noteTask.fileList,
                "isFavorite" to noteTask.isFavorite,
                "tag" to noteTask.tag,
                "time" to noteTask.time,
                "date" to noteTask.date,
                "requestCode" to noteTask.requestCode,
                "status" to noteTask.status
        )).addOnSuccessListener { onSuccess() }.addOnFailureListener { onFail(it.message.toString()) }
    }

    override fun deleteNoteTask(noteTask: NoteTask, onSuccess: () -> Unit, onFail: (String) -> Unit) {
        tasks.document(noteTask.id).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFail(it.message.toString()) }
    }

    override fun checkNoteTasks(onSuccess: () -> Unit, onFail: (String) -> Unit) {
        tasks.get().addOnSuccessListener { shaps->

            for (doc in shaps){
                val noteTask = doc.toObject(NoteTask::class.java)
                if (noteTask.user_id==CurrentUser.id && noteTask.status!=1 && noteTask.status!=2){
                    val nowDate = Date()
                    val curDate = SimpleDateFormat("$DATE_PATTERN $TIME_PATTERN", Locale.getDefault()).parse("${noteTask.date} ${noteTask.time}")!!

                    val difference = nowDate.time - curDate.time

                    val hours = (difference/(60 * 60 * 1000)).toInt()
                    val minutes = (difference/(60 * 1000)).toInt()
                    val seconds = (difference / 1000).toInt()
                    val days = (difference/(24 * 60 * 60 * 1000)).toInt()

                    if (minutes >= MINS && hours >= HOURS && days >= DAYS){
                        updateNoteTask(noteTask.copy(status = 2),{},{})
                    }

                }
            }
            onSuccess()

        }.addOnFailureListener { onFail(it.message.toString()) }
    }



    override fun uploadFile(noteTaskId: String, file: File) {
        val refUpload = fireStorage.reference.child("${CurrentUser.id}/$noteTaskId/${file.name}")
        val uploadTask = refUpload.putFile(Uri.fromFile(file))
        uploadTask.continueWithTask { task->
            if (!task.isSuccessful){
                task.exception?.let { throw it }
            }
            refUpload.downloadUrl
        }.addOnSuccessListener {
            Log.d("tag","downloaded file ${file.name} with url:${it}")
        }.addOnFailureListener {
            Log.d("tag","error downloaded file ${file.name} error: ${it.message.toString()}")
        }
    }

    override fun deleteFile(noteTaskId: String, filename: String) {
        val refDelete = fireStorage.reference.child("${CurrentUser.id}/$noteTaskId/$filename")
         refDelete.delete().addOnSuccessListener {
             Log.d("tag","Deleted successfully file $filename ")
         }.addOnFailureListener {
             Log.d("tag","error delete file $filename with error: ${it.message.toString()}")
         }
    }

    override fun downloadFile(noteTaskId: String, filename: String) {
        val refDownload = fireStorage.reference.child("${CurrentUser.id}/$noteTaskId/$filename")
        refDownload.downloadUrl.addOnSuccessListener {
            downloadFilesManager(filename, DIRECTORY_DOWNLOADS,it.toString())
        }.addOnFailureListener {
            Log.e("tag","error download files: ${it.message.toString()}")
        }
    }

}