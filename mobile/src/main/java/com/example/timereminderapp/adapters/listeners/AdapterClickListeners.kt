package com.example.timereminderapp.adapters.listeners

import com.example.timereminderapp.models.NoteTask

interface AdapterClickListeners {

    fun interface OnDone{
        fun onDoneClick(pos:Int)
    }

    fun interface OnEdit{
        fun onEditClick(pos:Int)
    }

    fun interface OnDel{
        fun onDelClick(pos:Int)
    }

    fun interface OnRef{
        fun onRefClick(pos:Int)
    }

    fun interface OnDelFav{
        fun onDelFavClick(pos:Int)
    }

    fun interface OnFile{
        fun onFileClick(parentTask:NoteTask?,pos:Int)
    }

    fun interface OnItemClick{
        fun onItemClick(posParent:Int)
    }

}