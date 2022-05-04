package com.example.timereminderapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.timereminderapp.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : FragmentActivity(), GoogleApiClient.OnConnectionFailedListener {

    private var binding: ActivityMainBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.butt.setOnClickListener {
            authGoogle()
        }

        if (FirebaseAuth.getInstance().currentUser!=null){
            finish()
            startActivity(Intent(this@MainActivity,TodayActivity::class.java))
        }

    }

    private lateinit var googleApiClient:GoogleApiClient


    private fun authGoogle() {
        googleApiClient = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()
            .let { signInConfigBuilder ->
                // Build a GoogleApiClient with access to the Google Sign-In API and the
                // options specified in the sign-in configuration.
                GoogleApiClient.Builder(this)
                    .enableAutoManage(this,this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, signInConfigBuilder)
                    .build()
            }
        Auth.GoogleSignInApi.getSignInIntent(googleApiClient).also { signInIntent ->
            startActivityForResult(signInIntent, 123)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123) {
            Auth.GoogleSignInApi.getSignInResultFromIntent(data)?.apply {
                if (isSuccess) {
                    // Get account information
                    val credential = GoogleAuthProvider.getCredential(signInAccount?.idToken,null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnSuccessListener {
                            //Toast.makeText(this@MainActivity,"You sign successfully!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@MainActivity,TodayActivity::class.java))
                        }
                        .addOnFailureListener {
                           // Toast.makeText(this@MainActivity,it.message.toString(), Toast.LENGTH_SHORT).show()
                            Log.e("tag", "error sign up with : ${it.message.toString()}")
                        }
                }
            }
        }
    }

/*    override fun onPause() {
        super.onPause()
        googleApiClient.stopAutoManage(this);
        googleApiClient.disconnect();
    }*/



    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this,p0.errorMessage, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}