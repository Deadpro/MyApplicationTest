package com.example.myapplicationtest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity implements View.OnClickListener {

    EditText Login, Password;
    Button btnLogin, btnSave, btnSettings;
    RequestQueue requestQueue;
    SharedPreferences sPrefLogin, sPrefPassword, sPrefDBName, sPrefDBPassword, sPrefDBUser;
    final String SAVED_LOGIN = "Login";
    final String SAVED_PASSWORD = "Password";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    SharedPreferences.Editor e;
    String loginUrl = "https://caiman.ru.com/php/test1.php", attribute, dbName, dbUser, dbPassword;
    public static final String EXTRA_ATTRIBUTE = "com.example.myapplicationtest.ATTRIBUTE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Login = findViewById((R.id.editTextLogin));
        Password = findViewById((R.id.editTextPassword));
        btnLogin = findViewById(R.id.buttonLogin);
        btnSave = findViewById(R.id.buttonSave);
        btnSettings = findViewById(R.id.buttonSettings);
        btnLogin.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnSettings.setOnClickListener(this);

        requestQueue = Volley.newRequestQueue((getApplicationContext()));

        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefPassword = getSharedPreferences(SAVED_PASSWORD, Context.MODE_PRIVATE);
        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);

        if (sPrefDBName.contains(SAVED_DBName) && sPrefDBUser.contains(SAVED_DBUser) && sPrefDBPassword.contains(SAVED_DBPassword)){
            dbName = sPrefDBName.getString(SAVED_DBName, "");
            dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
            dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        }
        if(sPrefLogin.contains(SAVED_LOGIN)) {
            Login.setText(sPrefLogin.getString(SAVED_LOGIN, ""));
            Password.setText(sPrefPassword.getString(SAVED_PASSWORD, ""));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                saveLoginPassword();
                Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonLogin:
                login();
                Toast.makeText(this, dbName + " " + dbUser + " " + dbPassword, Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonSettings:
                serverSettings();
                break;
            default:
                break;
        }
    }

    private void serverSettings(){
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    private void login() {
        StringRequest request = new StringRequest(Request.Method.POST,
                loginUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.names().get(0).equals("success")){
                        Toast.makeText(getApplicationContext(), "Login successfull", Toast.LENGTH_SHORT).show();
                        attribute = jsonObject.getString("success");
                        Intent intent = new Intent(getApplicationContext(), DisplayMessageActivity.class);
                        //EditText editText = (EditText) findViewById(R.id.editText2);
                        //String message = editText.getText().toString();
                        intent.putExtra(EXTRA_ATTRIBUTE, attribute);
                        startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Login ERROR", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("Login", Login.getText().toString());
                parameters.put("Password", Password.getText().toString());
                return parameters;
            }
        };
        requestQueue.add(request);

        /*JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                loginUrl, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    JSONArray dbUsers = response.getJSONArray("security");
                    for (int i = 0; i < dbUsers.length(); i++){
                        JSONObject dbUser = dbUsers.getJSONObject(i);

                        attribute = dbUser.getString("attribute");

                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.e("TAG", "Error " + error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        //EditText editText = (EditText) findViewById(R.id.editText2);
        //String message = editText.getText().toString();
        intent.putExtra(EXTRA_ATTRIBUTE, attribute);
        startActivity(intent);*/
    }

    private void saveLoginPassword() {
        e = sPrefLogin.edit();
        e.putString(SAVED_LOGIN, Login.getText().toString());
        e.apply();
        e = sPrefPassword.edit();
        e.putString(SAVED_PASSWORD, Password.getText().toString());
        e.apply();
    }
}
