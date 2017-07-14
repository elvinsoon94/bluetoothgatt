package com.example.nkwm87.bluetoothgatt;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = "Bluetooth Activity";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName;
    private String mDeviceAddress;

    private final String LIST_NAME = "NAME";
    private final String  LIST_UUID = "UUID";

    private final ServiceConnection mServiceConnection = new ServiceConnection(){


        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if(!mBluetoothLeService.initialize()){
                Log.d(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        TextView devicename = (TextView)findViewById(R.id.device_name);
        devicename.setText(mDeviceName);
        TextView deviceAddress = (TextView)findViewById(R.id.device_adress);
        deviceAddress.setText(mDeviceAddress);
    }
}
