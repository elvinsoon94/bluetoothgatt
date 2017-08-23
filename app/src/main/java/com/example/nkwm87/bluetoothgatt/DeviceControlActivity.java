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
    private ToggleButton LEDswitch;

    byte[] info_Data = null;

    private final String LIST_NAME = "NAME";
    private final String  LIST_UUID = "UUID";

    private final ServiceConnection mServiceConnection = new ServiceConnection(){

        /** Connect to the Selected Device **/
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

    /** Display the Connection Status and Data**/
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
                displayGattServices(mBluetoothLeService.getSupportedGattServices());    //display the available services
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){
                DisplayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));  //display any available data
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
                final int charaProp = characteristic.getProperties();   //obtaining the value of the properties of characteristics
                final String ledUUID = characteristic.getUuid().toString(); //Getting the UUID of the selected characteristics in term of string
                Log.d(TAG, "The UUID detected is:" +ledUUID);

                messageContainer = (ConstraintLayout)findViewById(R.id.message_container);  //layout for user to input data and read data
                mLedSwitch = (ConstraintLayout)findViewById(R.id.LED_Switch);   //layout for controlling the LED

                /** The LED control service is selected**/
                if (ledUUID.equals("00002a00-0000-1000-8000-00805f9b34fb")){

                    /** Turn off the writing layout if the layout is visible (WRITE / READ&WRITE Properties is selected before)**/
                    if (messageContainer.getVisibility() == View.VISIBLE) {
                        messageContainer.setVisibility(View.INVISIBLE);
                    }
                    Log.d(TAG, "LED Switch Service is Chosen");

                    mLedSwitch.setVisibility(View.VISIBLE);

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

                    /** Toggle button for the LED ON OFF switch **/
                    LEDswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked){
                                Log.d(TAG, "LED is turned on");
                                characteristic.setValue("ON");
                                mBluetoothLeService.writeCharacteristic(characteristic);
                            }else{
                                Log.d(TAG, "LED is turned off");
                                characteristic.setValue("OFF");
                                mBluetoothLeService.writeCharacteristic(characteristic);
                            }
                        }
                    });

                }else{

                /** Checking the properties of the characteristics **/

                if (charaProp < 5 ){    //this is READ properties (value is 2)
                    if (messageContainer.getVisibility() == View.VISIBLE){
                        messageContainer.setVisibility(View.INVISIBLE);
                    }

                    if (mLedSwitch.getVisibility() == View.VISIBLE){
                        mLedSwitch.setVisibility(View.INVISIBLE);
                    }

                    Log.d(TAG,"Permission to read");
                    mBluetoothLeService.readCharacteristic(characteristic);
                }

                else if (charaProp < 9){    //this is WRITE properties (value is 8)
                    if (mLedSwitch.getVisibility() == View.VISIBLE){
                        mLedSwitch.setVisibility(View.INVISIBLE);
                    }

                    Log.d(TAG,"Permission to write");
                    messageContainer.setVisibility(View.VISIBLE);
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

                else if (charaProp > 9){    //this is READ&WRITE properties (value is 10)
                    if (mLedSwitch.getVisibility() == View.VISIBLE){
                        mLedSwitch.setVisibility(View.INVISIBLE);
                    }

                    Log.d(TAG,"Permission to read and write");

                    messageContainer.setVisibility(View.VISIBLE);
                    Button sendData = (Button)findViewById(R.id.sendButton);    //write data into the characteristics
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

                    Button readData = (Button)findViewById(R.id.readButton);    //read date from the characteristics
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

    /**Update the Connection Status of the Device**/
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
        }
        });
    }

    /**Clear up the data field if the device is disconnected**/
    private void clearUI(){
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
        messageContainer.setVisibility(View.INVISIBLE);
        mLedSwitch.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        Log.d(TAG, "Displaying the Device information");
        final Intent intent = getIntent();

        /** Displaying the Information of the Connected Device **/
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

    /** Option Menu to Connect and Disconnect from the Device inside the UI**/
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

    /** Display the available services detected from the connected device **/
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

    /** Convert the value of the LED to string to check the condition **/
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
