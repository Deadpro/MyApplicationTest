package com.example.myapplicationtest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoice, btnPayments, btnSalesAgents, btnUpdateLocalDB, btnClearLocalTables,
            btnReports, btnViewInvoices;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDayOfTheWeekDefault, sPrefDBName,
            sPrefFreshStatus, sPrefDBPassword, sPrefDBUser, sPrefDayOfTheWeek, sPrefVisited,
            sPrefConnectionStatus, sPrefAreaDefault;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DAYOFTHEWEEKDEFAULT = "DayOfTheWeekDefault";
    final String SAVED_DayOfTheWeek = "DayOfTheWeek";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_VISITED = "visited";
    final String SAVED_CONNSTATUS = "connectionStatus";
    final String SAVED_FRESHSTATUS = "freshStatus";
    final String SAVED_AREADEFAULT = "areaDefault";
    String loginUrl = "https://caiman.ru.com/php/login.php", dbName, dbUser, dbPassword,
            syncUrl = "https://caiman.ru.com/php/syncDB.php", connStatus, areaDefault;
    String[] dayOfTheWeek, salesPartnersName, accountingType, author, itemName, comment, dateTimeDoc;
    Integer[] itemPrice, discountID, spID, area, serverDB_ID, itemNumber, discountType, discount,
            invoiceNumber, agentID, salesPartnerID;
    Double[] itemQuantity, totalSum, exchangeQuantity, returnQuantity, invoiceSum, paymentAmount;
    SharedPreferences.Editor e;
    DBHelper dbHelper;
    final String LOG_TAG = "myLogs";
    SQLiteDatabase db;
    Boolean one, two, three, four, five, six, seven, dropped = false;
    Integer countGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        btnInvoice = findViewById(R.id.buttonInvoice);
        btnPayments = findViewById(R.id.buttonPayments);
        btnSalesAgents = findViewById(R.id.buttonSalesPartners);
        btnUpdateLocalDB = findViewById(R.id.buttonUpdateLocalDB);
        btnClearLocalTables = findViewById(R.id.buttonClearLocalDB);
        btnReports = findViewById(R.id.buttonReports);
        btnViewInvoices = findViewById(R.id.buttonViewInvoices);
        btnViewInvoices.setOnClickListener(this);
        btnReports.setOnClickListener(this);
        btnClearLocalTables.setOnClickListener(this);
        btnUpdateLocalDB.setOnClickListener(this);
        btnInvoice.setOnClickListener(this);
        btnPayments.setOnClickListener(this);
        btnSalesAgents.setOnClickListener(this);

        one = false;
        two = false;
        three = false;
        four = false;
        five = false;
        six = false;
        seven = false;

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeekDefault = getSharedPreferences(SAVED_DAYOFTHEWEEKDEFAULT, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DayOfTheWeek, Context.MODE_PRIVATE);
        sPrefVisited = getSharedPreferences(SAVED_VISITED, Context.MODE_PRIVATE);
        sPrefFreshStatus = getSharedPreferences(SAVED_FRESHSTATUS, Context.MODE_PRIVATE);
        sPrefAreaDefault = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);

        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");

        sPrefArea.edit().clear().apply();
        sPrefAccountingType.edit().clear().apply();
        sPrefDayOfTheWeek.edit().clear().apply();
        sPrefVisited.edit().clear().apply();


        e = sPrefFreshStatus.edit();
        e.putString(SAVED_FRESHSTATUS, "fresh");
        e.apply();

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

