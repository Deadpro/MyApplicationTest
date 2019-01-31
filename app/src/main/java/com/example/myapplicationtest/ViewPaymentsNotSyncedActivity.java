package com.example.myapplicationtest;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPaymentsNotSyncedActivity extends AppCompatActivity implements View.OnClickListener  {

    List<DataPaymentLocal> listTmp = new ArrayList<>();
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    Button btnSyncPaymentsWithServer;
    Integer invoiceNumberServer, invoiceNumberLocalTmp;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefLogin, sPrefAccountingTypeDefault,
            sPrefArea, sPrefAreaDefault, sPrefAgent;
    String paymentSum, paymentsID = "", dbName, dbUser, dbPassword, agent,
            requestUrlMakePayment = "https://caiman.ru.com/php/makePayment.php",
            loginSecurity, statusSave = "", areaDefault, dateTimeDocServer;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_LOGIN = "Login";
    final String SAVED_ACCOUNTINGTYPEDEFAULT = "AccountingTypeDefault";
    final String SAVED_AREA = "Area";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_AGENT = "agent";
    ArrayList<String> arrItems, invoiceNumberServerTmp;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum;
    ArrayList<Integer> arrPriceChanged, paymentsIDsList;
    List<DataInvoice> dataArray;
    String[] dateTimeDocServerFromRequest, invoiceNumberFromRequest, paymentIDFromRequest;
    List<DataPay> dataPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_payments_not_synced);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        dataArray = new ArrayList<>();
        dataPay = new ArrayList<>();
        paymentsIDsList = new ArrayList<>();
        invoiceNumberServerTmp = new ArrayList<>();
