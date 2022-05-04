package com.example.timereminderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timereminderapp.R
import com.example.timereminderapp.adapters.listeners.AdapterClickListeners
import com.example.timereminderapp.models.NoteTask

class AdapterFiles(private val parentTask:NoteTask?,
    private val fileListener:AdapterClickListeners.OnFile?):RecyclerView.Adapter<AdapterFiles.FileHolder>() {

     val listFile = mutableListOf<String>()

    fun setData(list:List<String>){
        listFile.clear()
        listFile.addAll(list)
        notifyDataSetChanged()
    }

    fun deleteItem(pos:Int){
        listFile.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun updateList(list:List<String>){
        list.forEach {
            if (!listFile.contains(it)){
                listFile.add(it)
            }
        }
        notifyDataSetChanged()
    }

    inner class FileHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val tvFileName:TextView = itemView.findViewById(R.id.tvFileName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileHolder {
        return FileHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_file_task,parent,false))
    }

    override fun onViewAttachedToWindow(holder: FileHolder) {
        if (fileListener!=null){
            holder.itemView.setOnClickListener {
                val pos = holder.adapterPosition
                if(RecyclerView.NO_POSITION != pos){
                    fileListener.onFileClick(parentTask,pos)
                }
            }
        }
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: FileHolder) {
        holder.itemView.setOnClickListener(null)
        super.onViewDetachedFromWindow(holder)
    }

    override fun onBindViewHolder(holder: FileHolder, position: Int) {
        holder.tvFileName.text = listFile[position]
    }

    override fun getItemCount(): Int {
        return listFile.size
    }


}