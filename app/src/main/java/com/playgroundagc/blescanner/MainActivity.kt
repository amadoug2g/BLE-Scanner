package com.playgroundagc.blescanner

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.playgroundagc.blescanner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //region Variables
    private lateinit var binding: ActivityMainBinding

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    var locationIntent: Intent? = null

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    private val isBluetoothPermissionGranted
        get() = hasPermission(Manifest.permission.BLUETOOTH)

    private val isBluetoothOn
        get() = bluetoothAdapter.isBluetoothEnabled()

    private val isLocationOn
        get() = isLocationEnabled()

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { binding.bleScanner.text = if (value) "Stop scan" else "Start scan" }
        }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                // TODO: Handle the Intent
            }
        }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .build()
    } else {
        ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (result != null && ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                with(result.device) {
                    Log.d(
                        "TAG device",
                        "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                    )
                }
            }
        }
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        checkActivation()

        binding.apply {
            lifecycleOwner = this@MainActivity
            executePendingBindings()
            bleScanner.setOnClickListener {
                checkPermissions()
            }
        }
    }
    //endregion

    //region Permissions
    private fun checkPermissions() {
        checkActivation()

        if (isLocationPermissionGranted && isBluetoothPermissionGranted) {
            if (isLocationOn && isBluetoothOn) {
                startScan()
            } else {
                if (!isLocationOn) activateLocation()
                if (!isBluetoothOn) activateBluetooth()
            }
        } else {
            if (!isLocationPermissionGranted) requestLocationPermission()
            if (!isBluetoothPermissionGranted) requestBluetoothPermission()
        }
    }

    private fun checkActivation(): Boolean {
        return checkLocationPermission() && checkBluetoothPermission()
    }
    //endregion

    //region Bluetooth
    private fun checkBluetoothPermission(): Boolean {
        return when (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH
        )) {
            PackageManager.PERMISSION_GRANTED -> true
            else -> false
        }
    }

    private fun requestBluetoothPermission() {
        val requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    Log.d("Permission requests", "${it.key} = ${it.value}")
                }
            }

        //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH), BLUETOOTH_REQUEST_CODE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            val requestBluetooth =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        "Bluetooth permission granted".toast()
                    } else {
                        "Bluetooth permission denied".toast()
                    }
                }

            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
    }

    private fun activateBluetooth() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startForResult.launch(intent)
    }
    //endregion

    //region Location
    private fun checkLocationPermission(): Boolean {
        return when (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )) {
            PackageManager.PERMISSION_GRANTED -> true
            else -> false
        }
    }

    private fun requestLocationPermission() {
        val activityResultLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                // Handle Permission granted/rejected
                if (isGranted) {
                    "Permission granted!".toast()
                } else {
                    // Permission is denied
                    "Permission denied :(".toast()
                }
            }

        activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun activateLocation() {
        locationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(locationIntent)
    }
    //endregion

    //region BLE Actions
    private fun startScan() {
        if (isScanning) {
            stopScan()
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bleScanner.startScan(null, scanSettings, scanCallback)
            }
        }
    }

    private fun stopScan() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            bleScanner.stopScan(scanCallback)
            isScanning = false
        }
    }
    //endregion

    //region Extension Functions
    private fun String.toast() {
        Toast.makeText(applicationContext, this, Toast.LENGTH_SHORT).show()
    }

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Context.isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun BluetoothAdapter.isBluetoothEnabled(): Boolean {
        return this.isEnabled
    }
    //endregion
}