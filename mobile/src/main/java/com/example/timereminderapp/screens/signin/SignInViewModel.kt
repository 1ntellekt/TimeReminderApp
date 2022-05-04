package com.example.timereminderapp.screens.signin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.timereminderapp.utils.REPOSITORY
import com.example.timereminderapp.utils.showToast

class SignInViewModel(application: Application) : AndroidViewModel(application) {

    fun signInEmail(email:String,password:String, onSuccess:()->Unit){
        REPOSITORY.signInEmail(email,password,{
                  onSuccess()
        }, {
            showToast(it)
        })
    }

    fun signInGoogle(token:String,onSuccess:()->Unit){
        REPOSITORY.signGoogle(false,token,{
            onSuccess()
        }, {
            showToast(it)
        })
    }

    fun initDatabase(){
        REPOSITORY.initDatabase()
    }

}