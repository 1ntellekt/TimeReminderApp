package com.example.timereminderapp.screens.signin

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.example.timereminderapp.R
import com.example.timereminderapp.databinding.SignInFragmentBinding
import com.example.timereminderapp.utils.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {

    companion object {
        fun newInstance() = SignInFragment()
    }

    private lateinit var viewModel: SignInViewModel
    private lateinit var binding: SignInFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SignInFragmentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SignInViewModel::class.java)
    }


    override fun onStart() {
        super.onStart()

        if (getIsAuthUser()){
            viewModel.initDatabase()
            APP_ACTIVITY.mNavController.navigate(R.id.action_signInFragment_to_mainFragment)
        }

        binding.apply {

            btnSignInEmail.setOnClickListener {
                if (edEmail.text.isNullOrEmpty()||edPassword.text.isNullOrEmpty())
                {
                    showToast("Input is null!")
                }else {
                    viewModel.signInEmail(edEmail.text.toString(),edPassword.text.toString()){
                        if(FirebaseAuth.getInstance().currentUser?.isEmailVerified==true){
                            //showToast("Email sign in successfully!")
                                isAuthUser(true)
                            APP_ACTIVITY.mNavController.navigate(R.id.action_signInFragment_to_mainFragment)
                        } else {
                            showToast("Email is not verified!")
                        }
                    }
                }
            }
            btnSignInGoogle.setOnClickListener {
                authGoogle()
            }
            tvToggle.setOnClickListener {
                APP_ACTIVITY.mNavController.navigate(R.id.action_signInFragment_to_signUpFragment)
            }
        }
    }

    private fun authGoogle() {
        getGoogleClient().signOut()
        val intent = getGoogleClient().signInIntent
        taskGoogleAcc.launch(intent)
    }


    private val taskGoogleAcc = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let { acc->
                viewModel.signInGoogle(acc.idToken!!){
                    //showToast("Google sign in successfully!")
                    isAuthUser(true)
                    APP_ACTIVITY.mNavController.navigate(R.id.action_signInFragment_to_mainFragment)
                }
            }
        }catch (e:ApiException){
            Log.e("tag","Google sign error:${e.message}")
        }
    }


}