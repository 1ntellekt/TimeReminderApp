package com.example.timereminderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timereminderapp.R
import com.example.timereminderapp.adapters.listeners.AdapterClickListeners
import com.example.timereminderapp.models.NoteTask

class AdapterFavorites(private val onItemListener:AdapterClickListeners.OnItemClick):RecyclerView.Adapter<AdapterFavorites.TaskFavorite>()
{
    val listFavoriteList = mutableListOf<NoteTask>()

    fun setData(list:List<NoteTask>){
        listFavoriteList.clear()
        listFavoriteList.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskFavorite(itemView: View) :RecyclerView.ViewHolder(itemView){
        val tvNameTask:TextView = itemView.findViewById(R.id.tvNameTask)
        val imgStatus:ImageView = itemView.findViewById(R.id.imgStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskFavorite {
        return TaskFavorite(LayoutInflater.from(parent.context).inflate(R.layout.item_fav_add_task,parent,false))
    }

    override fun onBindViewHolder(holder: TaskFavorite, position: Int) {
        val noteTask = listFavoriteList[position]
        holder.apply {
            tvNameTask.text = noteTask.name
            when(noteTask.status){
                0->{imgStatus.setImageResource(R.drawable.time_def)}
                1->{imgStatus.setImageResource(R.drawable.time_done)}
                2->{imgStatus.setImageResource(R.drawable.time_error)}
            }
        }
    }

    override fun onViewAttachedToWindow(holder: TaskFavorite) {
        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            if (RecyclerView.NO_POSITION != pos){
                onItemListener.onItemClick(pos)
            }
        }
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: TaskFavorite) {
        holder.itemView.setOnClickListener(null)
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int {
        return listFavoriteList.size
    }

}