package com.pale_cosmos.helu

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.bassaer.chatmessageview.model.ChatUser
import com.github.bassaer.chatmessageview.model.Message
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import com.pale_cosmos.helu.util.myUtil
import kotlinx.android.synthetic.main.activity_talk.*
import kotlinx.android.synthetic.main.activity_u_chat.*
import java.io.File

class TalkActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var myFriend: Friends
    lateinit var me: ChatUser
    lateinit var you: ChatUser
    lateinit var yourIcon: Bitmap
    lateinit var dbr: DatabaseReference
    lateinit var str: StorageReference

    lateinit var myDB: DatabaseReference
    lateinit var yourDB: DatabaseReference
    lateinit var uid: String
    lateinit var myNick: String
    var areyou = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        myUtil.updateStatusBarColor(window, "#E43F3F")
        myNick = intent.getStringExtra("nickname")
        uid = intent.getStringExtra("key")
        myFriend = myUtil.popDataHolder(intent.getStringExtra("info")) as Friends
        you = ChatUser(
            1,
            myFriend.nickname,
            myUtil.stringToBitmap(myUtil.popDataHolder(intent.getStringExtra("image")) as String)
        )
        me = ChatUser(0, myNick, BitmapFactory.decodeResource(resources, R.drawable.face_2))
        myDB = FirebaseDatabase.getInstance().reference.child("users").child(uid)
            .child("talk").child(myFriend.key)
        yourDB = FirebaseDatabase.getInstance().reference.child("users").child(myFriend.key)
            .child("talk").child(uid)
        getImageIcon()
        initializationChayView()
        addChildListender(myDB) // 글로벌화시켜야함
        // 아직 미구현@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    }

    private fun addChildListender(ref: DatabaseReference) {
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val data = p0.getValue(ChatValue::class.java)
                var user: ChatUser? = null
                var flag = true
                if (data?.key == uid) {
                    user = me
                    flag = true
                    areyou = true
                } else {
                    user = you
                    flag = false
                    if (areyou) (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(40)
                    areyou = false
                }
                when (data?.type) {
                    "message" -> {

                        var message = Message.Builder()
                            .setUser(user)
                            .setRight(flag)
                            .setText(data.message!!)
                            .hideIcon(flag)
                            .build()
                        Handler().post {
                            mtalk.receive(message)
                        }
                    }
                    "photo" -> {
                        var message = Message.Builder()
                            .setUser(user)
                            .setRight(flag)
                            .setType(Message.Type.PICTURE)
                            .setPicture(myUtil.stringToBitmap(data?.photo))
                            .setText("")
                            .hideIcon(flag)
                            .build()
                        Handler().post {
                            mtalk.receive(message)
                        }
                    }
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }
        })
    }

    private fun getImageIcon() {
        dbr = FirebaseDatabase.getInstance().reference.child("users").child(myFriend.key).child("photo")
        Log.d("asdfasdf", myFriend.key)
        dbr.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                yourIcon = myUtil.stringToBitmap(p0.getValue(String::class.java)!!)
                you = ChatUser(1, myFriend.nickname, yourIcon)
                me = ChatUser(0, myNick, BitmapFactory.decodeResource(resources, R.drawable.face_2))

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    override fun onDestroy() {
        myUtil.whatChat = ""
        super.onDestroy()
    }

    private fun initializationChayView() {
        mtalk.setRightBubbleColor(ContextCompat.getColor(this, R.color.primary))
        mtalk.setLeftBubbleColor(R.color.primary)
        mtalk.setBackgroundColor(ContextCompat.getColor(this, R.color.blueGray500))
        mtalk.setSendButtonColor(ContextCompat.getColor(this, R.color.primary_darker))
        mtalk.setSendIcon(R.drawable.ic_action_send)
        mtalk.setRightMessageTextColor(Color.WHITE)
        mtalk.setLeftMessageTextColor(Color.WHITE)
        mtalk.setMessageStatusTextColor(Color.BLACK)
        mtalk.setUsernameTextColor(Color.WHITE)
        mtalk.setSendTimeTextColor(Color.WHITE)
        mtalk.setDateSeparatorColor(Color.WHITE)
        mtalk.inputTextColor = R.color.primary_darker
        mtalk.setInputTextHint("new message...")
        mtalk.setMessageMarginTop(5)
        mtalk.setMessageMarginBottom(5)
        mtalk.setAutoHidingKeyboard(true)
        mtalk.setOnClickSendButtonListener(this)
        mtalk.isEnabled = false
        mtalk.setOnClickOptionButtonListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var tents = Intent(this@TalkActivity, ProfileActivity::class.java)
                tents.putExtra("by", 2)
                startActivityForResult(tents, 37)
            }
        })
        mtalk.setOptionButtonColor(R.color.primary_darker)
    }

    private fun sendImage(map: Bitmap) {
        val msg = ChatValue()
        msg.type = "photo"
        msg.key = uid
        msg.message = ""
        msg.nickname = myFriend.nickname
        msg.photo = myUtil.bitmapToString(map)
        myDB.push().setValue(msg)
        yourDB.push().setValue(msg)
    }

    override fun onClick(v: View?) {


        var text = mtalk.inputText

        if (!text.isBlank()) {

            val msg = ChatValue()
            msg.type = "message"
            msg.key = uid
            msg.nickname = myFriend.nickname
            msg.message = text
            msg.photo = ""
            myDB.push().setValue(msg)
            yourDB.push().setValue(msg)

            mtalk.inputText = ""
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            75 -> {
                var profileUri = data?.getParcelableExtra("profileUri") as Uri


                var bitg = MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    profileUri
                )

                sendImage(bitg)

                var myFile = File(profileUri.path)
                if (myFile.exists()) myFile.delete()

            }

        }
    }


    override fun onBackPressed() {
        finish()
    }
}