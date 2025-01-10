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
class ChatsViewModel @Inject constructor(private val repository: Chats_Repository):ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages:LiveData<List<Message>> = _messages

    private val _chats = MutableLiveData<List<OneToOneChat>>()
    val chats:LiveData<List<OneToOneChat>> = _chats

    private val _operationSuccess = MutableLiveData<Boolean?>()
    val operationSuccess:LiveData<Boolean?> = _operationSuccess

    private val _chatUILists = MutableLiveData<ChatUIState>()
    val chatUILists:LiveData<ChatUIState> = _chatUILists

    private val _userDetails = MutableLiveData<Map<String,User>>()
    val userDetails:LiveData<Map<String,User>> = _userDetails

    private val _lastMessages = MutableLiveData<Map<String,Message>>()
    val lastMessages:LiveData<Map<String,Message>> = _lastMessages


    private val _uploadProgress = MutableLiveData<Map<String,Int>>()
    val uploadProgress: LiveData<Map<String,Int>> = _uploadProgress


    private val progressMap = mutableMapOf<String,Int>()
    private val temporaryIdToMessageIdMap = mutableMapOf<String,String>()

    fun updateUploadProgress(temporaryId:String,progress:Int){
        progressMap[temporaryId] = progress
        _uploadProgress.postValue(progressMap)
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

    fun sendMessage(context: Context,user1Id:String,user2Id: String,content:String,mediaUri: Uri?=null){
        _operationSuccess.postValue(null)
        var temporaryId = ""
        Log.d("check vm 2","$mediaUri")
        viewModelScope.launch {
            if(mediaUri!=null){
                   val filePath = AuthUtils.getRealPathFromURI(context,mediaUri)
                   val fileType = AttachmentUtils.getFileTypeFromUri(mediaUri,filePath,context)
                    val messageType = getMessageType(fileType)
                Log.d("check vm","$mediaUri and $fileType and $fileType")
                   temporaryId = repository.uploadFileWithProgress(user1Id,user2Id,context,filePath!!,fileType,
                    onProgress = {progress->
                         Log.d("progress on upload","$progress")
                        updateUploadProgress(temporaryId,progress)
                    },
                    onSuccess = {url->
//                        Log.d("success on upload","$temporaryId")
//                        withContext(Dispatchers.Main){
//                            Log.d("vm success","$temporaryId ")
//                            val messageId = repository.sendMessageWithMedia(context,user1Id,user2Id,content,url,messageType)
//                            temporaryIdToMessageIdMap[temporaryId]  = messageId
//                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                Log.d("trying vmscope","yeah")
                                val messageId = withContext(Dispatchers.IO) { // Use IO for network/database operations
                                    repository.sendMessageWithMedia(context, user1Id, user2Id, content, url, messageType)
                                }
                                temporaryIdToMessageIdMap[temporaryId] = messageId
                                Log.d("messageId success", "$messageId mapped to $temporaryId")
                            } catch (e: Exception) {
                                Log.e("sendMessageWithMedia error", "Exception: ${e.localizedMessage}", e)
                            }
                        }

                    },
                       onError = {

                       }
                )
            }else {
                repository.sendMessage(context, user1Id, user2Id, content)
            }
        }

    }


    fun progressById(messageId: String): Int? {
        val temporaryId = temporaryIdToMessageIdMap.entries.firstOrNull{it.value == messageId}?.key
        return temporaryId.let { progressMap[it] }?:0
    }


    fun fetchMessages(chatId:String){
        Log.d("Fetching messages","$chatId")
        repository.fetchMessages(chatId){messages->
            Log.d("got messages","${messages.size}")
            _messages.postValue(messages)
        }
    }

    fun fetchChats(userId: String){
        repository.fetchChats(userId){chats->
            _chats.postValue(chats)
            val otherUserIds = chats.map {
                if (it.user1Id == userId) it.user2Id else it.user1Id // Get the other user in the chat
            }

            val lastMessageIds = chats.mapNotNull{it.lastMessageId}

            fetchUsersAndMessages(otherUserIds,lastMessageIds)
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