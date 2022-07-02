package com.playgroundagc.blescanner

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.playgroundagc.blescanner.databinding.ActivityMainBinding

private const val ENABLE_BLUETOOTH_CODE = 1

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {
            lifecycleOwner = this@MainActivity
            executePendingBindings()
            bleScanner.setOnClickListener {
                if (checkBluetoothStatus()) {
                    Toast.makeText(applicationContext, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
                    if (checkBluetoothPermission()) {
                        Toast.makeText(applicationContext, "Bluetooth granted", Toast.LENGTH_SHORT).show()

                    // TODO: Scan for BLE devices

                    } else {
                        Toast.makeText(applicationContext, "Bluetooth not granted", Toast.LENGTH_SHORT).show()
                        askBluetoothPermission()
                    }
                } else {
                    Toast.makeText(applicationContext, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkBluetoothStatus(): Boolean {
        return if (bluetoothAdapter.isEnabled) {
            true
        } else {
            askBluetoothPermission()
            false
        }
    }

    private fun checkBluetoothPermission(): Boolean {
        return when (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)) {
            PackageManager.PERMISSION_GRANTED -> true
            else -> false
        }
    }

    private fun askBluetoothPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.BLUETOOTH),
            ENABLE_BLUETOOTH_CODE)
    }
}