package com.example.chatapp.ViewModels


import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.Models.ChatUIState
import com.example.chatapp.Models.Message
import com.example.chatapp.Models.MessageType
import com.example.chatapp.Models.OneToOneChat
import com.example.chatapp.Models.User
import com.example.chatapp.MyApplication
import com.example.chatapp.Repositories.Chats_Repository
import com.example.chatapp.Utilities.AttachmentUtils
import com.example.chatapp.Utilities.AuthUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repository: Chats_Repository,
    ):ViewModel() {

//    private val _messages = MutableLiveData<List<Message>>()
//    val messages:LiveData<List<Message>> = _messages
        private val chatMessagesMap = mutableMapOf<String,MutableLiveData<List<Message>>>()
    private val fetchedMessagesMap = mutableMapOf<String,Boolean>()


    private val _chats = MutableLiveData<List<OneToOneChat>>()
    val chats:LiveData<List<OneToOneChat>> = _chats

    private val _operationSuccess = MutableLiveData<Boolean?>()
    val operationSuccess:LiveData<Boolean?> = _operationSuccess

    private val _chatUILists = MutableLiveData<ChatUIState?>()
    val chatUILists:LiveData<ChatUIState?> = _chatUILists

    private val _userDetails = MutableLiveData<Map<String,User>>()
    val userDetails:LiveData<Map<String,User>> = _userDetails

    private val _lastMessages = MutableLiveData<Map<String,Message>>()
    val lastMessages:LiveData<Map<String,Message>> = _lastMessages


    private val _uploadProgress = MutableLiveData<Map<String,Int>>()
    val uploadProgress: LiveData<Map<String,Int>> = _uploadProgress


    private var progressMap = mutableMapOf<String,Int>()
//    private val temporaryIdToMessageIdMap = mutableMapOf<String,String>()
    private var areChatsLoaded = false

    fun getMessagesLiveData(chatId: String): MutableLiveData<List<Message>> {
        return chatMessagesMap.getOrPut(chatId){
            MutableLiveData(emptyList())
        }
    }

    fun updateUploadProgress(temporaryId:String,progress:Int){
        Log.d("cvm xxo","setting $temporaryId yo $progress")
        progressMap[temporaryId] = progress
        _uploadProgress.postValue(progressMap.toMap())
    }

    fun getMessageType(fileType:String):MessageType{
        return when(fileType){
            "video"->MessageType.VIDEO
            "image"->MessageType.IMAGE
            "document"->MessageType.DOCUMENT
            "audio"->MessageType.AUDIO
            else->MessageType.TEXT
        }
    }

    fun sendMessage(context: Context,user1Id:String,user2Id: String,content:String,mediaUri: Uri?=null,temporaryId: String="",chatId: String){
        _operationSuccess.postValue(null)
Log.d("sending xxxo","initiated")
        Log.d("check vm 2","$mediaUri")
        MyApplication.AppCoroutineScope.scope.launch {
            if(mediaUri!=null ){

                   val filePath = AuthUtils.getRealPathFromURI(context,mediaUri)
                   val fileType = AttachmentUtils.getFileTypeFromUri(mediaUri,filePath,context)
                    val messageType = getMessageType(fileType)
                Log.d("check vm","$mediaUri and $fileType and $fileType")
                // Generate temporary ID if not provided
                val actualTemporaryId = if (temporaryId.isEmpty()) {
                    "${user1Id}_${user2Id}_${System.currentTimeMillis()}"
                } else {
                    temporaryId
                }
                Log.d("sending xxxo"," media initiated for $actualTemporaryId")

                val placeholderMessage  = repository.sendMessageWithMedia(context,user1Id,user2Id,content,null,messageType,actualTemporaryId)
                Log.d("sending xxxo"," placeholder created")

                viewModelScope.launch(Dispatchers.Main) {
                    val liveData = chatMessagesMap[chatId]
                    if(liveData!=null){
                        val current = liveData.value?: emptyList()
                        val newList = current + placeholderMessage
                        liveData.value = newList
                        val updatedMessagesMap = _chatUILists.value?.messagesMap?.toMutableMap()?.apply {
                            // Ensure you're updating the map correctly
                            this[placeholderMessage.messageId] = placeholderMessage
                        }

                        val updatedChatList = _chatUILists.value?.chatsList?.map { chat ->
                            if (chat.chatId == chatId) {
                                chat.copy(
                                    lastMessageId = placeholderMessage.messageId,
                                    lastUpdatedOn = System.currentTimeMillis(),
                                    messageIds = chat.messageIds.apply {
                                        if (!contains(placeholderMessage.messageId)) {
                                            add(placeholderMessage.messageId)
                                        }
                                    }
                                )
                            } else {
                                chat
                            }
                        } ?: emptyList()

                        val newChatUIState = _chatUILists.value?.copy(
                            messagesMap = updatedMessagesMap ?: emptyMap(),
                            chatsList = updatedChatList
                        )


                        _chatUILists.postValue(newChatUIState)

                    }
                }

                repository.uploadFileWithProgress(user1Id,user2Id,context,filePath!!,fileType,

                                onProgress = {progress->
                         Log.d("progress on upload","$progress")
                        updateUploadProgress(actualTemporaryId,progress)
                        repository.updateMessageUploadProgress(actualTemporaryId,progress)
                    },
                    onSuccess = {url->
                        CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.IO){
                                Log.d("sending xxxo 2","successfully uploaded now redoing the message")
                                val finalMessage  = repository.sendMessageWithMedia(context,user1Id,user2Id,content,url,messageType,actualTemporaryId)
                                viewModelScope.launch(Dispatchers.Main){
                                    val liveData = chatMessagesMap[chatId]
                                    if(liveData!=null){
                                        val current = liveData.value?: emptyList()
                                        val messageIndex = current.indexOfFirst { it.messageId == actualTemporaryId }
                                        if(messageIndex!=-1){
                                            val newList = current.toMutableList()
                                            newList[messageIndex] = finalMessage
                                            Log.d("cvm xxo","${newList[messageIndex]}")
                                            liveData.value = newList
                                            chatMessagesMap[chatId] = liveData
                                            val updatedMessageMap =  _chatUILists.value?.messagesMap?.toMutableMap()?.apply  {
                                                this[finalMessage.messageId] = finalMessage
                                            }
                                            val updatedChatList = _chatUILists.value?.chatsList?.map { chat ->
                                                if (chat.chatId == chatId) {
                                                    chat.copy(
                                                        lastMessageId = finalMessage.messageId,
                                                        lastUpdatedOn = System.currentTimeMillis(),

                                                    )
                                                } else {
                                                    chat
                                                }
                                            } ?: emptyList()


                                            val newChatUIState = _chatUILists.value?.copy(
                                                messagesMap = updatedMessageMap ?: emptyMap(),
                                                chatsList = updatedChatList
                                            )

                                            // Post the new state to trigger the observer
                                            _chatUILists.postValue(newChatUIState)




                                        }


                                    }
                                }
                                progressMap.remove(actualTemporaryId)



                            }
                        }



                    },
                       onError = {

                       }
                )
            }else {
                val normalMessage= repository.sendMessage(context, user1Id, user2Id, content)
                val updatedMessageMap =  _chatUILists.value?.messagesMap?.toMutableMap()?.apply  {
                    this[normalMessage!!.messageId] = normalMessage

                }
                val updatedChatList = _chatUILists.value?.chatsList?.map { chat ->
                    if (chat.chatId == chatId) {
                        chat.copy(
                            lastMessageId = normalMessage!!.messageId,
                            lastUpdatedOn = System.currentTimeMillis(),

                            )
                    } else {
                        chat
                    }
                } ?: emptyList()


                val newChatUIState = _chatUILists.value?.copy(
                    messagesMap = updatedMessageMap ?: emptyMap(),
                    chatsList = updatedChatList
                )

                // Post the new state to trigger the observer
                _chatUILists.postValue(newChatUIState)



            }
        }

    }

