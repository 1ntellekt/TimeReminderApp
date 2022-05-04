package com.example.timereminderapp.screens.profile

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.timereminderapp.R
import com.example.timereminderapp.databinding.ProfileFragmentBinding
import com.example.timereminderapp.models.CurrentUser
import com.example.timereminderapp.utils.APP_ACTIVITY
import com.example.timereminderapp.utils.isAuthUser
import com.example.timereminderapp.utils.showToast

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel

    private lateinit var binding:ProfileFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ProfileFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        binding.apply {

            edEmail.setText(CurrentUser.email)
            edFirstName.setText(CurrentUser.firstname)
            edLastName.setText(CurrentUser.lastname)
            edNick.setText(CurrentUser.nickname)
            tvNickname.text = CurrentUser.nickname

            btnExit.setOnClickListener {
                viewModel.singOut()
                APP_ACTIVITY.mNavController.navigate(R.id.action_mainFragment_to_signInFragment)
            }

            btnEditProfile.setOnClickListener {

                if (edEmail.text.isNullOrEmpty()||edFirstName.text.isNullOrEmpty()||edLastName.text.isNullOrEmpty()||edNick.text.isNullOrEmpty()){
                    showToast("Some input is null!")
                } else {

                    if (CurrentUser.password!=""){
                        if (edEmail.text.toString()!=CurrentUser.email){
                            viewModel.updateEmail(edEmail.text.toString(),CurrentUser.password){
                                isAuthUser(false)
                                APP_ACTIVITY.mNavController.navigate(R.id.action_mainFragment_to_signInFragment)
                            }
                        }
                    } else if (edEmail.text.toString()!=CurrentUser.email) {
                        showToast("Profile email is only signed by Google!")
                    }

                    CurrentUser.nickname = edNick.text.toString()
                    CurrentUser.firstname = edFirstName.text.toString()
                    CurrentUser.lastname = edLastName.text.toString()
                    viewModel.editProfile {
                        //showToast("Profile updated!")
                    }
                }

            }

        }

    }

}