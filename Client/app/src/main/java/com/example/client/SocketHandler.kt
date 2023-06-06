package com.example.client
import java.net.Socket
import android.os.Handler
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
object SocketHandler{
    private var socket: Socket? = null
    private var Hreciever: Handler? = null


    fun setSocket(new_s: Socket?) {
        socket = new_s
    }

    fun getSocket(): Socket? {
        return socket
    }

    fun getHreciever(): Handler? {
        return Hreciever
    }

    fun setHreciever(recv: Handler) {
        Hreciever = recv
    }
}


