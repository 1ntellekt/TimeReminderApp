package com.example.timereminderapp.adapters

interface AdapterListeners{

    fun interface OnItemListener {
        fun onItemClick(pos:Int)
    }

    fun interface OnItemLongListener {
        fun onItemClick(pos:Int)
    }


}