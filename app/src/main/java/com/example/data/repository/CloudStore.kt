package com.example.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/** Typed maps keep existing UI models intact while allowing field-level security rules. */
internal class CloudStore(private val database: FirebaseFirestore) {
    private val moshi = Moshi.Builder().add(StringPairAdapter()).add(KotlinJsonAdapterFactory()).build()

    fun <T : Any> encode(value: T, type: Class<T>): Map<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return moshi.adapter(type).toJsonValue(value) as Map<String, Any?>
    }

    fun <T : Any> listen(path: String, type: Class<T>, onError: (Exception) -> Unit,
                        onData: (List<T>) -> Unit): ListenerRegistration =
        database.collection(path).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) onError(error)
            else if (snapshot != null && !snapshot.metadata.hasPendingWrites()) {
                try {
                    onData(snapshot.documents.mapNotNull { moshi.adapter(type).fromJsonValue(it.data) })
                } catch (exception: Exception) { onError(exception) }
            }
        }

    suspend fun <T : Any> save(path: String, value: T, type: Class<T>) {
        database.document(path).set(encode(value, type)).awaitResult()
    }

    suspend fun delete(path: String) { database.document(path).delete().awaitResult() }

    class StringPairAdapter {
        @ToJson fun toJson(value: Pair<String, String>): Map<String, String> =
            mapOf("first" to value.first, "second" to value.second)
        @FromJson fun fromJson(value: Map<String, String>): Pair<String, String> =
            value.getValue("first") to value.getValue("second")
    }
}
