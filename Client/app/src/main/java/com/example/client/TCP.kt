package com.example.client
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import android.os.AsyncTask
import android.os.Handler
import android.util.Log

import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException

import javax.crypto.spec.IvParameterSpec
import java.util.*
import kotlin.math.abs


val SEPRATOR = "#@@!?#%@"

fun decrypt(cipherText: String): String {

    println(cipherText)
    val algorithm = "AES/CBC/PKCS5Padding"
    val encText = Base64.getDecoder().decode(cipherText)

    println("BASE64DEC $encText")
    println(encText.size)

    val iv_string = encText.copyOfRange(0,16)
    val restText = encText.copyOfRange(18, encText.size)

    val key = SecretKeySpec(sessionData.DIFFIE_KEY.toByteArray(), "AES")
    val cipher = Cipher.getInstance(algorithm)

    val iv = IvParameterSpec(iv_string)

    cipher.init(Cipher.DECRYPT_MODE, key, iv)

    val plainText = cipher.doFinal(restText)

    return String(plainText, Charsets.UTF_8)
}

fun encrypt(inputText: String, ): String {
    val algorithm = "AES/CBC/PKCS5Padding"

    val cipher = Cipher.getInstance(algorithm)

    val key = SecretKeySpec(sessionData.DIFFIE_KEY.toByteArray(), "AES")

    val iv = ByteArray(16)
    val random = Random()
    random.nextBytes(iv)

    val iv_spec = IvParameterSpec(iv)

    cipher.init(Cipher.ENCRYPT_MODE, key, iv_spec)
    val cipherText = cipher.doFinal( inputText.toByteArray())
    println(cipherText)



    return Base64.getEncoder().encodeToString(iv + "?|".toByteArray() +cipherText)
}



val DEBUG = false

class TCP(ip: String) : AsyncTask<String, Handler, Void>() {
    lateinit var pw: PrintWriter
    var IP: String = sessionData.ip
    val LEN_SIZE: Int = 4
    lateinit var arr: List<String>

    init {
        if (this.IP == "") {
            this.IP = ip
        }
        var sock: Socket? = SocketHandler.getSocket()

    }



    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: String): Void? {
        val TAG = "doInBackground"
        var sk :Socket? = SocketHandler.getSocket()

        if (params.isEmpty()){
            Log.e(TAG, "no params")
            return  null
        }

        if (sk == null){
            try {
                Log.d(TAG, "before Connect")

                sk = Socket(IP, 27391)
                SocketHandler.setSocket(sk)
                Log.d(TAG, "connected successfully")
                val listener = Thread(Listener(sk))
                listener.start()

            }
            catch (e: UnknownHostException){
                Log.e(TAG, "ERROR UnknownHostException")
                return null
            }
            catch (e: IOException){
                Log.e(TAG, "ERROR ioException"+e.message)
                return null
            }

        }

        println("SENDING SEDNING $params")
        if(sessionData.DIFFIE_CONNECTED){
            arr = params.map { encrypt(it)}
        }
        else{
            arr = params.map { it }
        }

        var data = arr.joinToString(SEPRATOR)




        Log.d(TAG, data)

        data = (String.format("%08d", data.length) + "?|") + data

        Log.d("a", data)

        try{
            this.pw = PrintWriter(sk.getOutputStream())
            Log.d(TAG, "Sending")
            pw.write(data)
            pw.flush()
            Log.d(TAG, "after send:$data")
        }catch (e:IOException){
            Log.e(TAG, "ERROR write" + e.message)

            val mHandler = SocketHandler.getHreciever()

            if(mHandler == null){
                Log.e(TAG, "mHandler is null")
                return null
            }

            val msg = mHandler.obtainMessage()
            msg.obj = "SocketError"
            mHandler.sendMessage(msg)

        }



        return null
    }

    class Listener(sock: Socket) : Runnable{
        lateinit var skl: Socket
        var input:InputStream = sock.getInputStream()
        var cbuf: ByteArray
        val TAG = "Listener"

        init {
            try {
                this.skl = sock
                this.input = sock.getInputStream()
            }

            catch (e:IOException){
                Log.e(TAG, "ERROR buffer read" + e.message)
            }

            this.cbuf = ByteArray(2400000)
        }

        override fun run() {
            val ok = true

            while (ok){
                try {
                    if(DEBUG){Log.d(TAG, "starting recieve")}

                    if (input.available() > 0) {
                        var len_read: Int = 0

                        val cbuflen = ByteArray(8) //len size

                        while (len_read < 8) {
                            len_read += input.read(cbuflen)
                        }

                        if(DEBUG){Log.d("LOGGEDDATA", String(cbuflen,0, len_read))}

                        val recieved_len = String(cbuflen, 0, len_read)

                        val final_len = recieved_len.toInt()

                        Log.d(TAG, "recieved: $recieved_len")

                        val len = input.read(cbuf, 0, final_len + 1)
                        val all_msg = recieved_len + String(cbuf,0, len)

                        if(DEBUG){Log.d(TAG, "lem $len")}

                        if (len > 0) {
                            Log.d(TAG, "~~~~~GOT DATA ~~~~~~$all_msg")

                            val  mHandler = SocketHandler.getHreciever()



                            if (mHandler == null) {
                                try {
                                    Log.d(TAG, "dont be be here")
                                    Thread.sleep(2000)
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                            }
                            if (mHandler == null) {
                                try {
                                    Log.d(TAG, "dont be be here")
                                    Thread.sleep(2000)
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }

                            }
                            if (mHandler != null) {
                                val msg = mHandler.obtainMessage()
                                if (sessionData.DIFFIE_CONNECTED) {
                                    val no_len = all_msg.substring(8,all_msg.length)
                                    val arr = no_len.split(SEPRATOR)

                                    val dec_data = arrayListOf<String>()
                                    arr.forEach { dec_data.add(decrypt(it)) }


                                    msg.obj = dec_data.joinToString(SEPRATOR)

                                    println("FINAL FINAL FINAL ${msg.obj}")
                                }
                                else{
                                    msg.obj = all_msg
                                }
                                mHandler.sendMessage(msg)
                            }

                        } else {
                            Log.e(TAG, "Handle was null SKIPPED:$all_msg")
                            return
                        }
                    }



                }
                catch (e: IOException){
                    Log.e(TAG, "ERROR " + e.message)
                }
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    Log.e(TAG, "ERROR InterruptedException " + e.message)
                    e.printStackTrace()
                }
            }

        }
    }


}