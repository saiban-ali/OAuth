package com.xenderx.googleoauth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import org.json.JSONException
import org.json.JSONObject
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val SIGN_IN_CODE : Int = 1001

    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var callbackManager : CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInGoogle = findViewById<SignInButton>(R.id.btn_sign_in)
        val signInFb = findViewById<LoginButton>(R.id.btn_sign_in_fb)

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        signInGoogle.setOnClickListener(this)

        callbackManager =  CallbackManager.Factory.create()

        signInFb.setPermissions(listOf("email", "public_profile"))
        signInFb.registerCallback(callbackManager, object: FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                val accessToken = result?.accessToken
                updateUI(accessToken)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

    override fun onStart() {
        super.onStart()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null)
            updateUI(account)

        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken != null)
            updateUI(accessToken)
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            val intent = Intent(this, LoginResultActivity::class.java)

            val bundle = Bundle()

            bundle.putString("name", account.displayName)
            bundle.putString("email", account.email)
            bundle.putString("imageUri", account.photoUrl.toString())
            bundle.putSerializable("loginType", LoginTypes.GOOGLE_LOGIN)

            intent.putExtras(bundle)

            startActivity(intent)
            finish()
        }
    }

    private fun updateUI(accessToken: AccessToken?) {

        val graphRequest = GraphRequest.newMeRequest(
            accessToken
        ) { `object`, response ->
            try {
                val intent = Intent(this, LoginResultActivity::class.java)

                val bundle = Bundle()

                bundle.putString("name", `object`?.getString("name"))
                bundle.putString("email", `object`?.getString("email"))
                bundle.putString("imageUri", `object`?.getJSONObject("picture")
                    ?.getJSONObject("data")
                    ?.getString("url"))
                bundle.putSerializable("loginType", LoginTypes.FACEBOOK_LOGIN)

                intent.putExtras(bundle)

                startActivity(intent)
                finish()

            } catch (e: JSONException) {
                Toast.makeText(this@MainActivity, "Error: " + e.message, Toast.LENGTH_SHORT).show()
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "id,name,email,picture.width(200)")
        graphRequest.parameters = parameters
        // Initiate the GraphRequest
        graphRequest.executeAsync()

    }


    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.btn_sign_in -> signIn()
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SIGN_IN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == SIGN_IN_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            val account = task?.getResult(ApiException::class.java)

            updateUI(account)
        } catch (e : ApiException) {
            Toast.makeText(this, "Sign in failed with: " + e.statusCode, Toast.LENGTH_SHORT).show()
        }
    }
}
