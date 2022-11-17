package fr.lleotraas.blackjack_french.ui.activity.sign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fr.lleotraas.blackjack_french.R
import fr.lleotraas.blackjack_french.ui.activity.OnlineMainScreenActivity
import fr.lleotraas.blackjack_french.utils.Utils.Companion.CURRENT_USER_ID
import fr.lleotraas.blackjack_french.utils.Utils.Companion.REQUEST_CODE_SIGN_IN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleSignInActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var signInRequest: BeginSignInRequest
    private val reqOneTap = 2
    private var showOneTapUi = true
    private lateinit var oneTapClient: SignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = createSignInRequest()
        val options = createOptions()
        val signInClient = GoogleSignIn.getClient(this, options)
        createSignInIntent(signInClient)
//        createOneTapSignIn()
    }

    private fun createSignInIntent(signInClient: GoogleSignInClient) {
        signInClient.signInIntent.also {
            startActivityForResult(it, REQUEST_CODE_SIGN_IN)
        }
    }

    private fun createOneTapSignIn() {
        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener(this) { result ->
            try {
                Log.e(javaClass.simpleName, "createOneTapSignIn: intent for result")
                startIntentSenderForResult(
                    result.pendingIntent.intentSender, reqOneTap, null, 0, 0, 0, null
                )
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, "Couldn't start One Tap UI: ${e.localizedMessage}")
//                val options = createOptions()
//                val signInClient = GoogleSignIn.getClient(this, options)
//                createSignInIntent(signInClient)
            }
        }.addOnFailureListener(this) { e ->
            e.localizedMessage?.let {
                Log.e(javaClass.simpleName, it)
            }
        }
    }

    private fun createOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(this.resources.getString(R.string.webclient_id))
            .requestEmail()
            .build()
    }

    private fun createSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(this.resources.getString(R.string.webclient_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(this.resources.getString(R.string.webclient_id))
                    .setFilterByAuthorizedAccounts(true)
                    .build())
            .setAutoSelectEnabled(true)
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        oneTapSignInResult(requestCode, data)
        googleSignInResult(requestCode, data)
        Log.e(javaClass.simpleName, "onActivityResult: result received")
    }

    private fun googleSignInResult(requestCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SIGN_IN) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).result
            account?.let {
                googleAuthForFirebase(it)
            }
        }
    }

    private fun oneTapSignInResult(requestCode: Int, data: Intent?) {
       when (requestCode) {
            reqOneTap -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        signInWithCredential(firebaseCredential)
                        Log.e(javaClass.simpleName, "oneTapSignInResult: Got Id Token")
                    } else {
                        Log.e(javaClass.simpleName, "oneTapSignInResult: No ID token")
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
//                            val options = createOptions()
//                            val signInClient = GoogleSignIn.getClient(this, options)
//                            createSignInIntent(signInClient)
//                            googleSignInResult(requestCode, data)
                            Log.e(javaClass.simpleName, "oneTapSignInResult: One-tap dialog was closed.")
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Log.e(javaClass.simpleName, "oneTapSignInResult: One-tap encountered a network error.")
                        }
                        else -> {
                            Log.e(javaClass.simpleName, "oneTapSignInResult: Couldn't get credential from result ${e.localizedMessage}")
                        }
                    }
                }
            }
        }
    }

    private fun signInWithCredential(firebaseCredential: AuthCredential) {
        auth.signInWithCredential(firebaseCredential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.e(javaClass.simpleName, "signInWithCredential: SignIn success")
                val user = auth.currentUser
                updateUI(user?.uid)
            } else {
                Log.e(javaClass.simpleName, "signInWithCredential: SignIn failure", task.exception)
                updateUI(null)
            }
        }
    }

    private fun updateUI(userId: String?) {
        val onlineGameIntent = Intent(this, OnlineMainScreenActivity::class.java)
        onlineGameIntent.putExtra(CURRENT_USER_ID, userId)
        startActivity(onlineGameIntent)
    }

    private fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e(javaClass.simpleName, "googleAuthForFirebase: userId:${auth.currentUser?.uid}")
                        updateUI(auth.currentUser?.uid)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, applicationContext.resources.getString(R.string.main_screen_fragment_login_successfull) , Toast.LENGTH_SHORT).show()
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}