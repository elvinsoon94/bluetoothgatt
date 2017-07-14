package com.example.nkwm87.bluetoothgatt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;
import java.util.UUID;

import android.util.Log;

public class BluetoothLeService extends Service {

    String TAG = "BluetoothActivity";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED =0;
    private static final int STATE_CONNECTING =1;
    private static final int STATE_CONNECTED =2;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example/bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.d(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }else {
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic){
        final Intent intent = new Intent(action);

        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder{
        BluetoothLeService getService(){
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        close();
        return super.onUnbind(intent);
    }
    private final IBinder mBinder = new LocalBinder();

    public boolean initialize(){
        Log.d(TAG, "Initialize the Connection");

        if(mBluetoothManager == null){
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null){
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null){
            return false;
        }
        return true;
    }

    public boolean connect(final String address){
        if(mBluetoothAdapter == null || address == null){
            return false;
        }

        if(mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt!= null){
            if(mBluetoothGatt.connect()){
                mConnectionState = STATE_CONNECTING;
                return true;
            }else{
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(device == null){
            return false;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect(){
        if(mBluetoothAdapter == null || mBluetoothGatt == null){
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close(){
        if (mBluetoothGatt == null){
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }


    public void readCharacteristic(BluetoothGattCharacteristic characteristics){
        if(mBluetoothAdapter == null || mBluetoothGatt == null){
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristics);
    }


    public List<BluetoothGattService> getSupportedGattServices(){
        if(mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