//    private fun sendMessageWithMedia(
//        context: Context,
//        user1Id: String,
//        user2Id: String,
//        content: String,
//        mediaUrl: String,
//        messageType: MessageType
//    ): String {
////        val message = Message(
////            senderId = user1Id,
////            content = content,
////            messageType = messageType,
////            mediaUrl = mediaUrl,
////            timestamp = System.currentTimeMillis(),
////            isUploading = false
////        )
//        var id = "";
//        CoroutineScope(Dispatchers.IO).launch {
//            id = withContext(Dispatchers.IO) {
//                repository.sendMessageWithMedia(
//                    context,
//                    user1Id,
//                    user2Id,
//                    content,
//                    mediaUrl,
//                    messageType
//                )
//            }
//        }
//        return id
//    }


//    fun progressById(messageId: String): Int? {
//        val temporaryId = temporaryIdToMessageIdMap.entries.firstOrNull{it.value == messageId}?.key
//        return temporaryId.let { progressMap[it] }?:0
//    }


//    fun fetchMessages(chatId:String){
//        Log.d("Fetching messages","$chatId")
//        if(areMessagesLoaded && _messages.value!=null){
//            _messages.value = _messages.value
//        }
//        repository.fetchMessages(chatId){messages->
//            Log.d("got messages","${messages.size}")
//            _messages.postValue(messages)
//            areMessagesLoaded=true
//        }
//    }

    fun fetchMessages(chatId: String){
        if(fetchedMessagesMap[chatId] == true){
            return
        }
        Log.d("fetching messages xxxo","fetching messages for $chatId")
        repository.fetchMessages(chatId){messages->

            val liveData = chatMessagesMap.getOrPut(chatId){
                MutableLiveData(emptyList())
            }
            liveData.postValue(messages)
            fetchedMessagesMap[chatId]=true
        }

    }

    fun fetchChats(userId: String){
        if(areChatsLoaded && _chatUILists.value !=null) {
            _chatUILists.value=_chatUILists.value
            return
        }

        Log.d("chat fetch xxxo","fetching")

        repository.fetchChats(userId){chats->
            _chats.postValue(chats)
            val otherUserIds = chats.map {
                if (it.user1Id == userId) it.user2Id else it.user1Id // Get the other user in the chat
            }

            val lastMessageIds = chats.mapNotNull{it.lastMessageId}

            fetchUsersAndMessages(otherUserIds,lastMessageIds)
            areChatsLoaded = true
        }
    }

    private fun fetchUsersAndMessages(userIds: List<String>, lastMessageIds: List<String>) {
        Log.d("chats fetched","${userIds.size},${lastMessageIds.size}")
        repository.fetchUsersByIds(userIds){users->
            Log.d("users fetched","${users.size}")
            repository.fetchMessagesByIds(lastMessageIds){messages->
                _chatUILists.value = ChatUIState(
                    chatsList = _chats.value?: emptyList(),
                    messagesMap = messages.associateBy { it.messageId },
                    usersMap = users.associateBy { it.id }

                )
                Log.d("users and chats fetched","${users},${messages}")
            }
        }
    }

    fun deleteMessage(chatId: String,messageId:String){
        _operationSuccess.postValue(null)
        repository.deleteMessage(chatId,messageId)
    }




    fun loadUserDetails(userIds:List<String>){
        repository.fetchUsersByIds(userIds){users->
            _userDetails.postValue(users.associateBy { it.id })
        }
    }

    fun loadMessagesByIds(messageIds:List<String>){
        repository.fetchMessagesByIds(messageIds){messages->
            _lastMessages.postValue(messages.associateBy { it.messageId })
        }
    }
}