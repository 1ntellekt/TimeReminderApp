package com.example.timereminderapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timereminderapp.R
import com.example.timereminderapp.adapters.listeners.AdapterClickListeners
import com.example.timereminderapp.databinding.ItemTaskBinding
import com.example.timereminderapp.models.NoteTask
import com.example.timereminderapp.utils.Frags
import org.w3c.dom.Text

class AdapterNoteTask(
    private val isToday:Boolean,
    private val nameFragment:String,
    private val onDoneListener:AdapterClickListeners.OnDone?,
    private val onEditListener:AdapterClickListeners.OnEdit?,
    private val onDelListener:AdapterClickListeners.OnDel?,
    private val onDelFavListener:AdapterClickListeners.OnDelFav?,
    private val onRefListener:AdapterClickListeners.OnRef?,
    private val onFileListener:AdapterClickListeners.OnFile?
):RecyclerView.Adapter<AdapterNoteTask.NoteTaskHolder>()
{

    var listNoteTask = mutableListOf<NoteTask>()

    fun setData(list:List<NoteTask>){
        listNoteTask.clear()
        listNoteTask.addAll(list)
        notifyDataSetChanged()
    }

    fun filterList(list:MutableList<NoteTask>){
        listNoteTask = list
        notifyDataSetChanged()
    }

    inner class NoteTaskHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val btnDone:ImageButton = itemView.findViewById(R.id.btnDone)
        val btnDel:ImageButton = itemView.findViewById(R.id.btnDel)
        val btnEdit:ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnEditFav:ImageButton = itemView.findViewById(R.id.btnEditFav)
        val btnRef:ImageButton = itemView.findViewById(R.id.btnRef)
        val btnDelFav:ImageButton = itemView.findViewById(R.id.btnDelFav)

        val imgStatus:ImageView = itemView.findViewById(R.id.imgStatus)
        val imgFav:ImageView = itemView.findViewById(R.id.imgFav)

        val tvNameTask:TextView = itemView.findViewById(R.id.tvNameTask)
        val tvDescTask:TextView = itemView.findViewById(R.id.tvDescTask)
        val tvTagTask:TextView = itemView.findViewById(R.id.tvTaskTag)
        val tvTimeTask:TextView = itemView.findViewById(R.id.tvTimeTask)

        val panelFile:LinearLayout = itemView.findViewById(R.id.panelFiles)
        val recyclerViewFiles:RecyclerView = itemView.findViewById(R.id.recyclerFilesView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteTaskHolder {
        return NoteTaskHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_task,parent,false))
    }

    override fun onBindViewHolder(holder: NoteTaskHolder, position: Int) {
        val noteTask = listNoteTask[position]

        holder.apply {

            when(nameFragment){
                Frags.FAVORITE.nfr-> {

                    btnDone.visibility = View.GONE
                    btnDel.visibility = View.GONE
                    btnEdit.visibility = View.GONE

                    imgFav.visibility = View.VISIBLE
                    btnDelFav.visibility = View.VISIBLE
                    btnEditFav.visibility = View.VISIBLE
                }

                Frags.DEFAULT.nfr -> {
                    btnDone.visibility = View.VISIBLE
                    btnRef.visibility = View.GONE
                    imgFav.visibility = View.GONE
                }

                Frags.REFRESH.nfr -> {
                    btnDone.visibility = View.GONE
                    btnRef.visibility = View.VISIBLE
                    imgFav.visibility = View.GONE
                }
            }

            when(noteTask.status){
                0 -> {
                    imgStatus.visibility = View.GONE
                }
                1 -> {
                    imgStatus.visibility = View.VISIBLE
                    imgStatus.setImageResource(R.drawable.done_symbol)
                }
                2 -> {
                    imgStatus.visibility = View.VISIBLE
                    imgStatus.setImageResource(R.drawable.not_done_symbol)
                }
            }

            tvNameTask.text = noteTask.name
            tvDescTask.text = noteTask.description
            tvTagTask.text = noteTask.tag

            if (isToday) {
                tvTimeTask.text = noteTask.time
            } else {
                tvTimeTask.text = "${noteTask.date} ${noteTask.time}"
            }

            if (noteTask.fileList.isEmpty()){
                panelFile.visibility = View.GONE
            }else {
                panelFile.visibility = View.VISIBLE
                if (onFileListener!=null){
                    recyclerViewFiles.setHasFixedSize(true)
                    val adapterFiles = AdapterFiles(noteTask,onFileListener)
                    recyclerViewFiles.adapter = adapterFiles
                    adapterFiles.setData(noteTask.fileList)
                }
            }
        }
    }

    override fun onViewAttachedToWindow(holder: NoteTaskHolder) {
        holder.apply {
            if (onDoneListener!=null){
                btnDone.setOnClickListener {
                    val pos = holder.adapterPosition
                    if(RecyclerView.NO_POSITION != pos){
                        onDoneListener.onDoneClick(pos)
                    }
                }
            }

            if (onDelFavListener!=null){
                btnDelFav.setOnClickListener {
                    val pos = holder.adapterPosition
                    if(RecyclerView.NO_POSITION != pos){
                        onDelFavListener.onDelFavClick(pos)
                    }
                }
            }

            if (onDelListener!=null){
                btnDel.setOnClickListener {
                    val pos = holder.adapterPosition
                    if(RecyclerView.NO_POSITION != pos){
                        onDelListener.onDelClick(pos)
                    }
                }
            }

            if (onEditListener!=null){
                btnEdit.setOnClickListener {
                    val pos = holder.adapterPosition
                    if(RecyclerView.NO_POSITION != pos){
                        onEditListener.onEditClick(pos)
                    }
                }
                if (nameFragment==Frags.FAVORITE.nfr){
                    btnEditFav.setOnClickListener {
                        val pos = holder.adapterPosition
                        if(RecyclerView.NO_POSITION != pos){
                            onEditListener.onEditClick(pos)
                        }
                    }
                }
            }

            if (onRefListener!=null){
                btnRef.setOnClickListener {
                    val pos = holder.adapterPosition
                    if(RecyclerView.NO_POSITION != pos){
                        onRefListener.onRefClick(pos)
                    }
                }
            }

        }
        super.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: NoteTaskHolder) {
        holder.apply {
            btnRef.setOnClickListener(null)
            btnDone.setOnClickListener(null)
            btnDel.setOnClickListener(null)
            btnEdit.setOnClickListener(null)
            btnDelFav.setOnClickListener(null)
        }
        super.onViewDetachedFromWindow(holder)
    }

    override fun getItemCount(): Int {
        return listNoteTask.size
    }

}