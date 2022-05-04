package com.example.timereminderapp.screens.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.timereminderapp.utils.REPOSITORY
import com.example.timereminderapp.utils.showToast

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    fun signUpEmail(email:String,password:String, onSuccess:()->Unit){
        REPOSITORY.singUpEmail(email,password,{
            onSuccess()
        }, {
            showToast(it)
        })
    }

    fun signUpGoogle(token:String,onSuccess:()->Unit){
        REPOSITORY.signGoogle(true,token,{
            onSuccess()
        }, {
            showToast(it)
        })
    }

}