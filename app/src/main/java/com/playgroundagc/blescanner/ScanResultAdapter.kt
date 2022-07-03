package com.playgroundagc.blescanner

import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.playgroundagc.blescanner.databinding.ScanResultLayoutBinding
import kotlin.math.pow

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
            with(binding) {
                this.result = scanResult

                val distance = calculateDistance(scanResult)

                deviceDistance.text = distance.toString()

                val signal = when (distance)  {
                    in 0.0..2.0 -> R.drawable.ic_network_4_bar
                    in 2.0..4.0 -> R.drawable.ic_network_3_bar
                    in 4.0..8.0 -> R.drawable.ic_network_2_bar
                    else -> R.drawable.ic_network_1_bar
                }

                signalLogo.setImageResource(signal)
            }
        }

        private fun calculateDistance(scanResult: ScanResult): Double =
            10.0.pow((-69 - scanResult.rssi) / (10.0 * 2.0))

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
