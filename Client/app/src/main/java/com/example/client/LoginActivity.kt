package com.example.client

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)

        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(message: Message) {
                val reply = message.obj.toString()

                val splitted = reply.split("#@@!?#%@")

                val cmd = splitted[0]
                val data = splitted[1]


                if (cmd == "SUC"){
                    sessionData.logged = true
                    sessionData.username = findViewById<TextView>(R.id.editTextTextPersonName).text.toString()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)


                }
                else if(cmd == "ERR"){
                    val server_resp = findViewById<TextView>(R.id.serverAnswer)
                    server_resp.text = data
                }

            }
        }

        SocketHandler.setHreciever(mHandler)
    }

    //submits the login info to server
    fun submit(view: View) {


        val TAG = "SUBMIT_BUTTON"
        val userNameText = findViewById<TextView>(R.id.editTextTextPersonName)
        val password = findViewById<EditText>(R.id.editTextTextPassword2)

        sessionData.username = userNameText.text.toString()

        Log.d(TAG, "Starting login process with " + userNameText.text.toString())


        val bg = TCP(sessionData.ip)
        bg.execute("LOG",sessionData.username, password.text.toString())


    }

}