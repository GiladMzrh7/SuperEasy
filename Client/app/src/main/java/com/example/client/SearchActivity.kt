package com.example.client

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text


class SearchActivity : AppCompatActivity() {
    var SHOPS = listOf<Shop>()
    lateinit  var fusedLocation: FusedLocationProviderClient
    lateinit var requestQueue: RequestQueue
    var ADDRESS = ""
    var latitude  = 0.0
    var longitude = 0.0
    var json_string = ""
    var chunkSize = 0
    var BAR_START = 0
    val BAR_LENGTH = 5

    lateinit var shop_map: Map<String, List<String>>




    override fun onCreate(savedInstanceState: Bundle?) {

        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(message: Message) {
                val reply = message.obj.toString()

                var raw_data = reply
                if(!sessionData.DIFFIE_CONNECTED) {
                    raw_data = reply.substring(9)
                }
                val splitted = raw_data.split("#@@!?#%@")
                val cmd = splitted[0]
                println(cmd)
                val data = splitted[1]
                val bg = TCP(sessionData.ip)

                if (cmd == "ADR"){
                    ADDRESS = data
                    val button = findViewById<Button>(R.id.location_button)
                    button.text = ADDRESS
                }

                if (cmd == "CHL"){
                    chunkSize = data.toInt()
                    bg.execute("GOT","A")
                }

                else if(cmd == "CHD"){
                    json_string += data
                    bg.execute("GOT","a")
                }

                else if (cmd == "FNS"){
                    json_string += data

                    Log.d("FINAL",json_string)
                    val gson = Gson()
                    val type = object: TypeToken<Map<String, List<String>>> () {}.type
                    shop_map =  gson.fromJson(json_string,type)

                    shop_map.forEach{ (key, arr) ->
                        val price = arr[0].toFloat()
                        val recom = arr[1].toFloat()
                        val distance = arr[2].toInt()
                        val rating = arr[3].toFloat()
                        val id = arr[4].toInt()

                        val data =  key.split("|")
                        val addr = data[1]
                        val name = data[0]

                        val shop = Shop(name,addr,price,recom, distance, rating, id)
                        SHOPS = SHOPS + shop

                    }

                    val recyclerView: RecyclerView = findViewById(R.id.recycle)
                    val adapter = ShopAdapter(SHOPS, this@SearchActivity)
                    recyclerView.adapter = adapter

                    recyclerView.layoutManager = LinearLayoutManager(this@SearchActivity)
                    recyclerView.layoutDirection = View.LAYOUT_DIRECTION_RTL

                    val imageView = findViewById<ImageView>(R.id.loading)
                    imageView.visibility = View.GONE
                }

            }
        }
        SocketHandler.setHreciever(mHandler)

        super.onCreate(savedInstanceState)


