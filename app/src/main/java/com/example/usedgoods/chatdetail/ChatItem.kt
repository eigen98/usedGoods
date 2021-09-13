package com.example.usedgoods.chatdetail

data class ChatItem(
    val senderId : String,
    val message : String
){
    constructor() : this("","")
}
