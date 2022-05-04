package com.example.timereminderapp.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.timereminderapp.utils.REPOSITORY
import com.example.timereminderapp.utils.showToast

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    fun singOut(){
        REPOSITORY.signOut()
    }

    fun editProfile(onSuccess:()->Unit){
        REPOSITORY.editCurrentUser({
            onSuccess()
        },{ showToast(it)})
    }

    fun updateEmail(email: String,password:String, onSuccess: () -> Unit) {
        REPOSITORY.updateEmailAuth(email,password,{
           onSuccess()
        },{ showToast(it)})
    }


}