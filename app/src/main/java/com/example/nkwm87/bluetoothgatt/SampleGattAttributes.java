package com.example.nkwm87.bluetoothgatt;

import java.util.HashMap;


public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Sample Services
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("ff51b30e-d7e2-4d93-8842-a7c4a57dfb07", "Device Primary Service");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Attribute Profile");

        // Sample Characteristics
        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("ff51b30e-d7e2-4d93-8842-a7c4a57dfb08", "Input Characteristic");
        attributes.put("ff51b30e-d7e2-4d93-8842-a7c4a57dfb09", "Uptime Characteristic");
        attributes.put("ff51b30e-d7e2-4d93-8842-a7c4a57dfb10", "Load Average Characteristic");

        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Device Appearance");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Device Preferred Peripheral Connection");
        attributes.put("00002a08-0000-1000-8000-00805f9b34fb", "Device Data Transfer");
        attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level Characteristic");
        attributes.put("00002a25-0000-1000-8000-00805f9b34fb", "Device Serial Number");
        attributes.put("00002a24-0000-1000-8000-00805f9b34fb", "Device Model Number");
        attributes.put("00002a23-0000-1000-8000-00805f9b34fb", "Device System ID");
        attributes.put("00002a27-0000-1000-8000-00805f9b34fb", "Device Hardware Revision");
        attributes.put("00002a26-0000-1000-8000-00805f9b34fb", "Device Firmware Revision");
        attributes.put("00002a28-0000-1000-8000-00805f9b34fb", "Device Software Revision");
        attributes.put("00002a50-0000-1000-8000-00805f9b34fb", "Device PNP ID");
    }

    public static String lookup(String uuid, String defaultName){
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
