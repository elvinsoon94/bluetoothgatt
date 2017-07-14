package com.example.nkwm87.bluetoothgatt;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.example.nkwm87.bluetoothgatt.R.id.devicename_list;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothLeService mBluetoothLeService;
    String TAG = "BluetoothActivity";
    BluetoothAdapter mBluetoothAdapter ;
    BluetoothLeScanner mBluetoothLeScanner;

    BluetoothDevice device;
    ListView BLEListView;
    ListView BLENameListView;
    List<BluetoothDevice> listBluetoothDevice;
    ListAdapter adapterScanResult;
    ArrayAdapter<String> adapterScanResultName;
    ArrayList<String> DeviceName = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Started.");
        setContentView(R.layout.activity_main);

        getBluetoothAndLeScanner();
        Button ScanButton = (Button)findViewById(R.id.ScanButton);
        ScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Start Scanning");
                startLeScan(true);
            }
        });


        BLEListView = (ListView) findViewById(devicename_list);
        listBluetoothDevice = new ArrayList<>();
        adapterScanResult = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, listBluetoothDevice);
        BLEListView.setAdapter((adapterScanResult));
        BLEListView.setOnItemClickListener(scanDeviceOnItemClickListener);


        Button StopButton = (Button)findViewById(R.id.StopButton);
        StopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Stop Scanning");
                mBluetoothLeScanner.stopScan(mLeScanCallBack);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBluetoothLeScanner.stopScan(mLeScanCallBack);
        adapterScanResultName.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED){
            finish();
            return;
        }
        getBluetoothAndLeScanner();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getBluetoothAndLeScanner(){
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void startLeScan(final boolean enable){
        if(enable){
            mBluetoothLeScanner.startScan(mLeScanCallBack);
        }else{
            mBluetoothLeScanner.stopScan(mLeScanCallBack);
        }
    }


    private ScanCallback mLeScanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            addBluetoothDevice(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult result : results){
                addBluetoothDevice(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }

        private void addBluetoothDevice(BluetoothDevice device){
            if (!listBluetoothDevice.contains(device)){
                listBluetoothDevice.add(device);
                BLEListView.invalidateViews();
            }

        }

        private void addDeviceName (BluetoothDevice device){
            if (!listBluetoothDevice.contains(device)) {
                Log.d(TAG, "Displaying the Device Name");
                String deviceName = device.getName();
                if (deviceName != null) {
                    DeviceName.add(deviceName);
                    Log.d(TAG, "Device Name is " + deviceName);
                } else {
                    deviceName = "Unknown Device";
                    DeviceName.add(deviceName);
                }
                BLENameListView.invalidateViews();
            }
        }
    };

    AdapterView.OnItemClickListener scanDeviceOnItemClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            mBluetoothLeScanner.stopScan(mLeScanCallBack);
            final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);

            String message = "Device Name: "+ device.getName() + "\nMAC Address: " + device.getAddress();
            new AlertDialog.Builder(MainActivity.this).setTitle("Established Connection").setMessage(message).setPositiveButton("Connect", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    final Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    startActivity(intent);
                }
            }).show();
        }
    };

}