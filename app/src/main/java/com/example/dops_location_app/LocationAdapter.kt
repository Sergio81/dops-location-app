package com.example.dops_location_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dops_location_app.model.LocationResponse

class LocationAdapter() : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {
    private val items = ArrayList<LocationResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return ViewHolder(inflater.inflate(R.layout.location_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun updateItems(items : ArrayList<LocationResponse>){
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addItem(item:LocationResponse){
        items.add(0, item)
        notifyDataSetChanged()
    }

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        private val txtLatitude = view.findViewById<TextView>(R.id.txtLatitude)
        private val txtLongitude = view.findViewById<TextView>(R.id.txtLongitude)
        private val txtElapsedTime = view.findViewById<TextView>(R.id.txtElapsedTime)

        fun bind(item : LocationResponse){
            txtLatitude.text = item.latitude.toString()
            txtLongitude.text = item.longitude.toString()
            txtElapsedTime.text = item.elapsedTime.toString()
        }
    }
}