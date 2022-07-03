package com.playgroundagc.blescanner

import android.bluetooth.le.ScanResult
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.playgroundagc.blescanner.databinding.ScanResultLayoutBinding

class ScanResultAdapter(private var list: MutableList<ScanResult>) :
    RecyclerView.Adapter<ScanResultAdapter.ScanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        return ScanViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        val currentItem = list[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int = list.size

    fun setData(scans: MutableList<ScanResult>) {
        list = scans
        notifyDataSetChanged()
    }

    class ScanViewHolder(private val binding: ScanResultLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(scanResult: ScanResult) {
            Log.d("TAG", "scanResult: $scanResult")
            Log.d("TAG", "device: ${scanResult.device}")
            Log.d("TAG", "rssi: ${scanResult.rssi}")
            Log.d("TAG", "address: ${scanResult.device.address}")
            with(binding) {
                this.result = scanResult
                //Log.d("TAG results", "item: ${scanResult.device}")
            }
        }

        companion object {
            fun from(parent: ViewGroup): ScanViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    ScanResultLayoutBinding.inflate(layoutInflater, parent, false)
                return ScanViewHolder(binding)
            }
        }
    }
}
