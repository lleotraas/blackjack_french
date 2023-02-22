package fr.lleotraas.blackjack_french

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fr.lleotraas.blackjack_french.features_offline_main_screen.presentation.MainScreenActivity
import fr.lleotraas.blackjack_french.features_online_main_screen.presentation.OnlineMainScreenActivity
import fr.lleotraas.blackjack_french.features_offline_game.domain.utils.Utils

class FacebookSignInActivity: MainScreenActivity() {

    private lateinit var callbackManager: CallbackManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
        auth = Firebase.auth

        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult) {
                Log.d(TAG, "Facebook: onSuccess: $result")
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "Facebook: onCancel")
            }


            override fun onError(error: FacebookException) {
                Log.d(TAG, "Facebook: onError", error)
            }
        })
    }

//    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val intent = result.data
//            callbackManager.onActivityResult(0, result.resultCode ,intent)
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        Log.d(TAG, "handleFacebookAccessToken: $accessToken")

        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signInInWithCredential: success")
                val user = auth.currentUser
                updateUI(user!!.uid)
            } else {
                Log.w(TAG, "signInInWithCredential: failure", task.exception)
                Toast.makeText(this, this.resources.getString(R.string.facebook_sigin_in_activity_failure), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(userId: String) {
        val onlineGameIntent = Intent(this, OnlineMainScreenActivity::class.java)
        onlineGameIntent.putExtra(Utils.CURRENT_USER_ID, userId)
        startActivity(onlineGameIntent)
    }
}