        val TAG = "SEARCH"
        Log.d(TAG, "created")
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)


        requestQueue = Volley.newRequestQueue(this)


        setContentView(R.layout.search_activity)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUESTS_CODES.LOCATION_REQUEST_CODE)


        SocketHandler.setHreciever(mHandler)


        getLocation()

    }

    private fun getLocation() {
        val bg = TCP(sessionData.ip)

        val TAG = "LOCATIONSETTER"
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e(TAG, "NAH MATE")
            return
        }

        fusedLocation.lastLocation.addOnSuccessListener { location ->
            // Got last known location. In some rare situations this can be null.
            Log.d(TAG, "load location")
            if (location != null) {
                Log.d(TAG, "LOC LOC LOC LOC LOC LOC")
                latitude = location.latitude
                longitude = location.longitude
                bg.execute("LOC","$latitude","$longitude")
                sessionData.LOCATION_MADE = true
            }
            else{
                Log.d(TAG, "THIS IS NULL")
            }
        }

        Log.d(TAG, "finished")

    }


    fun selectMap(view: View){
    }

    fun chooseOpinion(view: View){
        if(!sessionData.LOCATION_MADE){return}

        val builder = AlertDialog.Builder(this)
        builder.setTitle("מה אתה מעדיף?")
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.choose_layout, null)
        builder.setView(dialogLayout)

        val nearFar:SeekBar = dialogLayout.findViewById(R.id.near_far_seekbar)
        val nearFarText:AppCompatTextView = dialogLayout.findViewById(R.id.near_far_label)
        nearFar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(seekBar == null){
                    return
                }

                if (seekBar.progress < 25){
                    nearFarText.text = "קרוב מאוד"
                }
                else if (seekBar.progress > 24 && seekBar.progress < 50){
                    nearFarText.text = "קרוב"
                }
                else if (seekBar.progress > 50 && seekBar.progress < 75){
                    nearFarText.text = "רחוק"
                }
                else if (seekBar.progress > 75){
                    nearFarText.text = "רחוק מאוד"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        val cheapExpensive = dialogLayout.findViewById<SeekBar>(R.id.cheap_expensive_seekbar)
        val cheapExpensiveText:AppCompatTextView = dialogLayout.findViewById(R.id.cheap_expensive_label)
        cheapExpensive.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(seekBar == null){
                    return
                }

                if (seekBar.progress < 25){
                    cheapExpensiveText.text = "זול מאוד"
                }
                else if (seekBar.progress > 24 && seekBar.progress < 50){
                    cheapExpensiveText.text = "זול"
                }
                else if (seekBar.progress > 50 && seekBar.progress < 75){
                    cheapExpensiveText.text = "יקר"
                }
                else if (seekBar.progress > 75){
                    cheapExpensiveText.text = "יקר מאוד"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        val mood = dialogLayout.findViewById<SeekBar>(R.id.mood_seekbar)
        val moodText:AppCompatTextView = dialogLayout.findViewById(R.id.mood_label)
        mood.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(seekBar == null){
                    return
                }

                if (seekBar.progress < 25){
                    moodText.text = "⭐"
                }
                else if (seekBar.progress > 24 && seekBar.progress < 50){
                    moodText.text = "⭐⭐"
                }
                else if (seekBar.progress >= 50 && seekBar.progress < 75){
                    moodText.text = "⭐⭐⭐"
                }
                else if (seekBar.progress >= 75 && seekBar.progress < 100){
                    moodText.text = "⭐⭐⭐⭐"
                }
                else{
                    moodText.text = "⭐⭐⭐⭐⭐"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        nearFar.progress = sessionData.near
        cheapExpensive.progress = sessionData.cheap
        mood.progress = sessionData.mood

        builder.setPositiveButton("OK") { _, _ ->
            val nearFarValue: Int = nearFar.progress
            val cheapExpensiveValue: Int = cheapExpensive.progress
            val moodValue: Int = mood.progress

            sessionData.near = nearFarValue
            sessionData.cheap = cheapExpensiveValue
            sessionData.mood = moodValue

        }


        val dialog = builder.create()
        dialog.show()

    }

    fun search_act(view: View){

        val TAG = "SEARCHBAR"
        Log.d(TAG, "button pressed")

        val  builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("מה תרצו לקנות?")

        builder.setMultiChoiceItems(sessionData.PRODUCT_NAMES,sessionData.selected) { _, i, b ->
            if (b) {
                sessionData.ID_OF_CHOSEN.add(i)
                sessionData.NAMES_OF_CHOSEN = sessionData.NAMES_OF_CHOSEN + sessionData.PRODUCT_NAMES[i]

            } else {
                sessionData.ID_OF_CHOSEN.remove(i)
                sessionData.NAMES_OF_CHOSEN = sessionData.NAMES_OF_CHOSEN - (sessionData.PRODUCT_NAMES[i])
            }
        }

        //button that let go of the Alert`
        builder.setNegativeButton("חזור", DialogInterface.OnClickListener(){ d, _ ->
            d.dismiss()
        })

        builder.setNegativeButton("בוצע", DialogInterface.OnClickListener(){ d, _ ->

            val json = JSONArray()
            sessionData.NAMES_OF_CHOSEN.forEach {
                json.put(it)
            }

            val obj = JSONObject()
            obj.put("data", json)

            val jsonString = obj.toString()

            val bg = TCP(sessionData.ip)
            bg.execute("PRD", jsonString)

            json_string = ""
            SHOPS = listOf()
        })

        builder.setNeutralButton("נקה הכל") { _, _ ->
            for (j in sessionData.selected.indices) {
                sessionData.selected[j] = false
            }
            sessionData.ID_OF_CHOSEN.clear()
            builder.show()
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun refresh(view: View){
        val imageView = findViewById<ImageView>(R.id.loading)
        imageView.visibility = View.VISIBLE
        Glide.with(this).load(R.drawable.loading).into(imageView)


        val bg = TCP(sessionData.ip)
        bg.execute("DAT","${sessionData.near}","${sessionData.cheap}","${sessionData.mood}")


    }

}
