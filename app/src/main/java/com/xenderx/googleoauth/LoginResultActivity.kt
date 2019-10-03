package com.xenderx.googleoauth

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import de.hdodenhof.circleimageview.CircleImageView

class LoginResultActivity : AppCompatActivity() {

    private lateinit var loginType: LoginTypes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_result)

        val profileImage = findViewById<CircleImageView>(R.id.profile_image)
        val name = findViewById<TextView>(R.id.name)
        val email = findViewById<TextView>(R.id.email)

        val bundle = intent.extras

        loginType = bundle?.getSerializable("loginType") as LoginTypes

        name.text = bundle?.getString("name")
        email.text = bundle?.getString("email")

        val uriString = bundle?.getString("imageUri")
//        val imageUri = Uri.parse(uriString)

        Glide.with(this)
            .load(uriString)
            .placeholder(R.drawable.ic_account_circle_24dp)
            .centerCrop()
            .into(profileImage)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.sign_out -> {
                signOut()
                return true
            }
        }

        return false
    }

    private fun signOut() {
        if (loginType == LoginTypes.GOOGLE_LOGIN) {
            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
            googleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    Toast.makeText(this, "Sign out successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                }
        } else if (loginType == LoginTypes.FACEBOOK_LOGIN) {
            LoginManager.getInstance().logOut()
            Toast.makeText(this, "Sign out successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
