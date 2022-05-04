package com.example.timereminderapp.screens.today

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.FileUtils
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import com.example.timereminderapp.R
import com.example.timereminderapp.adapters.AdapterFiles
import com.example.timereminderapp.adapters.AdapterNoteTask
import com.example.timereminderapp.databinding.DialogTaskAllBinding
import com.example.timereminderapp.databinding.TodayFragmentBinding
import com.example.timereminderapp.models.CurrentUser
import com.example.timereminderapp.models.NoteTask
import com.example.timereminderapp.utils.*
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.File
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class TodayFragment : Fragment() {

    companion object {
        fun newInstance() = TodayFragment()
    }

    private lateinit var binding:TodayFragmentBinding

    private lateinit var viewModel: TodayViewModel

    private lateinit var mObserver: Observer<List<NoteTask>>

    private lateinit var adapterNoteTask: AdapterNoteTask

    private lateinit var todayDate:String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TodayFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        init()
    }

    private fun init() {

        viewModel = ViewModelProvider(this).get(TodayViewModel::class.java)

        adapterNoteTask = AdapterNoteTask(
            true,
            Frags.DEFAULT.nfr,
            {pos->
                //Done
                val noteTask = adapterNoteTask.listNoteTask[pos]
                viewModel.doneTask(noteTask.copy(status = 1)){
                    //showToast("Done task ${noteTask.name}")
                }
            },{pos->
                //Edit
                taskDialog(adapterNoteTask.listNoteTask[pos])
            },{pos->
                //Delete
                val noteTask = adapterNoteTask.listNoteTask[pos]
                viewModel.deleteTask(noteTask){
                   // showToast("Deleted ${noteTask.name}")
                    noteTask.fileList.forEach {
                        viewModel.deleteFile(noteTaskId = noteTask.id, it)
                    }
                }
            },null,null,
            {parentTask,pos->
                //showToast("$parentId $pos")
                val filename = parentTask?.fileList!![pos]
                viewModel.downloadFile(parentTask.id,filename)
            }
        )

        todayDate = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(Date())

        binding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapterNoteTask
            tvHeadToday.text = todayDate
            btnAddTaskToday.setOnClickListener {
                taskDialog(null)
            }
        }

        mObserver = Observer { list ->
            adapterNoteTask.setData(list.sortedBy{ it.status })
        }

        viewModel.todayTasks.observe(this,mObserver)

    }

    var tvPickTime:TextView?=null

   private lateinit var adapterFiles:AdapterFiles

    private fun taskDialog(noteTask: NoteTask?) {
        val bindingDialog = DialogTaskAllBinding.inflate(LayoutInflater.from(APP_ACTIVITY))
        val alertDialogBuilder = AlertDialog.Builder(APP_ACTIVITY)
        alertDialogBuilder.setView(bindingDialog.root)

        listSelectedFiles.clear()

        bindingDialog.apply {
            panelDateSet.visibility = View.GONE
            panelTimeSet.visibility = View.VISIBLE

            tvPickTime = tvSetTime

            adapterFiles = AdapterFiles(null) { _,pos ->

                if (noteTask != null) {
                    if (noteTask.fileList.isNotEmpty()){
                        val filename = adapterFiles.listFile[pos]
                        viewModel.deleteFile(noteTask.id,filename)
                        noteTask.fileList.removeAt(pos)
                        adapterFiles.deleteItem(pos)
                    }
                }

                if (listSelectedFiles.isNotEmpty()&&listSelectedFiles.size>pos&&listSelectedFiles[pos].exists()){
                    listSelectedFiles[pos].delete()
                    adapterFiles.deleteItem(pos)
                }

            }

            if (noteTask!=null){
                pickedTime = noteTask.time
                tvPickTime!!.text = "Время установлено! "+pickedTime!!
                tvHeadDialog.text = "Изменение задачи на сегодня"
                edDescTask.setText(noteTask.description)
                edNameTask.setText(noteTask.name)
                edTagTask.setText(noteTask.tag)
                checkboxFav.isChecked = noteTask.isFavorite
                adapterFiles.setData(noteTask.fileList)
            }

            recyclerFilesView.setHasFixedSize(true)
            recyclerFilesView.adapter = adapterFiles

            btnSetTime.setOnClickListener {
                pickTime()
            }

            btnFiles.setOnClickListener {
                 takeFileOnResult.launch(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType("*/*"),"Select file"))
              //   takeFileOnResult.launch((Intent(Intent.ACTION_GET_CONTENT).setType("*/*")))
            }

            alertDialogBuilder.setPositiveButton("Ok")
            { dialog, which ->
                if (edDescTask.text.isNullOrEmpty() || edTagTask.text.isNullOrEmpty() || edNameTask.text.isNullOrEmpty()){
                    showToast("Edit input is null!")
                    clearSelectedFiles()
                } else if (pickedTime==null)  {
                    showToast("Time is null!")
                    clearSelectedFiles()
                } else {
                    if (noteTask==null){
                        val newTask = NoteTask(status = 0, name = edNameTask.text.toString(),
                            description = edDescTask.text.toString(), tag = edTagTask.text.toString(),
                            date = todayDate, time = pickedTime!!,
                            isFavorite = checkboxFav.isChecked,requestCode = getReqCodePrefs())

                        if (adapterFiles.listFile.isNotEmpty()){
                            newTask.fileList.clear()
                            newTask.fileList.addAll(adapterFiles.listFile)
                        }

                        viewModel.addNoteTask(newTask){ noteId->
                            //showToast("Added task!")
                            uploadSelectedFiles(noteId)
                        }
                    } else {

                        val isUpdateTime = pickedTime != noteTask.time

                        val newTask :NoteTask = if (isUpdateTime){
                            cancelAlarmFromTask(noteTask)
                            NoteTask(id = noteTask.id,status = 0, name = edNameTask.text.toString(),
                                description = edDescTask.text.toString(), tag = edTagTask.text.toString(),
                                date = todayDate, time = pickedTime!!,
                                isFavorite = checkboxFav.isChecked,requestCode = getReqCodePrefs())
                        } else {
                            NoteTask(id = noteTask.id,status = noteTask.status, name = edNameTask.text.toString(),
                                description = edDescTask.text.toString(), tag = edTagTask.text.toString(),
                                date = todayDate, time = noteTask.time,
                                isFavorite = checkboxFav.isChecked,requestCode = noteTask.requestCode)
                        }

                        if (adapterFiles.listFile.isNotEmpty()){
                            newTask.fileList.clear()
                            newTask.fileList.addAll(adapterFiles.listFile)
                        }

                        viewModel.editTask(isUpdateTime,newTask){
                            //showToast("updated task!")
                            uploadSelectedFiles(noteTask.id)
                        }
                    }
                }
            }
                .setNegativeButton("Cancel")
            { dialog, which ->
                clearSelectedFiles()
                dialog.dismiss()}
        }

        alertDialogBuilder.show()
    }

    private fun uploadSelectedFiles(noteTaskId:String){
        if (listSelectedFiles.isNotEmpty()){
            listSelectedFiles.forEach {
                viewModel.uploadFile(noteTaskId,file = it)
            }
            clearSelectedFiles()
        }
    }

    private fun clearSelectedFiles(){
        if(listSelectedFiles.isNotEmpty()){
            listSelectedFiles.forEach { it.delete() }
            listSelectedFiles.clear()
        }
    }

    private val listSelectedFiles = mutableListOf<File>()
    private val takeFileOnResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if(result.data!=null && result.resultCode==RESULT_OK){
            val uri = result.data!!.data!!
            //val fileUri = File(uri.!!)
            val filename = "${APP_ACTIVITY.filesDir.path}/${SimpleDateFormat(TIME_PATTERN,Locale.getDefault()).format(Date())}${uri.getName()}"
            val file = getFileFromResolver(filename,APP_ACTIVITY.contentResolver.openInputStream(uri))
            if (file!=null){
                listSelectedFiles.add(file)
                if(adapterFiles.listFile.isEmpty()){
                    adapterFiles.setData(listSelectedFiles.map{it.name})
                } else {
                    adapterFiles.updateList(listSelectedFiles.map{it.name})
                }
            }
        }
    }

    var pickedTime: String? = null
    private fun pickTime() {
        val materialTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(0).setMinute(0).setTitleText("Set time").build()
        materialTimePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = materialTimePicker.hour
            calendar[Calendar.MINUTE] = materialTimePicker.minute
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            pickedTime = SimpleDateFormat(TIME_PATTERN,Locale.getDefault()).format(calendar.time)
            tvPickTime!!.text = "Время установлено! "+pickedTime!!
            //showToast(pickedTime!!)
        }
        materialTimePicker.show(APP_ACTIVITY.supportFragmentManager,"timePicker")
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.todayTasks.removeObserver(mObserver)
    }

}