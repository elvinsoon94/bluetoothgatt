package com.example.nkwm87.bluetoothgatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = "BluetoothActivity";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private BluetoothLeService mBluetoothLeService;
    private ExpandableListView mGattServicesList;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean mConnected = false;
    private ConstraintLayout messageContainer;
    private ConstraintLayout mLedSwitch;
    private int writeMessage = 0;
    private int ledSwitch = 0;
    private int ledon = 0;
    private int ledoff = 0;
    private ToggleButton LEDswitch;

    byte[] info_Data = null;

    private final String LIST_NAME = "NAME";
    private final String  LIST_UUID = "UUID";

    private final ServiceConnection mServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.d(TAG, "Established Connection");
            if(!mBluetoothLeService.initialize()){
                Log.d(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connection is Successful");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
                mConnected  = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            }else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){
                DisplayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }

        }
    };

    private final ExpandableListView.OnChildClickListener servicesListClickListener = new ExpandableListView.OnChildClickListener() {

        /** value for the charapop 2=read 8=write 10=read and write **/

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            if(mGattCharacteristics != null){
                final boolean switchIson = true;
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                final String ledUUID = characteristic.getUuid().toString();
                Log.d(TAG, "The UUID detected is:" +ledUUID);

                if (ledUUID.equals("00002a00-0000-1000-8000-00805f9b34fb")){
                    Log.d(TAG, "Selection of the Service");

                    if(writeMessage==1) {
                        messageContainer.setVisibility(View.INVISIBLE);
                    }
                    if(ledSwitch==1){
                        mLedSwitch.setVisibility(View.INVISIBLE);
                    }

                    /*final ConstraintLayout mSelection = (ConstraintLayout)findViewById(R.id.functionSelectionLayout);
                    mSelection.setVisibility(View.VISIBLE);

                    Button mGPSButton = (Button)findViewById(R.id.gpsSelection);
                    mGPSButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSelection.setVisibility(View.INVISIBLE);
                            Log.d(TAG, "GPS service is chosen");
                            if(ledSwitch==1){
                                mLedSwitch.setVisibility(View.INVISIBLE);
                            }

                            messageContainer = (ConstraintLayout)findViewById(R.id.message_container);
                            messageContainer.setVisibility(View.VISIBLE);
                            writeMessage = 1;
                            Button sendData = (Button)findViewById(R.id.sendButton);
                            sendData.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final TextView message = (TextView)findViewById(R.id.dataMessage);
                                    characteristic.setValue(message.getText().toString().getBytes());
                                    Log.d(TAG, "The apps is writing the characteristics");
                                    mBluetoothLeService.writeCharacteristic(characteristic);
                                    message.setText("");
                                    Log.d(TAG, "New Data is Written:" +characteristic);
                                }
                            });
                        }
                    });*/

                    //Button mLEDButton = (Button)findViewById(R.id.ledSelection);
                    //mLEDButton.setOnClickListener(new View.OnClickListener() {
                        //@Override
                        //public void onClick(View v) {
                            //mSelection.setVisibility(View.INVISIBLE);
                            Log.d(TAG, "LED Switch Service is Chosen");
                            /*if(writeMessage==1) {
                                messageContainer.setVisibility(View.INVISIBLE);
                            }

                            if (ledon==1 && ledoff==0){
                                characteristic.setValue("ON");
                                mBluetoothLeService.writeCharacteristic(characteristic);
                            }
                            if (ledoff==1 && ledon==0){
                                characteristic.setValue("OFF");
                                mBluetoothLeService.writeCharacteristic(characteristic);
                            }*/

                            mLedSwitch = (ConstraintLayout)findViewById(R.id.LED_Switch);
                            mLedSwitch.setVisibility(View.VISIBLE);

                            ledSwitch = 1;

                            ToggleButton LEDswitch = (ToggleButton)findViewById(R.id.LEDSwitchButton);
                            mBluetoothLeService.readCharacteristic(characteristic);

                            /** Checking the condition of the LED before adding additional command **/
                            String status;
                            byte[] LEDdata = characteristic.getValue();

                            if (LEDdata != null && LEDdata.length > 0){
                                final StringBuilder stringBuilder = new StringBuilder(LEDdata.length);
                                for (byte byteChar : LEDdata)
                                    stringBuilder.append(String.format("%02X", byteChar));
                                status = stringBuilder.toString();
                                String printstatus = convertHexToString(status);
                                Log.d(TAG, "The status of the LED is**" +printstatus);

                                if(printstatus.equals("ON")){
                                    Log.d(TAG, "The LED is already turned on");
                                    LEDswitch.setChecked(switchIson);
                                }
                            }

                            LEDswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked){
                                        Log.d(TAG, "LED is turned on");
                                        characteristic.setValue("ON");
                                        mBluetoothLeService.writeCharacteristic(characteristic);
                                        ledon = 1;
                                        ledoff = 0;
                                    }else{
                                        Log.d(TAG, "LED is turned off");
                                        characteristic.setValue("OFF");
                                        mBluetoothLeService.writeCharacteristic(characteristic);
                                        ledon = 0;
                                        ledoff = 1;
                                    }
                                }
                            });

                        //}
                    //});

                }else{

                /** Checking the properties value of the characteristics **/

                if (charaProp < 5 ){
                    if(writeMessage==1) {
                        messageContainer.setVisibility(View.INVISIBLE);
                    }
                    else if(ledSwitch==1){
                        mLedSwitch.setVisibility(View.INVISIBLE);
                    }

                    Log.d(TAG,"Permission to read");
                    mBluetoothLeService.readCharacteristic(characteristic);
                    writeMessage = 0;
                }

                else if (charaProp < 9){
                    if(ledSwitch==1){
                        mLedSwitch.setVisibility(View.INVISIBLE);
                    }
                    Log.d(TAG,"Permission to write");

                    messageContainer = (ConstraintLayout)findViewById(R.id.message_container);
                    messageContainer.setVisibility(View.VISIBLE);
                    writeMessage = 1;
                    Button sendData = (Button)findViewById(R.id.sendButton);
                    sendData.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final TextView message = (TextView)findViewById(R.id.dataMessage);
                            characteristic.setValue(message.getText().toString().getBytes());
                            Log.d(TAG, "The apps is writing the characteristics");
                            mBluetoothLeService.writeCharacteristic(characteristic);
                            message.setText("");
                            Log.d(TAG, "New Data is Written:" +characteristic);
                        }
                    });
                }

                else if (charaProp > 9){
                    if(ledSwitch==1){
                        Log.d(TAG, "LED Switch layout is turned off");
                        mLedSwitch.setVisibility(View.INVISIBLE);
                    }
                    Log.d(TAG,"Permission to read and write");


                    messageContainer = (ConstraintLayout)findViewById(R.id.message_container);
                    messageContainer.setVisibility(View.VISIBLE);
                    writeMessage = 1;
                    Button sendData = (Button)findViewById(R.id.sendButton);
                    sendData.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final TextView message = (TextView)findViewById(R.id.dataMessage);
                            characteristic.setValue(message.getText().toString().getBytes());
                            Log.d(TAG, "The apps is writing the characteristics");
                            mBluetoothLeService.writeCharacteristic(characteristic);
                            message.setText("");
                        }
                    });

                    Button readData = (Button)findViewById(R.id.readButton);
                    readData.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                    });
                }}
                return true;
            }
            return false;
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
        }
        });
    }

    private void clearUI(){
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
        if(writeMessage == 1){
            messageContainer.setVisibility(View.INVISIBLE);
        }
        if(ledSwitch == 1){
            mLedSwitch.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        Log.d(TAG, "Displaying the Device information");
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        TextView devicename = (TextView)findViewById(R.id.device_name);
        devicename.setText(mDeviceName);
        TextView deviceAddress = (TextView)findViewById(R.id.device_address);
        deviceAddress.setText(mDeviceAddress);

        mConnectionState = (TextView)findViewById(R.id.device_status);
        mDataField = (TextView)findViewById(R.id.device_data);


        mGattServicesList = (ExpandableListView)findViewById(R.id.gatt_service_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListener);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if(mBluetoothLeService != null){
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect Request Result="+result);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if(mConnected){
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        }else{
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void DisplayData (String data){
        if(data != null){
            mDataField.setText(data);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices){
        if(gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        for (BluetoothGattService gattService : gattServices){
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            for(BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics){
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this, gattServiceData, android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID}, new int[] {android.R.id.text1, android.R.id.text2},
                gattCharacteristicData, android.R.layout.simple_expandable_list_item_2, new String[] {LIST_NAME, LIST_UUID},
                new int[] {android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public String convertHexToString(String hex){
        StringBuffer sb = new StringBuffer();
        StringBuilder temp = new StringBuilder();

        for (int i =0; i<hex.length()-1; i+=2){
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);

            temp.append(decimal);
        }
        return sb.toString();
    }

}
