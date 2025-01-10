package com.example.chatapp.Repositories

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.transition.TransitionSet
import com.example.chatapp.Models.Message
import com.example.chatapp.Models.MessageType
import com.example.chatapp.Models.OneToOneChat
import com.example.chatapp.Models.User
import com.example.chatapp.Utilities.AttachmentUtils
import com.example.chatapp.Utilities.AuthUtils
import com.example.chatapp.Utilities.CloudinaryHelper
import com.example.chatapp.Utilities.FirebaseService
import com.google.common.util.concurrent.Atomics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Chats_Repository @Inject constructor(){
    private val database = FirebaseService.firebaseDatabase

    private suspend fun ensureChatExists(user1Id:String,user2Id:String):String{
        val chatId = if(user1Id<user2Id) "${user1Id}_${user2Id}" else "${user2Id}_${user1Id}"
        val chatRef = database.reference.child("chats").child(chatId)

        val chatSnapshot = chatRef.get().await()
        if(!chatSnapshot.exists()){
            val chat = OneToOneChat(
                chatId = chatId,
                user1Id = user1Id,
                user2Id = user2Id,
                lastUpdatedOn = System.currentTimeMillis()

            )
            chatRef.setValue(chat).await()
        }
        return chatId
    }

    private fun addChatToUser(userId:String,chatId:String,onComplete: (Boolean) -> Unit){
        val userChatRef = database.reference.child("users/$userId").child("chats")
        userChatRef.runTransaction(object:Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentList = currentData.getValue<List<String>>() ?: emptyList()

                if (currentList.contains(chatId)) {
                    // If it exists, don't update
                    return Transaction.success(currentData)
                }

                val updatedList = currentList + chatId
                currentData.value = updatedList
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {

                onComplete(error==null && committed)
            }
        })
    }

    suspend fun sendMessage(context: Context,user1Id: String, user2Id: String, messageContent:String){
        val chatId = ensureChatExists(user1Id,user2Id)
        val messageId = database.reference.child("Messages").push().key?:return

        val message = Message(messageId,user1Id,messageContent,System.currentTimeMillis())
        saveMessageToDatabase(messageId,message,chatId,user1Id,user2Id,)



    }

    fun uploadFileWithProgress(user1Id: String,user2Id: String,context: Context,filePath:String,fileType: String,onProgress:(Int)->Unit,onSuccess:(String)->Unit,onError:(String)->Unit):String{
        val temporaryId = "${user1Id}_${user2Id}_${System.currentTimeMillis()}"


        if(filePath.isNotEmpty() && File(filePath).exists()){
            CloudinaryHelper.uploadFile(
                filePath = filePath,
                fileType = fileType,
                onStart = {

                },
                onProgress = {progress->
                    onProgress(progress)
                },
                onSuccess = {url->
                    onSuccess(url)
                },
                onError = {error->
                    onError(error)
                }
            )
        }

        return temporaryId
    }

    suspend fun sendMessageWithMedia(context: Context,user1Id: String,user2Id: String,content:String,mediaUrl:String,fileType:MessageType):String{
        val chatId = ensureChatExists(user1Id,user2Id)
        val messageId = database.reference.child("Messages").push().key?:return ""
//        val messageType:MessageType = getMessageType(fileType)
        val message = Message(messageId,user1Id,content,System.currentTimeMillis(),fileType,mediaUrl)
        Log.d("vm media send","$message")
        saveMessageToDatabase(messageId,message,chatId,user1Id,user2Id)

        return messageId

    }

    private fun getMessageType(fileType:String):MessageType{
        return when(fileType){
            "image"->{
                MessageType.IMAGE
            }
            else ->return MessageType.TEXT
        }
    }

    private suspend fun saveMessageToDatabase(messageId:String,message: Message,chatId: String,user1Id: String,user2Id: String){
        database.reference.child("Messages/$messageId").setValue(message).addOnCompleteListener { task->
            if(task.isSuccessful){
                val chatRef = database.reference.child("chats/${chatId}/messageIds")
                chatRef.runTransaction(object:Transaction.Handler{
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        val currentList = currentData.getValue<List<String>>()?: emptyList()
                        val updatedList = currentList + messageId
                        currentData.value = updatedList
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if(error!=null){

                            return
                        }

                        database.reference.child("chats/${chatId}").child("lastMessageId").setValue(messageId)
                        database.reference.child("chats/${chatId}").child("lastUpdatedOn").setValue(System.currentTimeMillis())

                        addChatToUser(user1Id,chatId){success1->
                            if(!success1){

                                return@addChatToUser
                            }

                            addChatToUser(user2Id,chatId){success2->
                                if(!success2){

                                    return@addChatToUser
                                }

                            }
                        }



                    }



                })
            }else{
                Log.d("save failed","failed saving to database")
            }
        }
    }



     fun fetchChats(userId: String,onResult:(List<OneToOneChat>)->Unit){
        val ref = database.reference.child("users/$userId/chats")
        ref.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        val chatIds =
                            snapshot.children.mapNotNull { it.getValue(String::class.java) }
                        if (chatIds.isNotEmpty()) {
                            fetchChatsById(chatIds, onResult)
                        } else {
                            onResult(emptyList())
                        }
                    } else {
                        onResult(emptyList())
                    }
                }catch(e:Exception){
                    Log.d("Chat Fetch Error","${e.message}")
                    onResult(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }

        })


    }

    private fun fetchChatsById(chatIds: List<String>,onResult: (List<OneToOneChat>) -> Unit) {
        val remainingChats = AtomicInteger(chatIds.size)
        Log.d("yo","${chatIds.size}")
        val chats = mutableListOf<OneToOneChat>()
        chatIds.forEach{id->
            Log.d("error fetching","Trying to access $id")
            database.reference.child("chats/$id").get().addOnSuccessListener { snapshot->
                Log.d("success fetching","yeah")
                snapshot.getValue(OneToOneChat::class.java)?.let{chats.add(it)}
            }.addOnCompleteListener{
                Log.d("completed fetching","wooh ${chats.size}" )
                if(remainingChats.decrementAndGet()==0) onResult(chats)
            }.addOnFailureListener{
                Log.d("failure fetching","${it.message}")
                onResult(emptyList())
            }
        }
    }


    fun fetchMessages(chatId: String,onResult: (List<Message>) -> Unit){
        val ref = database.reference.child("chats/$chatId/messageIds")
        ref.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot){
                if(snapshot.exists()){
                    val messageIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    fetchMessagesById(messageIds,onResult)
                }
                else{
                    onResult(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(emptyList())
            }

        })


    }

    private fun fetchMessagesById(messageIds: List<String>,onResult: (List<Message>) -> Unit) {
        val remainingMessages = AtomicInteger(messageIds.size)
        val messages = mutableListOf<Message>()
        messageIds.forEach{id->
            database.reference.child("Messages/$id").get().addOnSuccessListener { snapshot->
                snapshot.getValue(Message::class.java)?.let { messages.add(it) }
            }.addOnCompleteListener{
                if(remainingMessages.decrementAndGet()==0) onResult(messages)
            }.addOnFailureListener{
                onResult(emptyList())
            }
        }
    }


    fun deleteMessage(chatId: String,messageId: String){
        val chatRef = database.reference.child("chats/$chatId")
        chatRef.runTransaction(object :Transaction.Handler{
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentMessages = currentData.getValue<List<String>>()?: emptyList()
                val updatedList = currentMessages.filter { it!=messageId }
                currentData.value = updatedList
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if(error!=null){

                    return
                }

                val messageRef = database.reference.child("Messages/$messageId")
                messageRef.removeValue()
            }

        })


    }


    fun fetchUsersByIds(userIds:List<String>,onResult:(List<User>)->Unit){
        val users = mutableListOf<User>()
        val remainingUsers = AtomicInteger(userIds.size)
        userIds.forEach{id->
            database.reference.child("users/$id").get()
                .addOnSuccessListener { snapshot->
                    snapshot.getValue(User::class.java)?.let { users.add(it) }
                }.addOnCompleteListener{
                    if(remainingUsers.decrementAndGet()==0) onResult(users)
                }.addOnFailureListener{//review
                    onResult(emptyList())
                }
        }
    }



    fun fetchMessagesByIds(messageIds:List<String>,onResult:(List<Message>)->Unit){
        val messages = mutableListOf<Message>()
        val remainingMessages = AtomicInteger(messageIds.size)
        messageIds.forEach{id->
            database.reference.child("Messages/$id").get()
                .addOnSuccessListener { snapshot->
                    snapshot.getValue(Message::class.java)?.let { messages.add(it) }
                }.addOnCompleteListener{
                    if(remainingMessages.decrementAndGet()==0) onResult(messages)
                }.addOnFailureListener{
                    if(remainingMessages.decrementAndGet()==0) onResult(messages)
                }
        }
    }


}