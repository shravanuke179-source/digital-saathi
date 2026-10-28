package com.example.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Firebase owns password storage, token refresh and persistent sessions. */
internal suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (continuation.isActive) {
            if (task.isSuccessful) continuation.resume(task.result)
            else continuation.resumeWithException(task.exception ?: IllegalStateException("Request cancelled"))
        }
    }
}

internal suspend fun FirebaseAuth.signInWithGoogle(context: Context) {
    val resource = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    check(resource != 0) { "Google sign-in is not configured. Install the updated google-services.json." }
    val option = GetSignInWithGoogleOption.Builder(context.getString(resource)).build()
    val credential = CredentialManager.create(context).getCredential(
        context,
        GetCredentialRequest.Builder().addCredentialOption(option).build()
    ).credential
    check(credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        "Google did not return a valid sign-in credential."
    }
    val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
    signInWithCredential(GoogleAuthProvider.getCredential(token, null)).awaitResult()
}