//        dateTimeDocServer = new ArrayList<>();

        btnSyncPaymentsWithServer = findViewById(R.id.buttonSyncPaymentsWithServer);
        btnSyncPaymentsWithServer.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefAccountingTypeDefault = getSharedPreferences(SAVED_ACCOUNTINGTYPEDEFAULT, Context.MODE_PRIVATE);
        sPrefArea= getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAreaDefault  = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefAgent = getSharedPreferences(SAVED_AGENT, Context.MODE_PRIVATE);

        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        loginSecurity = sPrefLogin.getString(SAVED_LOGIN, "");
        agent = sPrefAgent.getString(SAVED_AGENT, "");

        setInitialData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSyncPaymentsWithServer:
                if (statusSave.equals("Saved")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Внимание")
                            .setMessage("У вас нет несинхронизированных платежей")
                            .setCancelable(false)
                            .setPositiveButton("Назад",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            finish();
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    savePaymentsToServerDB();
                }
                break;
            default:
                break;
        }
    }

    private void setInitialData() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy/MM/dd HH:mm:ss" );
        final String output = now.with(LocalTime.MIN).format( formatter );
        Toast.makeText(getApplicationContext(), output, Toast.LENGTH_SHORT).show();

        if (resultExists(db, "payments","InvoiceNumber")){
            String sql = "SELECT payments.id FROM payments " +
                    "WHERE NOT EXISTS (SELECT syncedPayments.paymentID FROM syncedPayments " +
                    "WHERE payments.id LIKE  syncedPayments.paymentID) " +
                    "AND payments.DateTimeDoc > ? ";
            Cursor c = db.rawQuery(sql, new String[]{output});
            if (c.moveToFirst()) {
                int iNumber = c.getColumnIndex("id");
                do {
                    paymentsID = paymentsID + "----" + c.getString(iNumber) ;
                    paymentsIDsList.add(Integer.parseInt(c.getString(iNumber)));
                } while (c.moveToNext());
            } else {
                alreadySyncedPrompt();
            }
            c.close();
            Toast.makeText(getApplicationContext(), "Несинхронизированные: " + String.valueOf(paymentsIDsList.size()), Toast.LENGTH_SHORT).show();
        } else {
            String sql = "SELECT id FROM payments WHERE payments.DateTimeDoc > ? ";
            Cursor c = db.rawQuery(sql, new String[]{output});
            if (c.moveToFirst()) {
                int iNumber = c.getColumnIndex("InvoiceNumber");
                do {
                    paymentsID = paymentsID + "----" + c.getString(iNumber);
                    paymentsIDsList.add(Integer.parseInt(c.getString(iNumber)));
                } while (c.moveToNext());
            }
            c.close();
            Toast.makeText(getApplicationContext(), "Ничего не синхронизировано: " + paymentsID, Toast.LENGTH_SHORT).show();
        }

        for (int i = 0; i < paymentsIDsList.size(); i++){
            String sql = "SELECT DISTINCT payments.InvoiceNumber, payments.DateTimeDoc, payments.сумма_внесения, payments.id, " +
                    "invoiceLocalDB.salesPartnerName, invoiceLocalDB.accountingTypeDoc, invoiceLocalDB.invoiceSum" +
                    " FROM payments INNER JOIN invoiceLocalDB ON payments.InvoiceNumber LIKE invoiceLocalDB.invoiceNumber" +
                    " WHERE payments.id LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{paymentsIDsList.get(i).toString()});
            if (c.moveToFirst()) {
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                int dateTimeDocLocalTmp = c.getColumnIndex("DateTimeDoc");
                int invoiceSumTmp = c.getColumnIndex("invoiceSum");
                int invoiceNumberTmp = c.getColumnIndex("InvoiceNumber");
                int paymentIDTmp = c.getColumnIndex("id");
                int paymentSumTmp = c.getColumnIndex("сумма_внесения");
                do {
                    String salesPartnerName = c.getString(salesPartnerNameTmp);
                    String accountingTypeDoc = c.getString(accountingTypeDocTmp);
                    String dateTimeDocLocal = c.getString(dateTimeDocLocalTmp);
                    Double invoiceSum = Double.parseDouble(c.getString(invoiceSumTmp));
                    Double paymentSum = Double.parseDouble(c.getString(paymentSumTmp));
                    Integer paymentIDLocal = Integer.parseInt(c.getString(paymentIDTmp));
                    Integer invoiceNumber = Integer.parseInt(c.getString(invoiceNumberTmp));
                    dateTimeDocServer = "";
                    listTmp.add(new DataPaymentLocal(salesPartnerName, accountingTypeDoc,
                            invoiceNumber, paymentIDLocal, invoiceSum, paymentSum, dateTimeDocLocal,
                            dateTimeDocServer));
                    DataPay dt = new DataPay(invoiceNumber, invoiceSum, paymentIDLocal);
                    dataPay.add(dt);
                } while (c.moveToNext());
            }
            c.close();
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerViewPaymentsLocal);
        DataAdapterViewPaymentsFromLocalDB adapter = new DataAdapterViewPaymentsFromLocalDB(this, listTmp);
        recyclerView.setAdapter(adapter);
    }

    private void alreadySyncedPrompt(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Поздравляю")
                .setMessage("У вас нет несинхронизированных документов")
                .setCancelable(false)
                .setPositiveButton("Я  Рад(а)",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void savePaymentsToServerDB(){
        sendToServer();
    }

    private void sendToServer(){
        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of( "Asia/Sakhalin" );
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , zoneId );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy/MM/dd HH:mm:ss" );
        final String output = zdt.format( formatter );

        Gson gson = new Gson();
        final String newDataArray = gson.toJson(dataPay);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlMakePayment, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response", "result: " + response);
                dataPay.clear();
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    invoiceNumberFromRequest = new String[jsonArray.length()];
                    paymentIDFromRequest = new String[jsonArray.length()];
                    String[] status = new String[jsonArray.length()];
                    String tmpStatus = "";

                    ContentValues cv = new ContentValues();
                    Log.d(LOG_TAG, "--- Insert in syncedPayments: ---");

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            invoiceNumberFromRequest[i] = obj.getString("invoiceNumber");
                            paymentIDFromRequest[i] = obj.getString("paymentID");
                            status[i] = obj.getString("status");
                            if (status[i].equals("Бабло внесено")) {
                                tmpStatus = "Yes";
                            }

                            cv.put("invoiceNumber", Integer.parseInt(invoiceNumberFromRequest[i]));
                            cv.put("paymentID", paymentIDFromRequest[i]);
                            cv.put("agentID", areaDefault);
                            cv.put("dateTimeDoc", output);
                            long rowID = db.insert("syncedPayments", null, cv);
                            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                        }
                        if (tmpStatus.equals("Yes")){
                            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                            builder.setTitle("Успешно синхронизировано")
                                    .setMessage("Деньги внесены и синхронизированы с сервером")
                                    .setCancelable(false)
                                    .setPositiveButton("Назад",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    finish();
                                                    dialog.cancel();
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                        Toast.makeText(getApplicationContext(), "<<< Платеж Синхронизирован >>>", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                onConnectionFailedPayment();
                Toast.makeText(getApplicationContext(), "Сообщите об этой ошибке. Код 002", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("agent", agent);
                parameters.put("agentID", areaDefault);
                parameters.put("array", newDataArray);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myLocalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    boolean resultExists(SQLiteDatabase db, String tableName, String selectField){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT COUNT(?) FROM " + tableName;
        Cursor cursor = db.rawQuery(sql, new String[]{selectField});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    private void onConnectionFailed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Нет ответа от Сервера")
                .setMessage("Попробуйте позже")
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void onConnectionFailedPayment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Деньги внесены")
                .setMessage("Синхронизируйте вручную, когда появится связь")
                .setCancelable(false)
                .setNegativeButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
//                                sPrefAccountingType.edit().clear().apply();
                                finish();
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
