package com.example.arducontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

import java.util.HashMap;
import java.util.Map;

public class Home extends AppCompatActivity {

    private String baseURL = TempData.url;

    private LinearLayout containerLayout;
    RequestQueue requestQueue;
    private Button buttonCreateAssembly;
    private Button buttonExit;
    private ImageView imageViewInfo;

    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        containerLayout = findViewById(R.id.containerLayout);
        buttonCreateAssembly = findViewById(R.id.buttonNewAssembly);
        buttonExit = findViewById(R.id.buttonExit);
        imageViewInfo = findViewById(R.id.imageViewInfo);
        builder = new AlertDialog.Builder(this);

        buttonCreateAssembly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewAssemblyForm();
            }
        });

        imageViewInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Info.class);
                startActivity(intent);
            }
        });

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
                finish();
            }
        });

        searchAssemblies(baseURL + "/home/searchAssemblies.php?email="+ TempData.getEmail());
    }

    private void searchAssemblies(String URL){

        containerLayout.removeAllViews();

        final String[] title = new String[1];
        final String[] date = new String[1];
        final String[] version = new String[1];

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        title[0] = jsonObject.getString("title");
                        date[0] = jsonObject.getString("dates");
                        version[0] = jsonObject.getString("version");
                        createBlock(title[0], date[0], version[0]);
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "No se encontraron ensambles", Toast.LENGTH_SHORT).show();
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

    public void createBlock(String title, String date, String version){

        //Bloque (Layout)
        LinearLayout blockLayout = new LinearLayout(this);
        blockLayout.setOrientation(LinearLayout.HORIZONTAL);
        blockLayout.setBackgroundColor(Color.parseColor("#787575"));
        blockLayout.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(16, 16, 16, 16);
        layoutParams.weight = 1;
        blockLayout.setLayoutParams(layoutParams);

        // Boton
        Button newButton = new Button(this);
        newButton.setText("X");
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        float density = getResources().getDisplayMetrics().density; // Obtén la densidad de la pantalla
        buttonParams.width = (int) (50 * density);
        newButton.setLayoutParams(buttonParams);
        blockLayout.addView(newButton);

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.setMessage("¿Quiere eliminar el ensamble?")
                        .setCancelable(false)
                        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                containerLayout.removeView(blockLayout);
                                deleteAssembly(baseURL +"/home/deleteAssembly.php", title);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        // Text view
        TextView newTextView = new TextView(this);
        newTextView.setText(String.format("  Titulo: %s\n  Fecha: %s\n  Versión: %s", title, date, version)); // newTextView.setText("Titulo: Ensamble de prueba \n Fecha: 11/10/2024 \n Versión: 1.0");
        newTextView.setTextSize(18);
        newTextView.setTextColor(Color.BLACK);
        newTextView.setBackgroundColor(Color.parseColor("#D3CECE"));
        blockLayout.addView(newTextView);
        newTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, Control.class);
                TempData.setTitle(title);
                TempData.setVersion(version);
                startActivity(intent);
            }
        });
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textViewParams.weight = 1;
        newTextView.setLayoutParams(textViewParams);

        containerLayout.addView(blockLayout);
    }

    private void showNewAssemblyForm() {
        View formularioView = getLayoutInflater().inflate(R.layout.new_assembly_form, null);

        EditText editTextTitle = formularioView.findViewById(R.id.editTextTitle);
        EditText editTextDate = formularioView.findViewById(R.id.editTextDate);
        EditText editTextVersion = formularioView.findViewById(R.id.editTextVersion);
        Button buttonSave = formularioView.findViewById(R.id.buttonSave);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(formularioView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = editTextTitle.getText().toString();
                String date = editTextDate.getText().toString();
                String version = editTextVersion.getText().toString();
                String URL = baseURL + "/home/insertAssembly.php";

                if (title.isEmpty() || date.isEmpty() || version.isEmpty()){
                    Toast.makeText(Home.this, "Error: Campos requeridos vacios", Toast.LENGTH_SHORT).show();
                } else{
                    insertAssembly(URL, title, date, version);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            searchAssemblies(baseURL + "/home/searchAssemblies.php?email=" + TempData.getEmail());
                        }
                    }, 100);
                }
                dialog.dismiss();
            }
        });
    }

    public void insertAssembly(String URL, String title, String date, String version){

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
                //Log.e("LogInsertar",error.toString());
            }
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                parametros.put("title", title);
                parametros.put("date", date);
                parametros.put("version", version);
                parametros.put("email", TempData.getEmail());

                JSONArray buttonsArray = new JSONArray();
                for (char c = 'A'; c <= 'U'; c++) {
                    JSONObject button = new JSONObject();
                    try {
                        button.put("name", "BTN_" + c);
                        button.put("character", String.valueOf(c));
                        button.put("position_x", 0);
                        button.put("position_y", 0);
                        button.put("visibility", true);
                        buttonsArray.put(button);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                parametros.put("buttons", buttonsArray.toString());

                return parametros;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void deleteAssembly(String URL, String title){
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
                params.put("email", TempData.getEmail());
                params.put("title", title);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}