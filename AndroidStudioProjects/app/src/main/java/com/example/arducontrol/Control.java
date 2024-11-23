package com.example.arducontrol;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Control extends AppCompatActivity {

    private String baseURL = TempData.url;

    private static final String TAG = "Control";
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
    private Button buttonSearch, buttonConnect, buttonDisconnect;
    private Spinner deviceList;
    private ImageView imageViewConfig, imageViewSave;

    RequestQueue requestQueue;
    public ArrayList<MovileButton> buttonList = new ArrayList<>();

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

        // Añadiendo los botones a la lista
        buttonList.add(findViewById(R.id.mButtonA));
        buttonList.add(findViewById(R.id.mButtonB));
        buttonList.add(findViewById(R.id.mButtonC));
        buttonList.add(findViewById(R.id.mButtonD));
        buttonList.add(findViewById(R.id.mButtonE));
        buttonList.add(findViewById(R.id.mButtonF));
        buttonList.add(findViewById(R.id.mButtonG));
        buttonList.add(findViewById(R.id.mButtonH));
        buttonList.add(findViewById(R.id.mButtonI));
        buttonList.add(findViewById(R.id.mButtonJ));
        buttonList.add(findViewById(R.id.mButtonK));
        buttonList.add(findViewById(R.id.mButtonL));
        buttonList.add(findViewById(R.id.mButtonM));
        buttonList.add(findViewById(R.id.mButtonN));
        buttonList.add(findViewById(R.id.mButtonO));
        buttonList.add(findViewById(R.id.mButtonP));
        buttonList.add(findViewById(R.id.mButtonQ));
        buttonList.add(findViewById(R.id.mButtonR));
        buttonList.add(findViewById(R.id.mButtonS));
        buttonList.add(findViewById(R.id.mButtonT));
        buttonList.add(findViewById(R.id.mButtonU));

        // Configurando atributos de los botones
        for (int i = 0; i < buttonList.size(); i++) {
            final int index = i; // Variable final para usar en el Runnable
            buttonList.get(i).post(new Runnable() {
                @Override
                public void run() {
                    int posX = (int) buttonList.get(index).getX();
                    int posY = (int) buttonList.get(index).getY();

                    buttonList.get(index).saveDefaultPositions(posX, posY);
                    buttonList.get(index).setCharacter((char)('A' + index));
                    buttonList.get(index).setName("BTN_" + (char)('A' + index));

                    buttonList.get(index).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (myConnectionBT == null){
                                return;
                            }
                            showFastToast(buttonList.get(index).getName());
                            myConnectionBT.write(buttonList.get(index).getCharacter());
                        }
                    });

                }
            });
        }

        textViewState = findViewById(R.id.textViewState);
        textViewAssembly = findViewById(R.id.textViewAssembly);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonDisconnect = findViewById(R.id.buttonDisconnect);
        imageViewConfig = findViewById(R.id.imageViewConfig);
        imageViewSave = findViewById(R.id.imageViewSave);
        deviceList = findViewById(R.id.spinnerIds);

        textViewAssembly.setText(String.format("Ensamble: %s v%s", TempData.getTitle(), TempData.getVersion()));

        // Configurando la lista de dispositivos (spinner)
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, btDeviceNames);
        deviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceList.setAdapter(deviceAdapter);

        // Guardar el dispositivo seleccionado en la lista
        deviceList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                searchDevicesBT();
            }
        });

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conectarDispositivoBT();
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
                        Toast.makeText(getBaseContext(), "Dispositivo desconectado", Toast.LENGTH_SHORT).show();
                        textViewState.setText("Estado: Desconectado");
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Error: No se puedo desconectar", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imageViewConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConfigButtonsForm(buttonList);
            }
        });

        imageViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateButtonsInfo(baseURL + "/control/updateButtonsInfo.php");
            }
        });

        checkIsConfig(baseURL + "/control/checkIsConfig.php?title=" + TempData.getTitle() + "&email=" + TempData.getEmail());
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
    public void searchDevicesBT() {
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

    //JSON buttons sql

    private void loadButtonsInfo(String URL){

        final Character[] character = new Character[1];

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONArray buttonsArray = response.getJSONArray(i);
                        for (int j = 0; j < buttonsArray.length(); j++) {
                            JSONObject buttonJSON = buttonsArray.getJSONObject(j);

                            String name = buttonJSON.getString("name");
                            char character = buttonJSON.getString("character").charAt(0);
                            int posX = buttonJSON.getInt("position_x");
                            int posY = buttonJSON.getInt("position_y");
                            boolean visibility = buttonJSON.getBoolean("visibility");

                            buttonList.get(j).setName(name);
                            buttonList.get(j).setText(name);
                            buttonList.get(j).setCharacter(character);
                            buttonList.get(j).setPositionX(posX);
                            buttonList.get(j).setPositionY(posY);
                            buttonList.get(j).setIsVisible(visibility);
                            buttonList.get(j).moveToConfiguredPosition();
                        }
                        //Toast.makeText(getApplicationContext(), "Botones cargados correctamente", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "No se encontraron botones", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    public void updateButtonsInfo(String URL){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    if (success) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("title", TempData.getTitle());
                params.put("email", TempData.getEmail());

                JSONArray buttonsArray = new JSONArray();
                for(int i = 0; i < buttonList.size(); i++){
                    JSONObject button = new JSONObject();
                    try {
                        button.put("name", buttonList.get(i).getName());
                        button.put("character", String.valueOf(buttonList.get(i).getCharacter()));
                        button.put("position_x", buttonList.get(i).getPositionX());
                        button.put("position_y", buttonList.get(i).getPositionY());
                        button.put("visibility", buttonList.get(i).getIsVisible());
                        buttonsArray.put(button);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                params.put("buttons", buttonsArray.toString());
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void checkIsConfig(String URL){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        if (jsonObject.getString("is_config").equals("1")){
                            loadButtonsInfo(baseURL + "/control/loadButtonsInfo.php?title=" + TempData.getTitle() + "&email=" + TempData.getEmail());
                        } else {
                            // Cargar atributos por default
                            updateButtonsInfo(baseURL + "/control/updateButtonsInfo.php");
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "is_config no encontrado", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    // config buttons

    private void showConfigButtonsForm(ArrayList<MovileButton> buttonList ) {

        final String[] selectedButtonName = new String[1];
        final MovileButton[] buttonSelected = new MovileButton[1];

        View formularioView = getLayoutInflater().inflate(R.layout.config_buttons_form, null);

        Spinner spinnerButtons = formularioView.findViewById(R.id.spinnerButtons);
        Switch switchLock = formularioView.findViewById(R.id.switchLock);
        Button buttonHide = formularioView.findViewById(R.id.buttonHide);
        Button buttonShow = formularioView.findViewById(R.id.buttonShow);
        Switch switchVisible = formularioView.findViewById(R.id.switchVisible);
        EditText editTextName = formularioView.findViewById(R.id.editTextName);
        EditText editTextCharacter = formularioView.findViewById(R.id.editTextCharacter);
        Button buttonSave = formularioView.findViewById(R.id.buttonSave);

        switchLock.setChecked(MovileButton.getIsBlocked());

        ArrayList<String> buttonNames = new ArrayList<>();
        for (MovileButton button : buttonList) {
            buttonNames.add(button.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, buttonNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerButtons.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(formularioView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        spinnerButtons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedButtonName[0] = buttonNames.get(position);
                buttonSelected[0] = buttonList.get(position);
                editTextName.setText(buttonSelected[0].getName());
                editTextCharacter.setText(String.valueOf(buttonSelected[0].getCharacter()));
                switchVisible.setChecked(buttonSelected[0].getIsVisible());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        buttonShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < buttonList.size(); i++){
                    buttonList.get(i).setIsVisible(true);
                }
            }
        });

        buttonHide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < buttonList.size(); i++){
                    buttonList.get(i).setIsVisible(false);
                }
            }
        });

        switchLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    MovileButton.setIsBlocked(true);
                    Toast.makeText(Control.this, "Bloqueado", Toast.LENGTH_SHORT).show();
                } else {
                    MovileButton.setIsBlocked(false);
                    Toast.makeText(Control.this, "Desbloqueado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTextName.getText().toString().length() < 5){
                    buttonSelected[0].setName(editTextName.getText().toString());
                    buttonSelected[0].setText(editTextName.getText().toString());
                } else {
                    buttonSelected[0].setName(editTextName.getText().toString().substring(0,5));
                    buttonSelected[0].setText(editTextName.getText().toString().substring(0,5));
                }
                buttonSelected[0].setIsVisible(switchVisible.isChecked());
                buttonSelected[0].setCharacter(editTextCharacter.getText().toString().charAt(0));
                updateButtonsInfo(baseURL + "/updateButtonsInfo.php");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}