//        db.execSQL("DROP TABLE IF EXISTS invoiceLocalDB");
//        db.execSQL("DROP TABLE IF EXISTS syncedInvoice");
//        db.execSQL("DROP TABLE IF EXISTS syncedPayments");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoice:
                createInvoice();
                break;
            case R.id.buttonViewInvoices:
                viewInvoices();
                break;
            case R.id.buttonPayments:
                makePayments();
                break;
            case R.id.buttonSalesPartners:
                manageSalesPartners();
                break;
            case R.id.buttonUpdateLocalDB:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Обновление локальной базы данных")
                        .setMessage("Все таблицы будут перезаписаны!")
                        .setCancelable(true)
                        .setNegativeButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        updateLocalDB();
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("Нет",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.buttonClearLocalDB:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Режим разработки")
                        .setMessage("Удалить к чертям всё?")
                        .setCancelable(true)
                        .setNegativeButton("Да",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (tableExists(db, "invoiceLocalDB")){
                                            clearTable("invoiceLocalDB");
                                        }
                                        if (tableExists(db, "syncedInvoice")){
                                            clearTable("syncedInvoice");
                                        }
                                        if (tableExists(db, "payments")){
                                            clearTable("payments");
                                        }
                                        if (tableExists(db, "syncedPayments")){
                                            clearTable("syncedPayments");
                                        }
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("Ой",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                alert = builder.create();
                alert.show();
                break;
            case R.id.buttonReports:
                testCheck();
                break;
            default:
                break;
        }
    }

    private void createInvoice(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAreaActivity.class);
        startActivity(intent);
    }

    private void viewInvoices(){
        Intent intent = new Intent(getApplicationContext(), ViewInvoicesMenuActivity.class);
        startActivity(intent);
    }

    private void makePayments(){
        Intent intent = new Intent(getApplicationContext(), ViewPaymentsMenuActivity.class);
        startActivity(intent);
    }

    private void manageSalesPartners(){
        Intent intent = new Intent(getApplicationContext(), ManageSalesPartnersActivity.class);
        startActivity(intent);
    }

    private void testCheck(){
        String tmp = "No local invoice table";
        if (tableExists(db, "invoiceLocalDB")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM invoiceLocalDB ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String tmp2 = "No tmp list of items table";
        if (tableExists(db, "itemsToInvoiceTmp")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM itemsToInvoiceTmp ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp2 = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String tmp3 = "No synced table";
        if (tableExists(db, "syncedInvoice")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM syncedInvoice ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp3 = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String tmp4 = "No payments table";
        if (tableExists(db, "payments")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM payments ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp4 = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String tmp5 = "No syncedPayments table";
        if (tableExists(db, "syncedPayments")){
            Integer countt;
            String sql = "SELECT COUNT(*) FROM syncedPayments ";
            Cursor cursor = db.rawQuery(sql, null);
            if (!cursor.moveToFirst()) {
                cursor.close();
                countt = 0;
            } else {
                countt = cursor.getInt(0);
            }
            cursor.close();
            tmp5 = countt.toString();
            Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>>>>>>>>" + countt);
        }

        String sql = "SELECT invoiceNumber FROM syncedInvoice ";
        Cursor c = db.rawQuery(sql, null);
        String tmpIN = "";
        if (c.moveToFirst()) {
            int iNumber = c.getColumnIndex("invoiceNumber");
            do {
                tmpIN = tmpIN + "---" + c.getString(iNumber) ;
            } while (c.moveToNext());
        } else {
            tmpIN = "No";
        }
        c.close();

        Toast.makeText(getApplicationContext(), tmp + "-----" + tmp2 + "-----" + tmp3 + "-----"
                + tmp4 + "-----" + tmp5 + "-----" + tmpIN, Toast.LENGTH_SHORT).show();
    }

    private void updateLocalDB(){
        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
        if (sPrefConnectionStatus.contains(SAVED_CONNSTATUS)){
            connStatus = sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "");
            if (!connStatus.equals("failed")){
                db.execSQL("DROP TABLE IF EXISTS salesPartners");//Удаление таблицы
                db.execSQL("DROP TABLE IF EXISTS items");
                db.execSQL("DROP TABLE IF EXISTS itemsWithDiscount");
                db.execSQL("DROP TABLE IF EXISTS discount");
                db.execSQL("DROP TABLE IF EXISTS invoice");
                db.execSQL("DROP TABLE IF EXISTS paymentsServer");
                dropped = true;
            } else {
                Toast.makeText(getApplicationContext(), "<<< Локальная База >>>" + connStatus, Toast.LENGTH_SHORT).show();
            }
        }
        if (sPrefFreshStatus.getString(SAVED_FRESHSTATUS, "").equals("fresh")){
            if (dropped == true){
                dbHelper.onUpgrade(db, 1, 2);

                loadDateFromServer();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadSalesPartnersFromServerDB();
                    }
                };
                Thread thread2 = new Thread(runnable);
                thread2.start();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadItemsFromServerDB();
                    }
                };
                Thread thread3 = new Thread(runnable);
                thread3.start();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadItemsWithDiscountsFromServerDB();
                    }
                };
                Thread thread4 = new Thread(runnable);
                thread4.start();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadDiscountsFromServerDB();
                    }
                };
                Thread thread5 = new Thread(runnable);
                thread5.start();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadInvoicesFromServerDB();
                    }
                };
                Thread thread6 = new Thread(runnable);
                thread6.start();

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadPaymentsFromServerDB();
                    }
                };
                Thread thread7 = new Thread(runnable);
                thread7.start();
            } else {
                Toast.makeText(getApplicationContext(), "<<< НЕТ подключения к Серверу >>>" + connStatus, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "<<< Обновление возможно только при первом входе >>>" + connStatus, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDateFromServer(){
        StringRequest request = new StringRequest(Request.Method.POST,
                loginUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    String[] dayOfTheWeek = new String[jsonArray.length()];
                    if (jsonArray.length() == 1){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("dayOfTheWeek");
                        }
                        e = sPrefDayOfTheWeekDefault.edit();
                        e.putString(SAVED_DAYOFTHEWEEKDEFAULT, dayOfTheWeek[0]);
                        e.apply();
                        Toast.makeText(getApplicationContext(), "Идет загрузка...", Toast.LENGTH_SHORT).show();
                        one = true;
                    }else{
                        Toast.makeText(getApplicationContext(), "Ошибка Входа. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadSalesPartnersFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    String[] dayOfTheWeek = new String[jsonArray.length()];
                    String[] salesPartnersName= new String[jsonArray.length()];
                    Integer[] area= new Integer[jsonArray.length()];
                    String[] accountingType= new String[jsonArray.length()];
                    String[] author= new String[jsonArray.length()];
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("DayOfTheWeek");
                            salesPartnersName[i] = obj.getString("Наименование");
                            area[i] = obj.getInt("Район");
                            accountingType[i] = obj.getString("Учет");
                            author[i] = obj.getString("Автор");
                            serverDB_ID[i] = obj.getInt("ID");

//                            if (!resultExists(db, "salesPartners","Автор", "Автор", "admin")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in salesPartners: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("Наименование", salesPartnersName[i]);
                                cv.put("Район", area[i]);
                                cv.put("Учет", accountingType[i]);
                                cv.put("DayOfTheWeek", dayOfTheWeek[i]);
                                cv.put("Автор", author[i]);
                                long rowID = db.insert("salesPartners", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu контрагенты loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
//                        Toast.makeText(getApplicationContext(), "Контрагенты загружены", Toast.LENGTH_SHORT).show();
                        two = true;
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
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "salesPartners");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadItemsFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] itemNumber = new Integer[jsonArray.length()];
                    String[] itemName= new String[jsonArray.length()];
                    Integer[] itemPrice= new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemNumber[i] = obj.getInt("Артикул");
                            itemName[i] = obj.getString("Наименование");
                            itemPrice[i] = obj.getInt("Цена");

//                            if (!resultExists(db, "items","Артикул", "Артикул", "1")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in items: ---");
                                cv.put("Артикул", itemNumber[i]);
                                cv.put("Наименование", itemName[i]);
                                cv.put("Цена", itemPrice[i]);
                                long rowID = db.insert("items", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu items loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
//                        Toast.makeText(getApplicationContext(), "Номенклатура загружена", Toast.LENGTH_SHORT).show();
                        three = true;
                        loadMessage();
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
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "items");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadItemsWithDiscountsFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];
                    Integer[] itemNumber= new Integer[jsonArray.length()];
                    Integer[] discountID= new Integer[jsonArray.length()];
                    Integer[] spID = new Integer[jsonArray.length()];
                    String[] author = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            itemNumber[i] = obj.getInt("Артикул");
                            discountID[i] = obj.getInt("ID_скидки");
                            spID[i] = obj.getInt("ID_контрагента");
                            author[i] = obj.getString("Автор");

//                            if (!resultExists(db, "itemsWithDiscount","Автор", "Автор", "admin")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in itemsWithDiscount: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("Артикул", itemNumber[i]);
                                cv.put("ID_скидки", discountID[i]);
                                cv.put("ID_контрагента", spID[i]);
                                cv.put("Автор", author[i]);
                                long rowID = db.insert("itemsWithDiscount", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu itemsWithDiscount loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
//                        Toast.makeText(getApplicationContext(), "НоменклатураСоСкидкой загружена", Toast.LENGTH_SHORT).show();
                        four = true;
                        loadMessage();
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
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "номенклатурасоскидкой");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadDiscountsFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];
                    Integer[] discountType= new Integer[jsonArray.length()];
                    Integer[] discount= new Integer[jsonArray.length()];
                    String[] author = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            discountType[i] = obj.getInt("Тип_скидки");
                            discount[i] = obj.getInt("Скидка");
                            author[i] = obj.getString("Автор");

//                            if (!resultExists(db, "discount","Автор", "Автор", "admin")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in discount: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("Тип_скидки", discountType[i]);
                                cv.put("Скидка", discount[i]);
                                cv.put("Автор", author[i]);
                                long rowID = db.insert("discount", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu discount loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
//                        Toast.makeText(getApplicationContext(), "Скидки загружены", Toast.LENGTH_SHORT).show();
                        five = true;
                        loadMessage();
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
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("tableName", "скидка");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadInvoicesFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];
                    Integer[] invoiceNumber= new Integer[jsonArray.length()];
                    Integer[] agentID= new Integer[jsonArray.length()];
                    Integer[] salesPartnerID = new Integer[jsonArray.length()];
                    String[] accountingType = new String[jsonArray.length()];
                    Integer[] itemNumber = new Integer[jsonArray.length()];
                    Double[] itemQuantity = new Double[jsonArray.length()];
                    Integer[] itemPrice = new Integer[jsonArray.length()];
                    Double[] totalSum = new Double[jsonArray.length()];
                    Double[] exchangeQuantity = new Double[jsonArray.length()];
                    Double[] returnQuantity = new Double[jsonArray.length()];
                    String[] dateTimeDoc = new String[jsonArray.length()];
                    Double[] invoiceSum = new Double[jsonArray.length()];
                    String[] comment = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            invoiceNumber[i] = obj.getInt("InvoiceNumber");
                            agentID[i] = obj.getInt("AgentID");
                            salesPartnerID[i] = obj.getInt("SalesPartnerID");
                            accountingType[i] = obj.getString("AccountingType");
                            itemNumber[i] = obj.getInt("ItemID");
                            itemQuantity[i] = obj.getDouble("Quantity");
                            itemPrice[i] = obj.getInt("Price");
                            totalSum[i] = obj.getDouble("Total");
                            exchangeQuantity[i] = obj.getDouble("ExchangeQuantity");
                            returnQuantity[i] = obj.getDouble("ReturnQuantity");
                            dateTimeDoc[i] = obj.getString("DateTimeDoc");
                            invoiceSum[i] = obj.getDouble("InvoiceSum");
                            comment[i] = obj.getString("Comment");

//                            if (!resultExists(db, "invoice","ReturnQuantity", "ReturnQuantity", "0")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in invoice: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("InvoiceNumber", invoiceNumber[i]);
                                cv.put("AgentID", agentID[i]);
                                cv.put("SalesPartnerID", salesPartnerID[i]);
                                cv.put("AccountingType", accountingType[i]);
                                cv.put("ItemID", itemNumber[i]);
                                cv.put("Quantity", itemQuantity[i]);
                                cv.put("Price", itemPrice[i]);
                                cv.put("Total", totalSum[i]);
                                cv.put("ExchangeQuantity", exchangeQuantity[i]);
                                cv.put("ReturnQuantity", returnQuantity[i]);
                                cv.put("DateTimeDoc", dateTimeDoc[i]);
                                cv.put("InvoiceSum", invoiceSum[i]);
                                cv.put("Comment", comment[i]);
                                long rowID = db.insert("invoice", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu invoice loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
//                        Toast.makeText(getApplicationContext(), "Накладные загружены", Toast.LENGTH_SHORT).show();
                        six = true;
                        loadMessage();
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
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("agentID", areaDefault);
                parameters.put("tableName", "invoice");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadPaymentsFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    Integer[] serverDB_ID = new Integer[jsonArray.length()];
                    String[] dateTimeDoc = new String[jsonArray.length()];
                    Integer[] invoiceNumber = new Integer[jsonArray.length()];
                    Double[] paymentAmount= new Double[jsonArray.length()];
                    String[] author = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            dateTimeDoc[i] = obj.getString("дата_платежа");
                            invoiceNumber[i] = obj.getInt("№_накладной");
                            paymentAmount[i] = obj.getDouble("сумма_внесения");
                            author[i] = obj.getString("автор");

//                            if (!resultExists(db, "paymentsServer","Автор", "Автор", "Рождественская Яна Андреевна")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in paymentsServer: ---");
//                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("DateTimeDoc", dateTimeDoc[i]);
                                cv.put("InvoiceNumber", invoiceNumber[i]);
                                cv.put("сумма_внесения", paymentAmount[i]);
                                cv.put("Автор", author[i]);
                                long rowID = db.insert("paymentsServer", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//                            } else {
//                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu платежи loadDB", Toast.LENGTH_SHORT).show();
//                            }
                        }
//                        Toast.makeText(getApplicationContext(), "Платежи загружены", Toast.LENGTH_SHORT).show();
                        seven = true;
                        loadMessage();
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
                Toast.makeText(getApplicationContext(), "Проблемы с запросом на сервер", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
                parameters.put("agentID", areaDefault);
                parameters.put("tableName", "платежи");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

//    private void syncDB(){
//        ContentValues cv = new ContentValues();
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//        Log.d(LOG_TAG, "--- Insert in salespartners: ---");
//        // подготовим данные для вставки в виде пар: наименование столбца - значение
//        for (int i = 0; i < serverDB_ID.length; i++){
//            cv.put("serverDB_ID", serverDB_ID[i]);
//            cv.put("Наименование", salesPartnersName[i]);
//            cv.put("Район", area[i]);
//            cv.put("Учет", accountingType[i]);
//            cv.put("DayOfTheWeek", dayOfTheWeek[i]);
//            cv.put("Автор", author[i]);
//            // вставляем запись и получаем ее ID
//            long rowID = db.insert("salespartners", null, cv);
//            Log.d(LOG_TAG, "row inserted, ID = " + rowID);
//        }
//    }

    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            // конструктор суперкласса
            super(context, "myLocalDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- onCreate database ---");
            db.execSQL("create table salesPartners ("
                    + "id integer primary key autoincrement,"
                    + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                    + "Наименование text,"
                    + "Район integer,"
                    + "Учет text,"
                    + "DayOfTheWeek text,"
                    + "Автор text" + ");");

            db.execSQL("create table items ("
                    + "id integer primary key autoincrement,"
                    + "Артикул integer UNIQUE ON CONFLICT REPLACE,"
                    + "Наименование text,"
                    + "Цена integer" + ");");

            db.execSQL("create table itemsWithDiscount ("
                    + "id integer primary key autoincrement,"
                    + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                    + "Артикул integer,"
                    + "ID_скидки integer,"
                    + "ID_контрагента integer,"
                    + "Автор text" + ");");

            db.execSQL("create table discount ("
                    + "id integer primary key autoincrement,"
                    + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                    + "Тип_скидки integer,"
                    + "Скидка integer,"
                    + "Автор текст" + ");");

            db.execSQL("create table invoice ("
                    + "id integer primary key autoincrement,"
                    + "serverDB_ID integer UNIQUE ON CONFLICT REPLACE,"
                    + "InvoiceNumber integer,"
                    + "AgentID integer,"
                    + "SalesPartnerID integer,"
                    + "AccountingType text,"
                    + "ItemID integer,"
                    + "Quantity real,"
                    + "Price real,"
                    + "Total real,"
                    + "ExchangeQuantity real,"
                    + "ReturnQuantity  real,"
                    + "DateTimeDoc text,"
                    + "InvoiceSum real,"
                    + "Comment text" + ");");

            db.execSQL("create table paymentsServer ("
                    + "id integer primary key autoincrement,"
                    + "DateTimeDoc text,"
                    + "InvoiceNumber integer,"
                    + "сумма_внесения real,"
                    + "Автор text" + ");");

            if (!tableExists(db, "payments")) {
                db.execSQL("create table payments ("
                        + "id integer primary key autoincrement,"
                        + "DateTimeDoc text,"
                        + "InvoiceNumber integer,"
                        + "сумма_внесения real,"
                        + "Автор text" + ");");
            }

            if (!tableExists(db, "itemsToInvoiceTmp")) {
                db.execSQL("create table itemsToInvoiceTmp ("
                        + "id integer primary key autoincrement,"
                        + "Контрагент text,"
                        + "Наименование text UNIQUE ON CONFLICT REPLACE,"
                        + "Цена integer,"
                        + "ЦенаИзмененная integer,"
                        + "Количество real,"
                        + "Обмен real,"
                        + "Возврат real,"
                        + "Итого real" + ");");
            }

            if (!tableExists(db, "invoiceLocalDB")) {
                db.execSQL("create table invoiceLocalDB ("
                        + "id integer primary key autoincrement,"
                        + "invoiceNumber integer,"
                        + "agentID integer,"
                        + "areaSP integer,"
                        + "salesPartnerName text,"
                        + "accountingTypeDoc text,"
                        + "accountingTypeSP text,"
                        + "itemName text,"
                        + "quantity real,"
                        + "price integer,"
                        + "totalCost real,"
                        + "exchangeQuantity real,"
                        + "returnQuantity real,"
                        + "comment text DEFAULT 'none',"
                        + "dateTimeDocLocal text,"
                        + "invoiceSum text" + ");");
            }

            if (!tableExists(db, "syncedInvoice")) {
                db.execSQL("create table syncedInvoice ("
                        + "id integer primary key autoincrement,"
                        + "invoiceNumber integer,"
                        + "dateTimeDoc text,"
                        + "agentID integer" + ");");
            }

            if (!tableExists(db, "syncedPayments")) {
                db.execSQL("create table syncedPayments ("
                        + "id integer primary key autoincrement,"
                        + "paymentID integer UNIQUE,"
                        + "invoiceNumber integer,"
                        + "dateTimeDoc text,"
                        + "agentID integer" + ");");
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onCreate(db);
        }
    }

    boolean tableExists(SQLiteDatabase db, String tableName){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    boolean resultExistsVariant(SQLiteDatabase db, String tableName){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM invoiceLocalDB";
        Cursor cursor = db.rawQuery(sql, null);
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        countGlobal = count;
        return count > 0;
    }

    boolean resultExists(SQLiteDatabase db, String tableName, String selectField, String fieldName, String fieldValue){
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        String sql = "SELECT ? FROM " + tableName + " WHERE ? LIKE ? LIMIT 1";
        Cursor cursor = db.rawQuery(sql, new String[]{selectField, fieldName, fieldValue});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    private void loadMessage(){
        if (one && two && three && four && five && six && seven){
            Toast.makeText(getApplicationContext(), "<<< Данные загружены >>>", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearTable(String tableName){
        Log.d(LOG_TAG, "--- Clear " + tableName + " : ---");
        // удаляем все записи
        int clearCount = db.delete(tableName, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
        Toast.makeText(getApplicationContext(), "<<< Таблицы очищены >>>", Toast.LENGTH_SHORT).show();
    }
}
