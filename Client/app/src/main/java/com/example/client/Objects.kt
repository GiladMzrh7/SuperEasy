package com.example.client


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt
import android.content.Context
import android.provider.Settings.System.getString
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView

class Shop(val name: String, val Address: String, val totalPrice: Float, val recom:Float, val distance:Int, val rating: Float, val id:Int) {}

class ShopViewHolder(itemView: View, val context: Context) : RecyclerView.ViewHolder(itemView) {
    private val shopNameTextView: TextView = itemView.findViewById(R.id.shop_name_text_view)
    private val shopDescriptionTextView: TextView =
        itemView.findViewById(R.id.shop_description_text_view)
    private val leaveReviewButton: Button = itemView.findViewById(R.id.leave_review_button)
    private val ratingTextView: TextView = itemView.findViewById(R.id.rating)
    private val address: TextView = itemView.findViewById(R.id.shop_address)
    private val price: TextView = itemView.findViewById(R.id.price)

    fun bind(shop: Shop) {
        shopNameTextView.text = shop.name
        shopDescriptionTextView.text = shop.recom.toString()
        ratingTextView.text = String.format("%.1f", shop.rating) + " " + "⭐".repeat(shop.rating.roundToInt())
        address.text = shop.Address
        price.text = context.getString(R.string.price_message, shop.totalPrice.toString())

        leaveReviewButton.setOnClickListener {
            Log.d("aaa", "aaa")
            val builder = AlertDialog.Builder(context)
            builder.setTitle("מה אתה מעדיף?")
            val dialogLayout = LayoutInflater.from(context).inflate(R.layout.rate_layout, null)
            builder.setView(dialogLayout)

            val mood = dialogLayout.findViewById<SeekBar>(R.id.mood_seekbar)
            val moodText: AppCompatTextView = dialogLayout.findViewById(R.id.mood_label)
            mood.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if(seekBar == null){
                        return
                    }

                    if (seekBar.progress < 25){
                        moodText.text = "⭐"
                    }
                    else if (seekBar.progress >= 25 && seekBar.progress < 50){
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

            builder.setPositiveButton("OK") { _, _ ->
                val bg = TCP(sessionData.ip)
                val stars = 5 - ((100 - mood.progress)/100).toInt()

                bg.execute("RAT","$stars", shop.id.toString())



            }
            val dialog = builder.create()
            dialog.show()
            }
    }
}

class ShopAdapter(private val shops: List<Shop>, private val cont: Context) : RecyclerView.Adapter<ShopViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.shop, parent, false)
        return ShopViewHolder(itemView, cont)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shop = shops[position]
        holder.bind(shop)
    }

    override fun getItemCount(): Int {
        return shops.size
    }
}
