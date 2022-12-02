package com.example.pingit.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.pingit.Model.Channel
import com.example.pingit.Model.Message
import com.example.pingit.Utilities.URL_GET_CHANNELS
import com.example.pingit.controller.App
import org.json.JSONException

object MessageService {
    val channels = ArrayList<Channel>()
    val message = ArrayList<Message>()

    fun getChannel(complete: (Boolean) -> Unit){

        val channelRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener {response ->
        try {
            for (x in 0 until response.length()){
                val channel = response.getJSONObject(x)
                val name = channel.getString("name")
                val chanDesc = channel.getString("description")
                val channelId = channel.getString("id")
                val newChannel = Channel(name, chanDesc, channelId)
                this.channels.add(newChannel)
            }
            complete(true)
        }catch (e: JSONException){
            Log.d("JSON", "EXC:" + e.localizedMessage)
            complete(false)
        }
        }, Response.ErrorListener {error ->
            Log.d("ERROR", "Could not retrieve channels")
            complete(false)

        }){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.prefs.authToken}")
                return headers
            }
        }
        Volley.newRequestQueue(context).add(channelRequest)
    }
}