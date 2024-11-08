package com.example.arducontrol;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Imported for bluetooth

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Home extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 2;
    private BluetoothAdapter myBtAdapter; // Allows interact with the BT device
    private BluetoothSocket btSocket; // Communication channel between 2 devices
    private BluetoothDevice selectedBtDevice;
    private ConnectedThread myConnectionBT; // Allows asynchronous sending and receiving of data
    private ArrayList<String> btDeviceNames = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;

    private TextView textViewAssembly, textViewState;
    private Button buttonSearch, buttonConnect, buttonOnL1, buttonOffL1, buttonOnL2, buttonOffL2, buttonDisconnect;
    private Spinner spinnerDevices;
    private ImageView imageViewConfig;
    private MovableButton movableButtonTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.buttonBluetooth), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        requestBluetoothConnectPermission(); // Permissions to connect via bluetooth
        requestLocationPermission(); // Permissions to search near bluetooth devices

        textViewState = findViewById(R.id.textViewState);
        textViewAssembly = findViewById(R.id.textViewAssembly);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonOnL1 = findViewById(R.id.buttonOnL1);
        buttonOffL1 = findViewById(R.id.buttonOffL1);
        buttonOnL2 = findViewById(R.id.buttonOnL2);
        buttonOffL2 = findViewById(R.id.buttonOffL2);
        buttonDisconnect = findViewById(R.id.buttonDisconnect);
        spinnerDevices = findViewById(R.id.spinnerIds);
        imageViewConfig = findViewById(R.id.imageViewConfig);
        movableButtonTest = findViewById(R.id.mButtonTest);

        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, btDeviceNames);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDevices.setAdapter(deviceAdapter);

        // Assign the select device
        spinnerDevices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedBtDevice = getBluetoothDeviceByName(btDeviceNames.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedBtDevice = null;
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBtDevices();
            }
        });

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectBtDevice();
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSocket != null)
                {
                    try {
                        btSocket.close();
                        myConnectionBT = null;
                        Toast.makeText(getBaseContext(), "Device disconnected", Toast.LENGTH_SHORT).show();
                        textViewState.setText("State: Disconnected");
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Error: No disconnected", Toast.LENGTH_SHORT).show();
                    }
                }
                //finish();
            }
        });

        buttonOnL1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myConnectionBT == null){
                    return;
                }
                //Toast.makeText(getBaseContext(),"LUZ 1 ON",Toast.LENGTH_SHORT).show();
                showFastToast("L1 ON");
                myConnectionBT.write('1');
            }
        });

        buttonOffL1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myConnectionBT == null){
                    return;
                }
                //Toast.makeText(getBaseContext(),"LUZ 1 OFF",Toast.LENGTH_SHORT).show();
                showFastToast("L1 OFF");
                myConnectionBT.write('2');
            }
        });

        buttonOnL2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myConnectionBT == null){
                    return;
                }
                //Toast.makeText(getBaseContext(),"LUZ 2 ON",Toast.LENGTH_SHORT).show();
                showFastToast("L2 ON");
                myConnectionBT.write('3');
            }
        });

        buttonOffL2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myConnectionBT == null){
                    return;
                }
                //Toast.makeText(getBaseContext(),"LUZ 2 OFF",Toast.LENGTH_SHORT).show();
                showFastToast("L2 OFF");
                myConnectionBT.write('4');
            }
        });

        movableButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myConnectionBT == null){
                    return;
                }
                //Toast.makeText(getBaseContext(),"LUZ 1 ON",Toast.LENGTH_SHORT).show();
                showFastToast("L1 ON");
                myConnectionBT.write('1');
            }
        });

        imageViewConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movableButtonTest.setIsBlocked(!movableButtonTest.getIsBlocked());
            }
        });
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == REQUEST_ENABLE_BT) {
                        Log.d(TAG, "Activity registered");
                        //Toast.makeText(getBaseContext(), "Activity registered", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private BluetoothDevice getBluetoothDeviceByName(String deviceSelected) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, " ----->>>>> ActivityCompat.checkSelfPermission");
        }
        Set<BluetoothDevice> pairedDevices = myBtAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(deviceSelected)) {
                return device;
            }
        }
        return null;
    }

    public void searchBtDevices() {
        myBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (myBtAdapter == null) {
            showToast("Bluetooth no available in this device");
            //finish();
            return;
        }

        if (!myBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            someActivityResultLauncher.launch(enableBtIntent);
        }

        // Get bluetooth devices already paired with the phone
        Set<BluetoothDevice> pairedDevices = myBtAdapter.getBondedDevices();

        btDeviceNames.clear();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                btDeviceNames.add(device.getName());
            }
            deviceAdapter.notifyDataSetChanged();
        } else {
            showToast("No bluetooth devices paired");
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
    }

    private void requestBluetoothConnectPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission granted, you can use BLUETOOTH_CONNECT features");
            } else {
                Log.d(TAG, "Permit denied");
            }
        }
    }

    private void connectBtDevice() {
        if (selectedBtDevice == null) {
            showToast("Select a BT device");
            return;
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            btSocket = selectedBtDevice.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
            btSocket.connect();
            myConnectionBT = new ConnectedThread(btSocket);
            myConnectionBT.start();
            showToast("Succesful connection");
            textViewState.setText("State: Connected");
        } catch (IOException e) {
            showToast("Error: Device not connected");
            textViewState.setText("State: Disconnected");
        }
    }

    private class ConnectedThread extends Thread { // Thread allows the asyncronous communication
        private final OutputStream mmOutStream;
        ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                showToast("Error creating the data stream");
            }
            mmOutStream = tmpOut;
        }
        public void write(char input) {
            //byte msgBuffer = (byte)input;
            try {
                mmOutStream.write((byte)input);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Connection failed", Toast.LENGTH_LONG).show();
                //finish();
            }
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFastToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 250);
    }
}
