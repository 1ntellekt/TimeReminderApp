package com.example.timereminderapp.models

import java.io.Serializable

data class NoteTask (
    val id:String="",
    val user_id:String="",
    val name:String="",
    val description:String="",
    val fileList:MutableList<String> = mutableListOf<String>(),
    @field:JvmField
    val isFavorite:Boolean = false,
    val tag:String="",
    val time:String="",
    val date:String="",
    val requestCode:Int=-1,
    val status:Int=0
):Serializable
