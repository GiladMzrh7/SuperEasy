package com.example.client

import com.google.gson.Gson;
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.reflect.TypeToken
import java.math.BigInteger
import com.bumptech.glide.Glide


class MainActivity : AppCompatActivity() {
    var json_string = ""
    var chunkSize:Int = 0
    var SHOPS_CONNECTED = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val TAG = "MAIN"

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d("MAIN", "creating object main")


        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(message: Message) {

                val bg = TCP(sessionData.ip)

                val reply = message.obj.toString()
                var raw_data = reply
                if(!sessionData.DIFFIE_CONNECTED) {
                   raw_data = reply.substring(9)
                }

                val splitted = raw_data.split("#@@!?#%@")

                var data = ""

                val cmd = splitted[0]
                println("CMD $cmd")
                data = splitted[1]


                Log.d(TAG, cmd+data)
                println(cmd+data)

                if(cmd == "PRM"){
                    sessionData.DIFFIE_PRIME = BigInteger(data)
                    bg.execute("got")
                }

                else if(cmd == "GEN"){
                    sessionData.DIFFIE_GEN = BigInteger(data)
                    val my_result = sessionData.DIFFIE_GEN.modPow(sessionData.DIFFIE_MY_KEY,sessionData.DIFFIE_PRIME)
                    bg.execute(my_result.toString())

                }

                else if (cmd == "MNM"){
                    sessionData.DIFFIE_SERVER_NUMBER = BigInteger(data)

                    sessionData.DIFFIE_KEY = sessionData.DIFFIE_SERVER_NUMBER.modPow(sessionData.DIFFIE_MY_KEY,sessionData.DIFFIE_PRIME).toString(10).toString().substring(0,24)

                    Log.d("DIFFIE_END", sessionData.DIFFIE_KEY.toString())

                    sessionData.DIFFIE_CONNECTED = true

                    bg.execute("GET","DATA")


                }

                else if (cmd == "CHL"){
                    chunkSize = data.toInt()
                    bg.execute("GOT","a")
                }

                else if(cmd == "CHD"){
                    json_string += data
                    bg.execute("GOT","a")

                }

                else if (cmd == "FNS"){
                    json_string += data

                    sessionData.SHOPS_CONNECTED = true

                    Log.d("FINAL",json_string)
                    val gson = Gson()
                    val type = object: TypeToken<Map<String, String>>() {}.type
                    println(json_string)
                    sessionData.PRODCUTS_CODES =  gson.fromJson(json_string,type)

                    Log.d(TAG, sessionData.PRODCUTS_CODES.toString())

                    sessionData.PRODUCT_NAMES = sessionData.PRODCUTS_CODES.keys.toTypedArray()

                    sessionData.PRODUCT_NAMES = sessionData.PRODUCT_NAMES.clone()

                    sessionData.PRODUCTS_BACKUP = sessionData.PRODUCT_NAMES.clone()
                    sessionData.selected = BooleanArray(sessionData.PRODUCT_NAMES.size){false}

                    val img = findViewById<ImageView>(R.id.loading)
                    img.visibility = View.GONE

                }

            }
        }


        val imageView = findViewById<ImageView>(R.id.loading)
        imageView.visibility = View.GONE




        SocketHandler.setHreciever(mHandler)
        Log.d(TAG, "finished creating")
        if(!sessionData.DIFFIE_CONNECTED)
        {
            imageView.visibility = View.VISIBLE
            Glide.with(this).load(R.drawable.loading).into(imageView)
            val bg = TCP(sessionData.ip)
            bg.execute("dh_aes")
        }

        if(sessionData.logged){
            val registerButton = findViewById<Button>(R.id.Register)
            val loginButton = findViewById<Button>(R.id.Login)
            val welcome = findViewById<TextView>(R.id.welcome)

            registerButton.visibility = View.GONE
            loginButton.visibility = View.GONE

            welcome.text = "${sessionData.username} Welcome to SuperEasy!"
        }


    }

    //going to the Register page
    fun register(view:View){
        if(!sessionData.SHOPS_CONNECTED){ return }
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    //going to the Login page
    fun login(view: View){
        if(!sessionData.SHOPS_CONNECTED){ return }

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun search(view:View){
        if(!sessionData.SHOPS_CONNECTED){ return }
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }


}