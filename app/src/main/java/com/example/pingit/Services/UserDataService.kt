package com.example.pingit.Services

object UserDataService {
    var id = ""
    var avatarColor = ""
    var avatarName = ""
    var email= ""
    var name = ""

    fun returnAvatarColor(components: String) : Int{

        val strippedColor = components
            .replace("[", "")
    }
}