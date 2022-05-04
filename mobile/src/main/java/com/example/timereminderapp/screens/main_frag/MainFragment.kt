package com.example.timereminderapp.screens.main_frag

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.timereminderapp.R
import com.example.timereminderapp.databinding.MainFragmentBinding
import com.example.timereminderapp.utils.APP_ACTIVITY

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding:MainFragmentBinding

    private lateinit var mNavController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = MainFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mNavController = (childFragmentManager.findFragmentById(R.id.fragmentHostNavMenu) as NavHostFragment).navController
        NavigationUI.setupWithNavController(binding.bottomNavMenu,mNavController)
        init()
        binding.btnSettings.setOnClickListener {
            APP_ACTIVITY.mNavController.navigate(R.id.action_mainFragment_to_settingsTimeFragment)
        }
    }

    private fun init(){
        viewModel.getData()

    }


}