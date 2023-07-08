package com.KAPO.madstyles

import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class serverCommu {
    companion object {
        fun sendRequest(JSONobj: JSONObject, api: String, onResponse: (String) -> Unit, onFailure: (String) -> Unit) {
            val url = "https://8799-192-249-19-234.ngrok-free.app/${api}"
            val okHttpClient= OkHttpClient()
            val body= JSONobj.toString().toRequestBody("application/json".toMediaType())
            val req=okhttp3.Request.Builder().url(url).addHeader("ngrok-skip-browser-warning","true").post(body).build()
            okHttpClient.newCall(req).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onFailure(e.message.toString())
                }
                override fun onResponse(call: Call, response: okhttp3.Response) {
                    val result = response.body!!.string().removeSurrounding("\"")
                    onResponse(result)
                }
            })
        }
    }
}