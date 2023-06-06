package com.example.client

import java.math.BigInteger


object sessionData {
    var username = ""
    var logged = false
    val ip =  "192.168.14.102"

    lateinit var PRODUCT_NAMES:Array<String>
    var ID_OF_CHOSEN = ArrayList<Int>()
    var NAMES_OF_CHOSEN = listOf<String>()


    lateinit var PRODCUTS_CODES: Map<String, String>
    lateinit  var selected: BooleanArray
    lateinit var PRODUCTS_BACKUP: Array<String>
    var cheap = 0
    var near = 0
    var mood = 0

    var SHOPS_CONNECTED = false

    var LOCATION_MADE = false


    val DIFFIE_MY_KEY= randomDigits(8)

    lateinit var DIFFIE_PRIME:BigInteger
    var DIFFIE_CONNECTED = false
    lateinit var DIFFIE_KEY: String
    lateinit var DIFFIE_SERVER_NUMBER:BigInteger
    lateinit var DIFFIE_GEN: BigInteger

    fun randomDigits(length: Int): BigInteger {
        val digits = "0123456789"
        val random = java.util.Random()
        return (1..length)
            .map { digits[random.nextInt(digits.length)] }
            .joinToString("").toBigInteger()
    }

}
