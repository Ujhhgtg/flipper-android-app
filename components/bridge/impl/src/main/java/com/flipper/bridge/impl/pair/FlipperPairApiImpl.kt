package com.flipper.bridge.impl.pair

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.flipper.bridge.api.device.FlipperDeviceApi
import com.flipper.bridge.api.pair.FlipperPairApi
import com.flipper.bridge.api.scanner.FlipperScanner
import com.flipper.bridge.api.utils.Constants
import com.flipper.bridge.api.utils.DeviceFeatureHelper
import com.flipper.bridge.api.utils.PermissionHelper
import com.flipper.bridge.impl.device.FlipperDeviceApiImpl
import com.flipper.bridge.impl.manager.FlipperBleManagerImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.ble.exception.BluetoothDisabledException
import javax.inject.Inject

class FlipperPairApiImpl @Inject constructor(
    private val scanner: FlipperScanner,
    private val adapter: BluetoothAdapter
) : FlipperPairApi {
    private val cachedDeviceApi: Map<String, FlipperDeviceApi> = mapOf()

    override fun getFlipperApi(
        context: Context,
        deviceId: String
    ): FlipperDeviceApi {
        val deviceApi = cachedDeviceApi[deviceId]
        if (deviceApi != null) {
            return deviceApi
        }

        val manager = FlipperBleManagerImpl(context)
        return FlipperDeviceApiImpl(manager, deviceId)
    }

    @ExperimentalCoroutinesApi
    override suspend fun connect(
        context: Context,
        flipperDeviceApi: FlipperDeviceApi
    ) {
        // If we already connected to device, just ignore it
        if (flipperDeviceApi.getBleManager().isDeviceConnected) {
            return
        }
        // If Bluetooth disable, return exception
        if (!PermissionHelper.isBluetoothEnabled()) {
            throw BluetoothDisabledException()
        }

        // If we use companion feature, we can't connect without bonded device
        if (DeviceFeatureHelper.isCompanionFeatureAvailable(context)) {
            connectWithBondedDevice(flipperDeviceApi)
            return
        }

        // If companion feature not available, we try find device in manual mode and connect with it
        findAndConnectToDevice(context, flipperDeviceApi)
    }

    override fun scheduleConnect(flipperDeviceApi: FlipperDeviceApi, device: BluetoothDevice) {
        if (flipperDeviceApi.getBleManager().isDeviceConnected) {
            return
        }
        flipperDeviceApi.getBleManager().connectToDevice(device)
    }

    private fun connectWithBondedDevice(flipperDeviceApi: FlipperDeviceApi) {
        val device = adapter.bondedDevices.find { it.address == flipperDeviceApi.address }
            ?: throw IllegalArgumentException("Can't find bonded device with this id")
        scheduleConnect(flipperDeviceApi, device)
    }

    private suspend fun findAndConnectToDevice(
        context: Context,
        flipperDeviceApi: FlipperDeviceApi
    ) {
        if (!PermissionHelper.isPermissionGranted(context)) {
            throw SecurityException(
                """
                For connect to Flipper via bluetooth you need grant permission for you application. 
                Please, check PermissionHelper#checkPermissions
                """.trimIndent()
            )
        }

        val device = withTimeout(Constants.BLE.CONNECT_TIME_MS) {
            scanner.findFlipperById(flipperDeviceApi.address).first()
        }.device

        scheduleConnect(flipperDeviceApi, device)
    }
}