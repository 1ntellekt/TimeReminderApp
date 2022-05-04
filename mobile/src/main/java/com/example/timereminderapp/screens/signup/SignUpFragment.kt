package com.example.timereminderapp.screens.signup

import android.app.ProgressDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.example.timereminderapp.R
import com.example.timereminderapp.databinding.SignUpFragmentBinding
import com.example.timereminderapp.models.CurrentUser
import com.example.timereminderapp.utils.APP_ACTIVITY
import com.example.timereminderapp.utils.getGoogleClient
import com.example.timereminderapp.utils.isAuthUser
import com.example.timereminderapp.utils.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class SignUpFragment : Fragment() {

    companion object {
        fun newInstance() = SignUpFragment()
    }

    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: SignUpFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SignUpFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
    }


    override fun onStart() {
        super.onStart()
        binding.apply {

            btnSignUpGoogle.setOnClickListener {
                if (edFirstName.text.isNullOrEmpty()||edLastName.text.isNullOrEmpty()||edNick.text.isNullOrEmpty()){
                    showToast("Input is null!")
                }else{
                    CurrentUser.firstname = edFirstName.text.toString()
                    CurrentUser.lastname = edLastName.text.toString()
                    CurrentUser.nickname = edNick.text.toString()
                    authGoogle()
                }
            }

            btnSignUpEmail.setOnClickListener {
                if (edFirstName.text.isNullOrEmpty()||edLastName.text.isNullOrEmpty()||
                    edNick.text.isNullOrEmpty()||edEmail.text.isNullOrEmpty()||edPassword.text.isNullOrEmpty()){
                    showToast("Input is null!")
                }else{
                    CurrentUser.firstname = edFirstName.text.toString()
                    CurrentUser.lastname = edLastName.text.toString()
                    CurrentUser.nickname = edNick.text.toString()
                    CurrentUser.password = edPassword.text.toString()
                    val  progressDialog = ProgressDialog(APP_ACTIVITY)
                    progressDialog.setTitle("Ожидание сервера....")
                    progressDialog.show()
                    viewModel.signUpEmail(edEmail.text.toString(),edPassword.text.toString()){
                        //showToast("Email sign up successfully!")
                        //isAuthUser(true)
                        if (progressDialog.isShowing) progressDialog.dismiss()
                        APP_ACTIVITY.mNavController.navigate(R.id.action_signUpFragment_to_signInFragment)
                    }
                }
            }

            tvToggle.setOnClickListener {
                APP_ACTIVITY.mNavController.navigate(R.id.action_signUpFragment_to_signInFragment)
            }

        }
    }

    private fun authGoogle() {
        getGoogleClient().signOut()
        val intent = getGoogleClient().signInIntent
        taskAccount.launch(intent)
    }

    private val taskAccount = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let { acc->
                viewModel.signUpGoogle(acc.idToken!!){
                    //showToast("Google sign up successfully!")
                    isAuthUser(true)
                    APP_ACTIVITY.mNavController.navigate(R.id.action_signUpFragment_to_mainFragment)
                }
            }
        }catch (e:ApiException){
            Log.e("tag", "Google sign up error:${e.message}")
        }
    }


}