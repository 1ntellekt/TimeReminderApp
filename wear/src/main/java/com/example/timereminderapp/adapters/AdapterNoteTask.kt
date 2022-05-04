package com.example.timereminderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timereminderapp.R
import com.example.timereminderapp.data.NoteTask

class AdapterNoteTask(
    private val onItemListener: AdapterListeners.OnItemListener,
    private val onItemLongListener: AdapterListeners.OnItemLongListener
    ):RecyclerView.Adapter<AdapterNoteTask.TaskHolder>() {

    val listNoteTasks = mutableListOf<NoteTask>()

    fun setData(list: List<NoteTask>){
        listNoteTasks.clear()
        listNoteTasks.addAll(list)
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, noteTask: NoteTask){
        listNoteTasks.removeAt(position)
        listNoteTasks.add(position,noteTask)
        notifyItemChanged(position)
    }

    inner class TaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvNameTask:TextView = itemView.findViewById(R.id.tvNameTask)
        val tvTime:TextView = itemView.findViewById(R.id.tvTime)
        val imgStatus:ImageView = itemView.findViewById(R.id.imgStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskHolder {
        return TaskHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task_today,parent,false))
    }

    override fun onViewAttachedToWindow(holder: TaskHolder) {
        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            if (RecyclerView.NO_POSITION != pos){
                onItemListener.onItemClick(pos)
            }
        }

        holder.itemView.setOnLongClickListener {
            val pos = holder.adapterPosition
            if (RecyclerView.NO_POSITION != pos){
                onItemLongListener.onItemClick(pos)
            }
            true
        }

        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: TaskHolder) {
        holder.itemView.setOnClickListener(null)
        holder.itemView.setOnLongClickListener(null)
        super.onViewDetachedFromWindow(holder)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val noteTask = listNoteTasks[position]
        holder.apply {
            when(noteTask.status){
                0 -> {imgStatus.visibility = View.GONE}
                1->{imgStatus.setImageResource(R.drawable.verified_symbol)}
                2->{imgStatus.setImageResource(R.drawable.error_ico)}
            }
            tvNameTask.text = noteTask.name
            tvTime.text = noteTask.time
        }
    }

    override fun getItemCount(): Int {
        return listNoteTasks.size
    }

}