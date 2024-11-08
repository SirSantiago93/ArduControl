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

public class Control extends AppCompatActivity {

    private static final String TAG = "Home";
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 2;
    private BluetoothAdapter myBtAdapter; // Permite interactuar con el dispositivo BT
    private BluetoothSocket btSocket; // Canal de comunicación entre los dispositivos
    private BluetoothDevice selectedBtDevice; // Dispositivo bluetooth seleccionado de la lista
    private ConnectedThread myConnectionBT; // Permite envio y recepción asincronica de data
    private ArrayList<String> btDeviceNames = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;

    private TextView textViewAssembly, textViewState;
    private Button buttonBuscar, buttonConectarse, buttonOnL1, buttonOffL1, buttonOnL2, buttonOffL2, buttonDesconectarse;
    private Spinner listaDispositivos;
    private ImageView imageViewConfigurar;
    private MovableButton movableButtonPrueba;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_control);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bloquear la orientación del dispositivo
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        requestBluetoothConnectPermission(); // Permisos para conectarse via bluetooth
        requestLocationPermission(); // Permisos para buscar dispositivos bluetooth cercanos

        textViewState = findViewById(R.id.textViewState);
        textViewAssembly = findViewById(R.id.textViewAssembly);
        buttonBuscar = findViewById(R.id.buttonBuscar);
        buttonConectarse = findViewById(R.id.buttonConnect);
        buttonOnL1 = findViewById(R.id.buttonOnL1);
        buttonOffL1 = findViewById(R.id.buttonOffL1);
        buttonOnL2 = findViewById(R.id.buttonOnL2);
        buttonOffL2 = findViewById(R.id.buttonOffL2);
        buttonDesconectarse = findViewById(R.id.buttonDisconnect);
        listaDispositivos = findViewById(R.id.spinnerIds);
        imageViewConfigurar = findViewById(R.id.imageViewConfig);
        movableButtonPrueba = findViewById(R.id.mButtonTest);

        // Configurando la lista de dispositivos (spinner)
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, btDeviceNames);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listaDispositivos.setAdapter(deviceAdapter);

        // Guardar el dispositivo seleccionado en la lista
        listaDispositivos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedBtDevice = getBluetoothDeviceByName(btDeviceNames.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                selectedBtDevice = null;
            }
        });

        buttonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscarDispositivosBT();
            }
        });

        buttonConectarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conectarDispositivoBT();
            }
        });

        buttonDesconectarse.setOnClickListener(new View.OnClickListener() {
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

        movableButtonPrueba.setOnClickListener(new View.OnClickListener() {
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

        imageViewConfigurar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                movableButtonPrueba.setIsBlocked(!movableButtonPrueba.getIsBlocked());
                if (movableButtonPrueba.getIsBlocked()){
                    showFastToast("Bloqueado");
                } else {
                    showFastToast("Desbloqueado");
                }
            }
        });
    }

    // Lanzador de peticiones de conexión
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == REQUEST_ENABLE_BT) {
                        Log.d(TAG, "Actividad registrada");
                        //Toast.makeText(getBaseContext(), "Activity registered", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // Obtener el nombre del dispositivo seleccionado
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

    // Buscar los dispositivos bluetooth emparejados
    public void buscarDispositivosBT() {
        myBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (myBtAdapter == null) {
            showToast("Bluetooth no disponible en el dispositivo");
            return;
        }

        if (!myBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            someActivityResultLauncher.launch(enableBtIntent);
        }

        Set<BluetoothDevice> pairedDevices = myBtAdapter.getBondedDevices();

        btDeviceNames.clear();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                btDeviceNames.add(device.getName());
            }
            deviceAdapter.notifyDataSetChanged();
        } else {
            showToast("No hay dispositivos bluetooth emparejados");
        }
    }

    //Respuesta a la petición de conexión bluetooth
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso concedido, función BLUETOOTH_CONNECT activa");
            } else {
                Log.d(TAG, "Permiso denegado");
            }
        }
    }

    // Conectarse al dispositivo bluetooth seleccionado
    private void conectarDispositivoBT() {
        if (selectedBtDevice == null) {
            showToast("Seleccione un dispositivo BT");
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
            showToast("Conexión exitosa");
            textViewState.setText("Estado: Conectado");
        } catch (IOException e) {
            showToast("Conexión fallida");
            textViewState.setText("Estado: Desconectado");
        }
    }

    // Clase para el hilo de conexión (Comunicación asincrona)
    private class ConnectedThread extends Thread {
        private final OutputStream mmOutStream;
        ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                showToast("Error creando el canal de datos");
            }
            mmOutStream = tmpOut;
        }
        public void write(char input) {
            //byte msgBuffer = (byte)input;
            try {
                mmOutStream.write((byte)input);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Conexión fallida", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
    }

    private void requestBluetoothConnectPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_CONNECT_PERMISSION);
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