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

public class Register extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword1;
    private EditText editTextPassword2;
    private Button buttonRegister;

    private Button buttonSearch;
    private Button buttonEdit;
    private Button buttonDelete;

    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.buttonBluetooth), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword1 = findViewById(R.id.editTextPassword1);
        editTextPassword2 = findViewById(R.id.editTextPassword2);
        buttonRegister = findViewById(R.id.buttonLogin);

        buttonSearch = findViewById(R.id.buttonSearch);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertUser("http://192.168.20.4/arducontrol_sql/insertUser.php");
            }
        });

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stringEmail = editTextEmail.getText().toString();
                String stringPassword1 = editTextPassword1.getText().toString();
                searchUser("http://192.168.20.4/arducontrol_sql/searchUser.php?email="+stringEmail.toLowerCase()+"&password="+stringPassword1);
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editUser("http://192.168.20.4/arducontrol_sql/editUser.php");
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser("http://192.168.20.4/arducontrol_sql/deleteUser.php");
            }
        });

    }

    public void insertUser(String URL){

        String stringUsername = editTextUsername.getText().toString();
        String stringEmail = editTextEmail.getText().toString();
        String stringPassword1 = editTextPassword1.getText().toString();
        String stringPassword2 = editTextPassword2.getText().toString();

        if (stringUsername.isEmpty() || stringEmail.isEmpty() || stringPassword1.isEmpty() || stringPassword2.isEmpty()){
            Toast.makeText(getApplicationContext(), "ERROR: Empty fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!stringPassword1.equals(stringPassword2)){
            Toast.makeText(getApplicationContext(), "ERROR: Different passwords", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "ERROR: " + message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void searchUser(String URL){

        String stringEmail = editTextEmail.getText().toString();
        String stringPassword1 = editTextPassword1.getText().toString();
        String stringPassword2 = editTextPassword2.getText().toString();

        if (stringEmail.isEmpty() || stringPassword1.isEmpty() || stringPassword2.isEmpty()){
            Toast.makeText(getApplicationContext(), "ERROR: Empty fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!stringPassword1.equals(stringPassword2)){
            Toast.makeText(getApplicationContext(), "ERROR: Different passwords", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                JSONObject jsonObject = null;
                for (int i = 0; i < response.length(); i++) {
                    try {
                        jsonObject = response.getJSONObject(i);
                        Toast.makeText(getApplicationContext(), "User found", Toast.LENGTH_SHORT).show();
                        editTextUsername.setText(jsonObject.getString("username"));
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    public void editUser(String URL){

        String stringUsername = editTextUsername.getText().toString();
        String stringEmail = editTextEmail.getText().toString();
        String stringPassword1 = editTextPassword1.getText().toString();
        String stringPassword2 = editTextPassword2.getText().toString();

        if (stringUsername.isEmpty() || stringEmail.isEmpty() || stringPassword1.isEmpty() || stringPassword2.isEmpty()){
            Toast.makeText(getApplicationContext(), "ERROR: Empty fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!stringPassword1.equals(stringPassword2)){
            Toast.makeText(getApplicationContext(), "ERROR: Different passwords", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "ERROR: " + message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void deleteUser(String URL){

        String stringEmail = editTextEmail.getText().toString();
        String stringPassword1 = editTextPassword1.getText().toString();
        String stringPassword2 = editTextPassword2.getText().toString();

        if (stringEmail.isEmpty() || stringPassword1.isEmpty() || stringPassword2.isEmpty()){
            Toast.makeText(getApplicationContext(), "ERROR: Empty fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!stringPassword1.equals(stringPassword2)){
            Toast.makeText(getApplicationContext(), "ERROR: Different passwords", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "ERROR: " + message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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