package com.example.nkwm87.bluetoothgatt;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class GattClient extends Service {

    String TAG = "BluetoothActivity";

    private static int NOTIFICATION_ID = 0;
    public static final ParcelUuid UUID = ParcelUuid.fromString("0000FED8-0000-1000-8000-00805F9B34FB");
    public static final java.util.UUID SERVICE_UUID = java.util.UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb07");
    public static final java.util.UUID CHAR_UUID = java.util.UUID.fromString("ff51b30e-d7e2-4d93-8842-a7c4a57dfb08");
    public static final java.util.UUID DES_UUID = java.util.UUID.fromString("00003333-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattServer server;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private boolean start;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Client is being created");

    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        Log.d(TAG, "Bluetooth is Setting Up");
        setupBluetooth();
        return Service.START_STICKY;
    }

    private void setupBluetooth(){
        BluetoothManager bluetoothManager = (BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        server = bluetoothManager.openGattServer(this, serverCallback);
        initServer();
        Log.d(TAG, "Server is initialized");
        bluetoothAdapter = bluetoothManager.getAdapter();
        Log.d(TAG, "Server is Advertised");
        advertise();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initServer(){
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(CHAR_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ|BluetoothGattCharacteristic.PERMISSION_WRITE);
        service.addCharacteristic(characteristic);
        server.addService(service);
    }

    private void advertise() {
        Log.d(TAG, "The Client is Advertising");
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        AdvertiseData advertisementData = getAdvertisementData();
        AdvertiseSettings advertisementSettings = getAdvertiseSettings();
        bluetoothLeAdvertiser.startAdvertising(advertisementSettings, advertisementData, advertiseCallback);
        start = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseData getAdvertisementData(){
        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.setIncludeTxPowerLevel(true);
        builder.addServiceUuid(UUID);
        bluetoothAdapter.setName("BLE Client");
        builder.setIncludeDeviceName(true);
        return builder.build();
       }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AdvertiseSettings getAdvertiseSettings(){
        AdvertiseSettings.Builder builder = new AdvertiseSettings.Builder();
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        builder.setConnectable(true);
        return builder.build();
    }

    private final AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @SuppressLint("Override")
        @Override
        public void onStartSuccess(AdvertiseSettings advertiseSettings) {
            final String message = "Advertisement successful";
            sendNotification(message);
        }

        @SuppressLint("Override")
        @Override
        public void onStartFailure(int errorCode) {
            final String message = "Advertisement failed error code: " + errorCode;
            sendNotification(message);
        }
    };

    private BluetoothGattServerCallback serverCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED){
                sendNotification("Client connected");
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            byte[] bytes = value;
            String message = new String(bytes);
            sendNotification(message);
            server.sendResponse(device, requestId, 0, offset, value);
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
        }
    };

    @Override
    public void onDestroy(){
        if (start){
            bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
        }
        super.onDestroy();
    }

    private void sendNotification(String message) {
        NotificationManager mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setContentTitle(getString(R.string.app_name)).setStyle(new NotificationCompat.BigTextStyle().bigText(message)).setAutoCancel(true).setContentText(message);
        Notification note = mBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;
        mNotificationManager.notify(NOTIFICATION_ID++, note);
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
    
}
