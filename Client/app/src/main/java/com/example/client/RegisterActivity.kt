package com.example.client

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.view.View
import android.widget.EditText

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(message: Message) {
                val reply = message.obj.toString()

                val splitted = reply.split("#@@!?#%@##")
                val cmd = splitted[0]
                val data = splitted[1]


                if (cmd == "WEL"){
                    sessionData.logged = true
                    sessionData.username = findViewById<TextView>(R.id.editTextTextPersonName).text.toString()

                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)


                }
                else if(cmd == "ERR"){
                    val server_resp = findViewById<TextView>(R.id.serverAnswer)
                    server_resp.text = data
                }

            }
        }

    }

    fun submit(view: View){
        val password = findViewById<EditText>(R.id.editTextTextPassword2)
        val verify_password = findViewById<EditText>(R.id.editTextTextPassword)
        val uname = findViewById<TextView>(R.id.editTextTextPersonName)

        if(password.text.toString() != verify_password.text.toString()){
            val error_label = findViewById<TextView>(R.id.serverAnswer)
            error_label.text = "NOT THE SAME PASSWORD"
            return
        }

        val bg = TCP(sessionData.ip)
        bg.execute("REG",uname.text.toString(), password.text.toString())



    }

}
