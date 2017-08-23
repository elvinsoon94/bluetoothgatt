package com.example.nkwm87.bluetoothgatt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.example.nkwm87.bluetoothgatt.R.id.devicename_list;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private static int NOTIFICATION_ID = 0;

    private static final int REQUEST_ENABLE_BT = 1;
    private Button StartButton;
    private Button StopButton;
    private ProgressBar refreshbar;
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

        /** Checking the device supporting BLE or not **/
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLUETOOTH_LE not supported in this device!", Toast.LENGTH_SHORT).show();
            finish();
        }

        getBluetoothAndLeScanner();

        if (mBluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Bluetooth Adapter is not Available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button ScanButton = (Button)findViewById(R.id.ScanButton);
        ScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Start Scanning");
                startLeScan(true);
            }
        });

        /** Putting the detected device in an array list and display it **/
        BLEListView = (ListView) findViewById(devicename_list);
        listBluetoothDevice = new ArrayList<>();
        adapterScanResult = new ArrayAdapter<BluetoothDevice>(MainActivity.this, android.R.layout.simple_list_item_1, listBluetoothDevice);
        BLEListView.setAdapter((adapterScanResult));
        BLEListView.setOnItemClickListener(scanDeviceOnItemClickListener);

        mHandler = new Handler();


        Button StartButton = (Button)findViewById(R.id.startbutton);
        Button StopButton = (Button)findViewById(R.id.StopButton);

        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Becoming Client and Start Advertising");
                start();
            }
        });


        StopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Stop Advertising Process");
                stopService(new Intent(MainActivity.this, GattClient.class));
                cancelNotification();
            }
        });
    }

    /** The advertise process **/
    private void start(){
        startService(new Intent(this, GattClient.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /** Ask the user to enable Bluetooth if it is not being enabled **/
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    /** If the Bluetooth is not enabled by the user the application will closed **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED){
            finish();
            return;
        }
        getBluetoothAndLeScanner();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** Obtain the Bluetooth Adapter and Scanner **/
    private void getBluetoothAndLeScanner(){
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void startLeScan(final boolean enable){
        final ProgressBar refreshbar = (ProgressBar)findViewById(R.id.refreshbar);
        if(enable){
            listBluetoothDevice.clear();
            BLEListView.invalidateViews();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Scanning Process Timeout");
                    refreshbar.setVisibility(View.INVISIBLE);
                    mBluetoothLeScanner.stopScan(mLeScanCallBack);
                    BLEListView.invalidateViews();
                }
            }, SCAN_PERIOD);

            mBluetoothLeScanner.startScan(mLeScanCallBack); //Scanning process started and will last for 10 seconds
            Log.d(TAG, "Scanning in Progree");
            refreshbar.setVisibility(View.VISIBLE); //refresh bar as the indicator for the scanning process
        }else{
            mBluetoothLeScanner.stopScan(mLeScanCallBack);
        }
    }

    /** ScanCallBack for the Scanning result from the process **/
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
            Log.d(TAG, "Scanning Failed");
            super.onScanFailed(errorCode);
        }

        private void addBluetoothDevice(BluetoothDevice device){
            if (!listBluetoothDevice.contains(device)){
                listBluetoothDevice.add(device);
                BLEListView.invalidateViews();
            }

        }
    };

    AdapterView.OnItemClickListener scanDeviceOnItemClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            mBluetoothLeScanner.stopScan(mLeScanCallBack);
            final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);

            String message = "Device Name: "+ device.getName() + "\nMAC Address: " + device.getAddress();
            new AlertDialog.Builder(MainActivity.this).setTitle("Established Connection")
                    .setMessage(message).setPositiveButton("Connect", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    final Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                    startActivity(intent);  //will be directed to DeviceControlActivity together with the device name and MAC address
                    Log.d(TAG, "Connecting to " + device.getName());
                }
            }).show();
        }
    };

    private void cancelNotification() {
        final NotificationManager cancel = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        cancel.cancel(NOTIFICATION_ID);
        NOTIFICATION_ID++;
    }

}