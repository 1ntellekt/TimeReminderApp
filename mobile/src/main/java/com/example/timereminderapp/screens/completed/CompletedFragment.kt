package com.example.timereminderapp.screens.completed

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.timereminderapp.adapters.AdapterFiles
import com.example.timereminderapp.adapters.AdapterNoteTask
import com.example.timereminderapp.databinding.CompletedFragmentBinding
import com.example.timereminderapp.databinding.DialogTaskAllBinding
import com.example.timereminderapp.models.NoteTask
import com.example.timereminderapp.utils.*
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CompletedFragment : Fragment() {

    companion object {
        fun newInstance() = CompletedFragment()
    }

    private lateinit var viewModel: CompletedViewModel

    private lateinit var binding:CompletedFragmentBinding

    private lateinit var mObserver:Observer<List<NoteTask>>

    private lateinit var adapterNoteTask: AdapterNoteTask

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CompletedFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProvider(this).get(CompletedViewModel::class.java)

        adapterNoteTask = AdapterNoteTask(false, Frags.REFRESH.nfr,null,
        {pos->
            //edit
            taskDialog(adapterNoteTask.listNoteTask[pos])
        },
        {pos->
            //delete
            val noteTask = adapterNoteTask.listNoteTask[pos]
            viewModel.deleteTask(noteTask){
                noteTask.fileList.forEach {
                    viewModel.deleteFileTask(noteTaskId = noteTask.id,it)
                }
                //showToast("Delete task ${noteTask.name}")
            }
        },null,
        {pos->
            //refresh
            pickDateDialog(adapterNoteTask.listNoteTask[pos])
        },{ parentTask,pos->
          //download file
          val filename = parentTask!!.fileList[pos]
          viewModel.downloadFileTask(noteTaskId = parentTask.id,filename)
        })

        mObserver = Observer {
            adapterNoteTask.setData(it)
        }

        binding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapterNoteTask
        }

        viewModel.allCompletedTasks.observe(this,mObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.allCompletedTasks.removeObserver(mObserver)
    }

    private lateinit var adapterFiles: AdapterFiles
    private fun taskDialog(noteTask: NoteTask) {
        val bindingDialog = DialogTaskAllBinding.inflate(LayoutInflater.from(APP_ACTIVITY))
        val alertDialog = AlertDialog.Builder(APP_ACTIVITY)
        alertDialog.setView(bindingDialog.root)

        listSelectedFiles.clear()

        bindingDialog.apply {
            panelDateSet.visibility = View.GONE
            panelTimeSet.visibility = View.GONE

            tvHeadDialog.text = "Редактирование задачи"

            adapterFiles = AdapterFiles(null){_,pos->

                if (noteTask.fileList.isNotEmpty()){
                    val filename = noteTask.fileList[pos]
                    viewModel.deleteFileTask(noteTask.id,filename)
                    noteTask.fileList.removeAt(pos)
                    adapterFiles.deleteItem(pos)
                }
                if (listSelectedFiles.isNotEmpty()&&listSelectedFiles.size>pos&&listSelectedFiles[pos].exists()){
                    listSelectedFiles[pos].delete()
                    adapterFiles.deleteItem(pos)
                }
            }

            recyclerFilesView.setHasFixedSize(true)
            recyclerFilesView.adapter = adapterFiles

            adapterFiles.setData(noteTask.fileList)
            edTagTask.setText(noteTask.tag)
            edDescTask.setText(noteTask.description)
            edNameTask.setText(noteTask.name)
            checkboxFav.isChecked = noteTask.isFavorite

            btnFiles.setOnClickListener {
                taskFiles.launch(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType("*/*"),"Select file"))
            }


            alertDialog.setPositiveButton("Ok"){ dialog,which->
                if (edNameTask.text.isNullOrEmpty()||edTagTask.text.isNullOrEmpty()||edDescTask.text.isNullOrEmpty()){
                    showToast("Input is null")
                    clearSelectedFiles()
                } else {
                        val newTask = NoteTask(id = noteTask.id,status = noteTask.status,name = edNameTask.text.toString(),
                            description = edDescTask.text.toString(),tag = edTagTask.text.toString(),
                            isFavorite = checkboxFav.isChecked,requestCode = noteTask.requestCode)

                        if (adapterFiles.listFile.isNotEmpty()){
                            newTask.fileList.clear()
                            newTask.fileList.addAll(adapterFiles.listFile)
                        }
                        viewModel.editTask(false,newTask){
                            uploadSelectedFiles(noteTask.id)
                        }
                }
            }

            alertDialog.setNegativeButton("Cancel"){ dialog,which->
                clearSelectedFiles()
                dialog.dismiss()
            }

        }

        alertDialog.show()



        }

    private fun uploadSelectedFiles(id: String) {
        if (listSelectedFiles.isNotEmpty()){
            listSelectedFiles.forEach {
                viewModel.uploadFileTask(id,file = it)
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

    private var pickTime:String?=null
    private fun pickTimeDialog(noteTask: NoteTask){
        val materialTimePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12).setMinute(0).setTitleText("Select time").build()
        materialTimePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = materialTimePicker.hour
            calendar[Calendar.MINUTE] = materialTimePicker.minute
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            pickTime = SimpleDateFormat(TIME_PATTERN, Locale.getDefault()).format(calendar.time)

            if (pickDate.isNullOrEmpty()||pickTime.isNullOrEmpty()){
                showToast("Some date is null")
            } else {
                //cancelAlarmFromTask(noteTask)
                val newTask = noteTask.copy(status = 0,time = pickTime!!, date = pickDate!!,requestCode = getReqCodePrefs())
                viewModel.refreshTask(newTask){
                    //showToast("Is updated time for ${newTask.name}")
                }
            }

        }
        materialTimePicker.show(APP_ACTIVITY.supportFragmentManager,"pickerTime")
    }

    private var pickDate:String?=null
    private fun pickDateDialog(noteTask: NoteTask){
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTitleText("Select date").build()

        materialDatePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it
            pickDate = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(calendar.time)
            pickTimeDialog(noteTask)
        }
        materialDatePicker.show(APP_ACTIVITY.supportFragmentManager,"pickerDate")
    }

    private val listSelectedFiles = mutableListOf<File>()
    private val taskFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if (result.resultCode== Activity.RESULT_OK){
            val uri = result.data!!.data!!
            val filename = "${APP_ACTIVITY.filesDir.path}/${
                SimpleDateFormat(TIME_PATTERN, Locale.getDefault()).format(Date())}${uri.getName()}"
            val file = getFileFromResolver(filename, APP_ACTIVITY.contentResolver.openInputStream(uri))
            if (file!=null){
                listSelectedFiles.add(file)
                if (adapterFiles.listFile.isEmpty()){
                    adapterFiles.setData(listSelectedFiles.map{ it.name })
                } else {
                    adapterFiles.updateList(listSelectedFiles.map{ it.name })
                }
            }
        }
    }

}


