/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mupro.multi_ble;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();
    //public static String BLE_NUS_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    //public static String BLE_NUS_RX_CHARACTERISTIC = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    //public static String BLE_NUS_TX_CHARACTERISTIC = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
  //public static String BLE_SCALE_SERVICE = "237e0001";
    //public static String BLE_SCALE_SERVICE_NOTIFY = "237e0003";
    //public static String BLE_SCALE_SERVICE_WRITE = "237e0002";
    public static String SMARTEQUIT_SERVICE = "0000fff0";
    public static String SMARTEQUIT_SERVICE_NOTIFY = "0000fff4";
    public static String SMARTEQUIT_SERVICE_WRITE = "0000fff3";

    static {
        // Sample Services.
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Profile(GAP)");
        attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute Profile(GATT)");
        // Sample Characteristics.
        //attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("81211105-b295-a7bc-eb7c-3469045b5f3b", "Manufacturer Name String");
        
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name Characteristic");
        attributes.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance Characteristic");
        attributes.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag Characteristic");
        attributes.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address Characteristic");
        attributes.put("00002a04-0000-1000-8000-00805f9b34fb", "Peripheral Preferred Connection Parameters Characteristic");
        attributes.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed Characteristic");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
