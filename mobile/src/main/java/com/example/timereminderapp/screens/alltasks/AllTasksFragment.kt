package com.example.timereminderapp.screens.alltasks

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.timereminderapp.adapters.AdapterFiles
import com.example.timereminderapp.adapters.AdapterNoteTask
import com.example.timereminderapp.databinding.AllTasksFragmentBinding
import com.example.timereminderapp.databinding.DialogTaskAllBinding
import com.example.timereminderapp.models.NoteTask
import com.example.timereminderapp.utils.*
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AllTasksFragment : Fragment() {

    private lateinit var binding: AllTasksFragmentBinding

    companion object {
        fun newInstance() = AllTasksFragment()
    }

    private lateinit var viewModel: AllTasksViewModel

    private lateinit var mObserver:Observer<List<NoteTask>>

    private lateinit var adapterNoteTask: AdapterNoteTask


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = AllTasksFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private val allTasks = mutableListOf<NoteTask>()

    override fun onStart() {
        super.onStart()

        viewModel = ViewModelProvider(this).get(AllTasksViewModel::class.java)

        adapterNoteTask = AdapterNoteTask(false, Frags.DEFAULT.nfr,
            {pos->
              //done
                val noteTask = adapterNoteTask.listNoteTask[pos]
                viewModel.doneTask(noteTask.copy(status = 1)){
                    //showToast("Done task ${noteTask.name}")
                }
            },{pos->
              //edit
                taskDialog(adapterNoteTask.listNoteTask[pos])
            },{pos->
              //delete
                val noteTask = adapterNoteTask.listNoteTask[pos]
                viewModel.deleteTask(noteTask){
                    noteTask.fileList.forEach {
                        viewModel.deleteFileTask(noteTaskId = noteTask.id,it)
                    }
                    //showToast("Delete task ${noteTask.name}")
                }
            },null,null,
            { parentTask,pos->
                //download file
                val filename = parentTask!!.fileList[pos]
                viewModel.downloadFileTask(noteTaskId = parentTask.id,filename)
            })

        mObserver = Observer {
                allTasks.clear()
                allTasks.addAll(it)
                adapterNoteTask.setData(it)
        }

        binding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.adapter = adapterNoteTask

            btnAddTask.setOnClickListener {
                taskDialog(null)
            }

            btnSearch.setOnClickListener {

                val filterList = mutableListOf<NoteTask>()
                filterList.addAll(allTasks)

                var currL = filterList

                if (edNameTask.text.toString().isNotEmpty()&&!currL.isNullOrEmpty()){
                  currL = currL.filter { it.name == edNameTask.text.toString()} as MutableList<NoteTask>
                    adapterNoteTask.filterList(currL)
                }
                if (edTagTask.text.toString().isNotEmpty()&&!currL.isNullOrEmpty()){
                    currL = currL.filter{ it.tag == edTagTask.text.toString()} as MutableList<NoteTask>
                    adapterNoteTask.filterList(currL)
                }
                if (radioDone.isChecked&&!currL.isNullOrEmpty()){
                    currL = currL.filterNot{ it.status==0} as MutableList<NoteTask>
                    adapterNoteTask.filterList(currL)
                }
                if (radioNoDone.isChecked&&!currL.isNullOrEmpty()){
                    currL = currL.filter{ it.status==0} as MutableList<NoteTask>
                    adapterNoteTask.filterList(currL)
                }
                if (radNew.isChecked&&!currL.isNullOrEmpty()){
                    currL = currL.sortedBy{ it.date } as MutableList<NoteTask>
                    adapterNoteTask.filterList(currL)
                }
                if(radOld.isChecked&&!currL.isNullOrEmpty()){
                    currL = currL.sortedByDescending{ it.date } as MutableList<NoteTask>
                    adapterNoteTask.filterList(currL)
                }
                if(edNameTask.text.toString().isEmpty()&&edTagTask.text.toString().isEmpty()
                    &&!radioNoDone.isChecked&&!radioDone.isChecked&&!radOld.isChecked&&!radNew.isChecked){
                    adapterNoteTask.setData(allTasks)
                }
                edNameTask.text.clear()
                edTagTask.text.clear()
            }

        }

        viewModel.allTaskList.observe(this,mObserver)

    }

    private lateinit var adapterFiles:AdapterFiles
    private var tvTimeSet:TextView?=null
    private var tvDateSet:TextView?=null
    private fun taskDialog(noteTask: NoteTask?){
        val bindingDialog = DialogTaskAllBinding.inflate(LayoutInflater.from(APP_ACTIVITY))
        val alertDialog = AlertDialog.Builder(APP_ACTIVITY)
        alertDialog.setView(bindingDialog.root)

        listSelectedFiles.clear()

        bindingDialog.apply {
            panelTimeSet.visibility = View.VISIBLE
            panelDateSet.visibility = View.VISIBLE

            tvTimeSet = tvSetTime
            tvDateSet = tvSetDate

            btnSetTime.setOnClickListener {
                pickTimeDialog()
            }

            btnSetDate.setOnClickListener {
                pickDateDialog()
            }

            btnFiles.setOnClickListener {
                taskFiles.launch(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).setType("*/*"),"Select file"))
            }

            adapterFiles = AdapterFiles(null){_,pos->
                noteTask?.let { task->
                    if (task.fileList.isNotEmpty()){
                        val filename = task.fileList[pos]
                        viewModel.deleteFileTask(task.id,filename)
                        task.fileList.removeAt(pos)
                        adapterFiles.deleteItem(pos)
                    }
                }

                if (listSelectedFiles.isNotEmpty()&&listSelectedFiles.size>pos&&listSelectedFiles[pos].exists()){
                    listSelectedFiles[pos].delete()
                    adapterFiles.deleteItem(pos)
                }
            }

            recyclerFilesView.setHasFixedSize(true)
            recyclerFilesView.adapter = adapterFiles

            if (noteTask!=null){
                tvHeadDialog.text = "Изменение задачи"
                pickDate = noteTask.date
                pickTime = noteTask.time
                tvDateSet!!.text = "Дата установлена! "+pickDate!!
                tvTimeSet!!.text = "Время установлено! "+pickTime!!
                edTagTask.setText(noteTask.tag)
                edDescTask.setText(noteTask.description)
                edNameTask.setText(noteTask.name)
                checkboxFav.isChecked = noteTask.isFavorite
                adapterFiles.setData(noteTask.fileList)
            }

            alertDialog.setPositiveButton("Ok"){ dialog,which->
                if (edNameTask.text.isNullOrEmpty()||edTagTask.text.isNullOrEmpty()||edDescTask.text.isNullOrEmpty()){
                    showToast("Input is null")
                    clearSelectedFiles()
                } else if (pickTime.isNullOrEmpty()){
                    showToast("Time is null")
                    clearSelectedFiles()
                } else if (pickDate.isNullOrEmpty()){
                    showToast("Date is null")
                    clearSelectedFiles()
                } else {
                    if (noteTask==null){

                        val newTask = NoteTask(status = 0,name = edNameTask.text.toString(),
                            description = edDescTask.text.toString(),tag = edTagTask.text.toString(),
                            time = pickTime!!, date = pickDate!!,requestCode = getReqCodePrefs(),isFavorite = checkboxFav.isChecked)

                        if(adapterFiles.listFile.isNotEmpty()){
                            newTask.fileList.clear()
                            newTask.fileList.addAll(adapterFiles.listFile)
                        }

                        viewModel.addTask(newTask){
                            uploadSelectedFiles(it)
                        }

                    } else {
                        val  isUpdateTime = (noteTask.time != pickTime) || (noteTask.date != pickDate)
                        val newTask = if (isUpdateTime){
                            cancelAlarmFromTask(noteTask)
                            NoteTask(id = noteTask.id,status = 0,name = edNameTask.text.toString(),
                                description = edDescTask.text.toString(),tag = edTagTask.text.toString(),
                                time = pickTime!!, date = pickDate!!,requestCode = getReqCodePrefs(),isFavorite = checkboxFav.isChecked)
                        }
                        else NoteTask(id = noteTask.id,status = noteTask.status,name = edNameTask.text.toString(),
                            description = edDescTask.text.toString(),tag = edTagTask.text.toString(),
                            isFavorite = checkboxFav.isChecked,requestCode = noteTask.requestCode,time = noteTask.time, date = noteTask.date)

                        if (adapterFiles.listFile.isNotEmpty()){
                            newTask.fileList.clear()
                            newTask.fileList.addAll(adapterFiles.listFile)
                        }

                        viewModel.editTask(isUpdateTime,newTask){
                            uploadSelectedFiles(noteTask.id)
                        }

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
    private fun pickTimeDialog(){
        val materialTimePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12).setMinute(0).setTitleText("Select time").build()
        materialTimePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = materialTimePicker.hour
            calendar[Calendar.MINUTE] = materialTimePicker.minute
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
            pickTime = SimpleDateFormat(TIME_PATTERN,Locale.getDefault()).format(calendar.time)
            tvTimeSet!!.text = "Время установлено! "+pickTime!!
        }
        materialTimePicker.show(APP_ACTIVITY.supportFragmentManager,"pickerTime")
    }

    private var pickDate:String?=null
    private fun pickDateDialog(){
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTitleText("Select date").build()

        materialDatePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = it
            pickDate = SimpleDateFormat(DATE_PATTERN,Locale.getDefault()).format(calendar.time)
            tvDateSet!!.text = "Дата установлена! "+pickDate!!
        }
        materialDatePicker.show(APP_ACTIVITY.supportFragmentManager,"pickerDate")
    }

    private val listSelectedFiles = mutableListOf<File>()
    private val taskFiles = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if (result.resultCode==RESULT_OK){
            val uri = result.data!!.data!!
            val filename = "${APP_ACTIVITY.filesDir.path}/${SimpleDateFormat(TIME_PATTERN, Locale.getDefault()).format(Date())}${uri.getName()}"
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.allTaskList.removeObserver(mObserver)
    }

}