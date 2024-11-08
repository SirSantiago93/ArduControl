package com.example.arducontrol;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
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

public class Registro extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword1;
    private EditText editTextPassword2;
    private Button buttonRegistrarse;

    private Button buttonBuscar;
    private Button buttonEditar;
    private Button buttonEliminar;

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword1 = findViewById(R.id.editTextPassword1);
        editTextPassword2 = findViewById(R.id.editTextPassword2);
        buttonRegistrarse = findViewById(R.id.buttonIngresar);

        buttonBuscar = findViewById(R.id.buttonBuscar);
        buttonEditar = findViewById(R.id.buttonEditar);
        buttonEliminar = findViewById(R.id.buttonEliminar);

        buttonRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertarUsuario("http://192.168.20.4/arducontrol_sql/insertar_usuario.php");
            }
        });

        buttonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringEmail = editTextEmail.getText().toString();
                String stringPassword1 = editTextPassword1.getText().toString();
                buscarUsuario("http://192.168.20.4/arducontrol_sql/buscar_usuario.php?email="+stringEmail.toLowerCase()+"&password="+stringPassword1);
            }
        });

        buttonEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editarUsuario("http://192.168.20.4/arducontrol_sql/editar_usuario.php");
            }
        });

        buttonEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminar_usuario("http://192.168.20.4/arducontrol_sql/eliminar_usuario.php");
            }
        });
    }

    public void insertarUsuario(String URL){

        String stringUsername = editTextUsername.getText().toString();
        String stringEmail = editTextEmail.getText().toString();
        String stringPassword1 = editTextPassword1.getText().toString();
        String stringPassword2 = editTextPassword2.getText().toString();

        if (stringUsername.isEmpty() || stringEmail.isEmpty() || stringPassword1.isEmpty() || stringPassword2.isEmpty()){
            Toast.makeText(getApplicationContext(), "Campos obligatorios vacios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!stringPassword1.equals(stringPassword2)){
            Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

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
                parametros.put("username", stringUsername);
                parametros.put("email", stringEmail.toLowerCase());
                parametros.put("password", stringPassword1);
                return parametros;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void buscarUsuario(String URL){

        String stringEmail = editTextEmail.getText().toString();
        String stringPassword1 = editTextPassword1.getText().toString();
        String stringPassword2 = editTextPassword2.getText().toString();

        if (stringEmail.isEmpty() || stringPassword1.isEmpty() || stringPassword2.isEmpty()){
            Toast.makeText(getApplicationContext(), "Campos obligatorios vacios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!stringPassword1.equals(stringPassword2)){
            Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        Toast.makeText(getApplicationContext(), "Usuario encontrado", Toast.LENGTH_SHORT).show();
                        editTextUsername.setText(jsonObject.getString("username"));
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Usuario no encontrado", Toast.LENGTH_SHORT).show();
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

    public void editarUsuario(String URL){

        String stringUsername = editTextUsername.getText().toString();
        String stringEmail = editTextEmail.getText().toString();
        String stringPassword1 = editTextPassword1.getText().toString();
        String stringPassword2 = editTextPassword2.getText().toString();

        if (stringUsername.isEmpty() || stringEmail.isEmpty() || stringPassword1.isEmpty() || stringPassword2.isEmpty()){
            Toast.makeText(getApplicationContext(), "Campos obligatorios vacios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!stringPassword1.equals(stringPassword2)){
            Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

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
                params.put("username", stringUsername);
                params.put("email", stringEmail.toLowerCase());
                params.put("password", stringPassword1);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void eliminar_usuario(String URL){

        String stringEmail = editTextEmail.getText().toString();
        String stringPassword1 = editTextPassword1.getText().toString();
        String stringPassword2 = editTextPassword2.getText().toString();

        if (stringEmail.isEmpty() || stringPassword1.isEmpty() || stringPassword2.isEmpty()){
            Toast.makeText(getApplicationContext(), "Campos obligatorio vacios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!stringPassword1.equals(stringPassword2)){
            Toast.makeText(getApplicationContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

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
                cleanForm();
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
                params.put("email", stringEmail.toLowerCase());
                params.put("password", stringPassword1);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void cleanForm(){
        editTextUsername.setText("");
        editTextEmail.setText("");
        editTextPassword1.setText("");
        editTextPassword2.setText("");
    }

}