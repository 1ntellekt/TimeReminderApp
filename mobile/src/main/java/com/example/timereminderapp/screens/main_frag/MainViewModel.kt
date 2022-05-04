package com.example.timereminderapp.screens.main_frag

import android.app.Application
import android.app.ProgressDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.timereminderapp.models.CurrentUser
import com.example.timereminderapp.utils.APP_ACTIVITY
import com.example.timereminderapp.utils.REPOSITORY
import com.example.timereminderapp.utils.showToast

class MainViewModel(application: Application) : AndroidViewModel(application) {

    fun getData(){
        val progressDialog = ProgressDialog(APP_ACTIVITY)
        progressDialog.setTitle("Ожидание сервера....")
        progressDialog.show()
        REPOSITORY.initDatabase()
        REPOSITORY.getCurrentUser({
            if (progressDialog.isShowing)
                progressDialog.dismiss()

                REPOSITORY.checkNoteTasks(
                    {
                       // showToast("Data get successfully!")
                    },{ showToast(it)})



        },{ showToast(it)})
    }

}