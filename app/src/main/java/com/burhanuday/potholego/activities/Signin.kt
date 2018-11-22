package com.burhanuday.potholego.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import com.burhanuday.potholego.ApiClient
import com.burhanuday.potholego.ApiService
import com.burhanuday.potholego.R
import com.burhanuday.potholego.models.User
import com.burhanuday.potholego.utils.Constants
import kotlinx.android.synthetic.main.activity_signin.*
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.api.OptionalPendingResult
import com.google.android.gms.common.api.ResultCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * Created by Burhanuddin on 27-10-2018.
 */

class Signin : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    private var mGoogleApiClient: GoogleApiClient? = null
    val RC_SIGN_IN = 1000
    var mProgressDialog: ProgressDialog? = null
    lateinit var apiService: ApiService
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        supportActionBar!!.hide()

        sharedPreferences = getSharedPreferences("com.burhanuday.potholego", Context.MODE_PRIVATE)
        val token:String = sharedPreferences.getString("token", "")
        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constants.BASE_URL).build()

        apiService = retrofit.create(ApiService::class.java)

        /**
         * ask user for CAMERA, LOCATION and WRITE_EXTERNAL_STORAGE permissions
         */
        checkRequiredPermissions()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        btn_sign_in.setSize(SignInButton.SIZE_STANDARD)
        btn_sign_in.setScopes(gso.scopeArray)
        btn_sign_in.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this, "A network error has occured: " + p0.errorCode + p0.errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun checkRequiredPermissions() {
        if (ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE
                ), Constants.PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess) {
            showProgressDialog()
            val acct: GoogleSignInAccount = result.signInAccount!!
            val personName = acct.displayName
            val email = acct.email
            val call:Call<User> = apiService.authenticate(personName, email)
            Log.i("signin", "trying to auth")
            call.enqueue(object : Callback<User>{
                override fun onFailure(call: Call<User>, t: Throwable) {
                    Log.i("signin", "Error: " + t.message)
                    Log.i("signin", "failed")
                    hideProgressDialog()
                }

                override fun onResponse(call: Call<User>, response: Response<User>) {
                    Log.i("signin", "success")
                    hideProgressDialog()
                    val user: User? = response.body()
                    Log.i("signin", response.body().toString())
                    if (user?.status != 0){
                        updateUI(true)
                        sharedPreferences.edit().putString("token", user!!.token).apply()
                    }
                }
            })
        } else {
            updateUI(false)
        }
    }

    private fun updateUI(signedIn: Boolean) {
        Log.i("signin", "UpdateUI" + signedIn.toString())
        if (signedIn){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            handleSignInResult(result)
        }
    }

    public override fun onStart() {
        super.onStart()
        val opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient)
        if (opr.isDone) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d("Signin", "Got cached sign-in")
            val result = opr.get()
            handleSignInResult(result)
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog()
            opr.setResultCallback(object : ResultCallback<GoogleSignInResult> {
                override fun onResult(p0: GoogleSignInResult) {
                    hideProgressDialog()
                    handleSignInResult(p0)
                }

            })
        }
    }

    private fun showProgressDialog(){
        if (mProgressDialog == null){
            mProgressDialog = ProgressDialog.show(this, "Loading", "Getting log in details", true)
        }
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog(){
        if (mProgressDialog!=null && mProgressDialog!!.isShowing){
            mProgressDialog!!.hide()
        }
    }
}

