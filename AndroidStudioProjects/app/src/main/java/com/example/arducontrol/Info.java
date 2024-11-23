package com.example.arducontrol;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Info extends AppCompatActivity {

    private String baseURL = TempData.url;
    private TextView textViewUsername;
    private TextView textViewEmail;
    RequestQueue requestQueue;

    private Button buttonChangeUsername;
    private Button buttonChangeEmail;
    private Button buttonChangePassword;
    private Button buttonDeleteAccount;
    private Button buttonGoBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);

        textViewUsername.setText(TempData.getUsername());
        textViewEmail.setText(TempData.getEmail());

        buttonChangeUsername = findViewById(R.id.buttonChangeUsername);
        buttonChangeEmail = findViewById(R.id.buttonChangeEmail);
        buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        buttonGoBack = findViewById(R.id.buttonGoBack);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        buttonChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeUsernameForm();
            }
        });

        buttonChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeEmailForm();
            }
        });

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordForm();
            }
        });

        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteAccountForm();
            }
        });

        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
                finish();
            }
        });

    }

    private void showChangeUsernameForm() {
        View formularioView = getLayoutInflater().inflate(R.layout.change_username_form, null);

        EditText editTextUsername = formularioView.findViewById(R.id.editTextUsername);
        EditText editTextPassword1 = formularioView.findViewById(R.id.editTextPassword1);
        EditText editTextPassword2 = formularioView.findViewById(R.id.editTextPassword2);
        Button buttonChange = formularioView.findViewById(R.id.buttonChange);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(formularioView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = editTextUsername.getText().toString();
                String password1 = editTextPassword1.getText().toString();
                String password2 = editTextPassword2.getText().toString();
                String URL = baseURL + "/info/changeUsername.php";

                if (username.isEmpty() || password1.isEmpty() || password2.isEmpty()){
                    Toast.makeText(Info.this, "Error: Campos requeridos vacios", Toast.LENGTH_SHORT).show();
                } else if (!password1.equals(password2)) {
                    Toast.makeText(Info.this, "Error: Contraseñas diferentes", Toast.LENGTH_SHORT).show();
                } else {
                    changeUsername(URL, username, password1);
                }
                dialog.dismiss();
            }
        });

    }

    private void showChangeEmailForm() {
        View formularioView = getLayoutInflater().inflate(R.layout.change_email_form, null);

        EditText editTextEmail = formularioView.findViewById(R.id.editTextEmail);
        EditText editTextPassword1 = formularioView.findViewById(R.id.editTextPassword1);
        EditText editTextPassword2 = formularioView.findViewById(R.id.editTextPassword2);
        Button buttonChange = formularioView.findViewById(R.id.buttonChange);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(formularioView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password1 = editTextPassword1.getText().toString();
                String password2 = editTextPassword2.getText().toString();
                String URL = baseURL + "/info/changeEmail.php";

                if (email.isEmpty() || password1.isEmpty() || password2.isEmpty()){
                    Toast.makeText(Info.this, "Error: Campos requeridos vacios", Toast.LENGTH_SHORT).show();
                } else if (!password1.equals(password2)) {
                    Toast.makeText(Info.this, "Error: Contraseñas diferentes", Toast.LENGTH_SHORT).show();
                } else {
                    changeEmail(URL, email.toLowerCase(), password1);
                }
                dialog.dismiss();
            }
        });
    }

    private void showChangePasswordForm() {
        View formularioView = getLayoutInflater().inflate(R.layout.change_password_form, null);

        EditText editTextNewPassword = formularioView.findViewById(R.id.editTextNewPassword);
        EditText editTextPassword1 = formularioView.findViewById(R.id.editTextPassword1);
        EditText editTextPassword2 = formularioView.findViewById(R.id.editTextPassword2);
        Button buttonChange = formularioView.findViewById(R.id.buttonChange);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(formularioView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String new_password = editTextNewPassword.getText().toString();
                String password1 = editTextPassword1.getText().toString();
                String password2 = editTextPassword2.getText().toString();
                String URL = baseURL + "/info/changePassword.php";

                if (new_password.isEmpty() || password1.isEmpty() || password2.isEmpty()){
                    Toast.makeText(Info.this, "Error: Campos requeridos vacios", Toast.LENGTH_SHORT).show();
                } else if (!password1.equals(password2)) {
                    Toast.makeText(Info.this, "Error: Contraseñas diferentes", Toast.LENGTH_SHORT).show();
                } else {
                    changePassword(URL, new_password, password1);
                }
                dialog.dismiss();
            }
        });
    }

    private void showDeleteAccountForm() {
        View formularioView = getLayoutInflater().inflate(R.layout.delete_account_form, null);

        EditText editTextPassword1 = formularioView.findViewById(R.id.editTextPassword1);
        EditText editTextPassword2 = formularioView.findViewById(R.id.editTextPassword2);
        Button buttonChange = formularioView.findViewById(R.id.buttonChange);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(formularioView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password1 = editTextPassword1.getText().toString();
                String password2 = editTextPassword2.getText().toString();
                String URL = baseURL + "/info/deleteAccount.php";

                if (password1.isEmpty() || password2.isEmpty()){
                    Toast.makeText(Info.this, "Error: Campos requeridos vacios", Toast.LENGTH_SHORT).show();
                } else if (!password1.equals(password2)) {
                    Toast.makeText(Info.this, "Error: Contraseñas diferentes", Toast.LENGTH_SHORT).show();
                } else {
                    deleteAccount(URL, password1);
                }
                dialog.dismiss();
            }
        });
    }

    public void changeUsername(String URL, String username, String password){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    if (success) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        TempData.setUsername(username);
                        textViewUsername.setText(username);
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
                params.put("username", username);
                params.put("email", TempData.getEmail());
                params.put("password", password);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void changeEmail(String URL, String new_email, String password){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    if (success) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        TempData.setEmail(new_email);
                        textViewEmail.setText(new_email);
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
                params.put("new_email", new_email);
                params.put("email", TempData.getEmail());
                params.put("password", password);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void changePassword(String URL, String new_password, String password){

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
                params.put("new_password", new_password);
                params.put("email", TempData.getEmail());
                params.put("password", password);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void deleteAccount(String URL, String password){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    String message = jsonResponse.getString("message");
                    if (success) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        TempData.defaultValues();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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
                params.put("password", password);
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

}