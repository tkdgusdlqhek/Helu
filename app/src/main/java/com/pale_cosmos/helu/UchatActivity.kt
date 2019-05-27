package com.pale_cosmos.helu


import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.github.bassaer.chatmessageview.model.ChatUser
import com.github.bassaer.chatmessageview.model.Message
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_u_chat.*

class UchatActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var database: FirebaseDatabase
    lateinit var storage: FirebaseStorage
    var dataRef: DatabaseReference?=null
    lateinit var myDataRef: DatabaseReference
    lateinit var stoRef: StorageReference
    lateinit var nicknames: String
    lateinit var key: String
    lateinit var univ: String
    lateinit var depart: String
    lateinit var me: ChatUser
    lateinit var you: ChatUser
    var myInfo: UserInfo?=null
    lateinit var wantgenderString: String
    lateinit var intents: Intent
    var yourInfo: UchatInfo?=null
    var wantgender = true
    var myId: Int = 0
    lateinit var myIcon: Bitmap
    var yourId = 1
    lateinit var yourIcon: Bitmap

    var backKeyPressedTime: Long = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        updateStatusBarColor("#E43F3F")
        setContentView(R.layout.activity_u_chat)
        initializationChatView()
        initialization()

    }

    private fun initialization() {

        setValue()
        intents = Intent(this@UchatActivity, SocketReceiveDialog::class.java)
        intents.putExtra("USERINFO", myInfo)
        intents.putExtra("key", key)
        intents.putExtra("univ", univ)
        intents.putExtra("depart", depart)
        intents.putExtra("wantgender", wantgenderString)
        startActivityForResult(intents, 1)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()


    }

    private fun setValue() {
        myId = 0
        myIcon = BitmapFactory.decodeResource(resources, R.drawable.face_2)
        yourId = 1

        wantgender = intent.getBooleanExtra("wantgender", true)
        wantgenderString = if (wantgender) {
            "true"
        } else {
            "false"
        }
        myInfo = intent.getSerializableExtra("USERINFO") as UserInfo?
        key = intent.getStringExtra("key")
        database = FirebaseDatabase.getInstance()
        myDataRef = database.reference.child("chats").child("$key").child("Uchat")
        addChildListener(myDataRef)
        univ = intent.getStringExtra("univ")
        depart = intent.getStringExtra("depart")
        me = ChatUser(0, myInfo?.nickname!!, myIcon)
    }

    private fun addChildListener(ref: DatabaseReference) {
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val data = p0.getValue(ChatValue::class.java)

                when (data?.type) {
                    "message" -> {
                        var message = Message.Builder()
                            .setUser(you)
                            .setRight(false)
                            .setText(data.message!!)
                            .hideIcon(false)
                            .build()
                        Handler().post {
                            mChatView.receive(message)
                        }
                    }
                    "photo" -> {

                    }
                }


            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })


    }


    private fun initializationChatView() {

        mChatView.setRightBubbleColor(ContextCompat.getColor(this, R.color.primary))
        mChatView.setLeftBubbleColor(R.color.primary)
        mChatView.setBackgroundColor(ContextCompat.getColor(this, R.color.blueGray500))
        mChatView.setSendButtonColor(ContextCompat.getColor(this, R.color.primary_darker))
        mChatView.setSendIcon(R.drawable.ic_action_send)
        mChatView.setRightMessageTextColor(Color.WHITE)
        mChatView.setLeftMessageTextColor(Color.WHITE)
        mChatView.setMessageStatusTextColor(Color.BLACK)
        mChatView.setUsernameTextColor(Color.WHITE)
        mChatView.setSendTimeTextColor(Color.WHITE)
        mChatView.setDateSeparatorColor(Color.WHITE)
        mChatView.inputTextColor = R.color.primary_darker
        mChatView.setInputTextHint("new message...")
        mChatView.setMessageMarginTop(5)
        mChatView.setMessageMarginBottom(5)
        mChatView.setAutoHidingKeyboard(true)
        mChatView.setOnClickSendButtonListener(this)
        mChatView.isEnabled = false

    }

    fun updateStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor(color)
        }
    }

    override fun onClick(v: View?) {

        var text = mChatView.inputText

        if (!text.isBlank()) {
            var message = Message.Builder()
                .setUser(me)
                .setRight(true)
                .setText(text)
                .hideIcon(true)
                .build()

            if(dataRef!=null) {
                val msg = ChatValue()
                msg.type="message"
                msg.key=key
                msg.message=text
                msg.photo=null
                dataRef?.push()?.setValue(msg)
            }
            mChatView.inputText = ""
            Handler().post {
                mChatView.receive(message)
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(resultCode)
        {
            88-> finish()
            32->finish()
            8080->{
                yourInfo = data?.getSerializableExtra("yourInfo") as UchatInfo?

                stoRef = storage.reference.child("profile/${yourInfo?.key}.png")
                GlideApp.with(applicationContext).asBitmap().load(stoRef)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            yourIcon = resource
                            you = ChatUser(1, yourInfo?.nickname!!, yourIcon)
                        }
                    })
//여기부터추가해야함

                dataRef = database.reference.child("chats").child("${yourInfo?.key}").child("Uchat")

            }
            8081->finish()
            8082->finish()
        }

    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000L) {
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(applicationContext, "나갈거야?", Toast.LENGTH_SHORT).show()
            return
        } else {
            val back = Intent(this, BackKeyPress::class.java)
            back.putExtra("code", 0)
            startActivityForResult(back, 1)
        }
    }

}