package com.example.myapplicationtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.EditText;

import static android.icu.text.MessagePattern.ArgType.SELECT;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    Button btnInvoice, btnPayments, btnSalesAgents;
    SharedPreferences sPrefArea, sPrefAccountingType, sPrefDayOfTheWeekDefault, sPrefDBName,
            sPrefDBPassword, sPrefDBUser, sPrefDayOfTheWeek, sPrefVisited, sPrefConnectionStatus;
    final String SAVED_AREA = "Area";
    final String SAVED_ACCOUNTINGTYPE = "AccountingType";
    final String SAVED_DAYOFTHEWEEKDEFAULT = "DayOfTheWeekDefault";
    final String SAVED_DayOfTheWeek = "DayOfTheWeek";
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_VISITED = "visited";
    final String SAVED_CONNSTATUS = "connectionStatus";
    String loginUrl = "https://caiman.ru.com/php/login.php", dbName, dbUser, dbPassword,
            syncUrl = "https://caiman.ru.com/php/syncDB.php", connStatus;
    String[] dayOfTheWeek, salesPartnersName, accountingType, author, itemName;
    Integer[] itemPrice, discountID, spID, area, serverDB_ID, itemNumber, discountType, discount;
    SharedPreferences.Editor e;
    DBHelper dbHelper;
    final String LOG_TAG = "myLogs";
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

//        if (tableExists(db, "salesPartners")){
////            if (resultExists(db, "salesPartners","Автор", "Автор", "admin")){
//            Log.d(LOG_TAG, "--- Clear mytable: ---");
//            // удаляем все записи из таблицы
//            int clearCount = db.delete("salesPartners", null, null);
//            Log.d(LOG_TAG, "deleted rows count = " + clearCount);
//        }

        btnInvoice = findViewById(R.id.buttonInvoice);
        btnPayments = findViewById(R.id.buttonPayments);
        btnSalesAgents = findViewById(R.id.buttonSalesPartners);
        btnInvoice.setOnClickListener(this);
        btnPayments.setOnClickListener(this);
        btnSalesAgents.setOnClickListener(this);

//        Intent intent = getIntent();
//        String agentName = intent.getStringExtra(Login.EXTRA_AGENTNAME);
//        TextView textView = findViewById(R.id.textViewAgent);
//        textView.setText(agentName);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefArea = getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAccountingType = getSharedPreferences(SAVED_ACCOUNTINGTYPE, Context.MODE_PRIVATE);
        sPrefDayOfTheWeekDefault = getSharedPreferences(SAVED_DAYOFTHEWEEKDEFAULT, Context.MODE_PRIVATE);
        sPrefDayOfTheWeek = getSharedPreferences(SAVED_DayOfTheWeek, Context.MODE_PRIVATE);
        sPrefVisited = getSharedPreferences(SAVED_VISITED, Context.MODE_PRIVATE);

        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");

        sPrefArea.edit().clear().apply();
        sPrefAccountingType.edit().clear().apply();
        sPrefDayOfTheWeek.edit().clear().apply();
        sPrefVisited.edit().clear().apply();

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        sPrefConnectionStatus = getSharedPreferences(SAVED_CONNSTATUS, Context.MODE_PRIVATE);
        if (sPrefConnectionStatus.contains(SAVED_CONNSTATUS)){
            connStatus = sPrefConnectionStatus.getString(SAVED_CONNSTATUS, "");
            if (!connStatus.equals("failed")){
//                if (tableExists(db, "salesPartners") &&
//                        tableExists(db, "items") &&
//                        tableExists(db, "itemsWithDiscount") &&
//                        tableExists(db, "discount") &&
//                        tableExists(db, "invoice") &&
//                        tableExists(db, "payments")) {
                db.execSQL("DROP TABLE IF EXISTS salesPartners");//Удаление таблицы
                db.execSQL("DROP TABLE IF EXISTS items");
                db.execSQL("DROP TABLE IF EXISTS itemsWithDiscount");
                db.execSQL("DROP TABLE IF EXISTS discount");
                db.execSQL("DROP TABLE IF EXISTS invoice");
                db.execSQL("DROP TABLE IF EXISTS payments");
                dbHelper.onUpgrade(db, 1, 2);
                loadDateFromServer();
                loadSalesPartnersFromServerDB();
                loadItemsFromServerDB();
                loadItemsWithDiscountsFromServerDB();
                loadDiscountsFromServerDB();
                loaInvoicesFromServerDB();
                loadPaymentsFromServerDB();
//        syncDB();
//                }
            } else {
                Toast.makeText(getApplicationContext(), "Статус входа через Интернет: " + connStatus, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInvoice:
                createInvoice();
                break;
            case R.id.buttonPayments:
                makePayments();
                break;
            case R.id.buttonSalesPartners:
                manageSalesPartners();
                break;
            default:
                break;
        }
    }

    private void createInvoice(){
        Intent intent = new Intent(getApplicationContext(), CreateInvoiceFilterAreaActivity.class);
        startActivity(intent);
    }

    private void makePayments(){
        Intent intent = new Intent(getApplicationContext(), MakePaymentsActivity.class);
        startActivity(intent);
    }

    private void manageSalesPartners(){
        Intent intent = new Intent(getApplicationContext(), ManageSalesPartnersActivity.class);
        startActivity(intent);
    }

    private void loadDateFromServer(){
        StringRequest request = new StringRequest(Request.Method.POST,
                loginUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    dayOfTheWeek = new String[jsonArray.length()];
                    if (jsonArray.length() == 1){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("dayOfTheWeek");
                        }
                        e = sPrefDayOfTheWeekDefault.edit();
                        e.putString(SAVED_DAYOFTHEWEEKDEFAULT, dayOfTheWeek[0]);
                        e.apply();
                        Toast.makeText(getApplicationContext(), "День недели: " + dayOfTheWeek[0], Toast.LENGTH_SHORT).show();
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
                    dayOfTheWeek = new String[jsonArray.length()];
                    salesPartnersName= new String[jsonArray.length()];
                    area= new Integer[jsonArray.length()];
                    accountingType= new String[jsonArray.length()];
                    author= new String[jsonArray.length()];
                    serverDB_ID = new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            dayOfTheWeek[i] = obj.getString("DayOfTheWeek");
                            salesPartnersName[i] = obj.getString("Наименование");
                            area[i] = obj.getInt("Район");
                            accountingType[i] = obj.getString("Учет");
                            author[i] = obj.getString("Автор");
                            serverDB_ID[i] = obj.getInt("ID");

                            if (!resultExists(db, "salesPartners","Автор", "Автор", "admin")){
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
                            } else {
                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu контрагенты loadDB", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Контрагенты загружены", Toast.LENGTH_SHORT).show();
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
                    itemNumber = new Integer[jsonArray.length()];
                    itemName= new String[jsonArray.length()];
                    itemPrice= new Integer[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            itemNumber[i] = obj.getInt("Артикул");
                            itemName[i] = obj.getString("Наименование");
                            itemPrice[i] = obj.getInt("Цена");

                            if (!resultExists(db, "items","Артикул", "Артикул", "1")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in items: ---");
                                cv.put("Артикул", itemNumber[i]);
                                cv.put("Наименование", itemName[i]);
                                cv.put("Цена", itemPrice[i]);
                                long rowID = db.insert("items", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                            } else {
                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu items loadDB", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Номенклатура загружена", Toast.LENGTH_SHORT).show();
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
                    serverDB_ID = new Integer[jsonArray.length()];
                    itemNumber= new Integer[jsonArray.length()];
                    discountID= new Integer[jsonArray.length()];
                    spID = new Integer[jsonArray.length()];
                    author = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            itemNumber[i] = obj.getInt("Артикул");
                            discountID[i] = obj.getInt("ID_скидки");
                            spID[i] = obj.getInt("ID_контрагента");
                            author[i] = obj.getString("Автор");

                            if (!resultExists(db, "itemsWithDiscount","Автор", "Автор", "admin")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in itemsWithDiscount: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("Артикул", itemNumber[i]);
                                cv.put("ID_скидки", discountID[i]);
                                cv.put("ID_контрагента", spID[i]);
                                cv.put("Автор", author[i]);
                                long rowID = db.insert("itemsWithDiscount", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                            } else {
                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu itemsWithDiscount loadDB", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Toast.makeText(getApplicationContext(), "НоменклатураСоСкидкой загружена", Toast.LENGTH_SHORT).show();
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
                    serverDB_ID = new Integer[jsonArray.length()];
                    discountType= new Integer[jsonArray.length()];
                    discount= new Integer[jsonArray.length()];
                    author = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            discountType[i] = obj.getInt("Тип_скидки");
                            discount[i] = obj.getInt("Скидка");
                            author[i] = obj.getString("Автор");

                            if (!resultExists(db, "discount","Автор", "Автор", "admin")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in discount: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("Тип_скидки", itemNumber[i]);
                                cv.put("Скидка", discountID[i]);
                                cv.put("Автор", author[i]);
                                long rowID = db.insert("discount", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                            } else {
                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu discount loadDB", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Скидки загружены", Toast.LENGTH_SHORT).show();
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

    private void loaInvoicesFromServerDB(){
        StringRequest request = new StringRequest(Request.Method.POST,
                syncUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    serverDB_ID = new Integer[jsonArray.length()];
                    invoiceNumber= new Integer[jsonArray.length()];
                    agentID= new Integer[jsonArray.length()];
                    salesPartnerID = new Integer[jsonArray.length()];
                    accountingType = new String[jsonArray.length()];
                    itemNumber = new Integer[jsonArray.length()];
                    itemQuantity = new Double[jsonArray.length()];
                    itemPrice = new Integer[jsonArray.length()];
                    totalSum = new Double[jsonArray.length()];
                    exchangeQuantity = new Double[jsonArray.length()];
                    returnQuantity = new Double[jsonArray.length()];
                    dateTimeDoc = new Date[jsonArray.length()];
                    invoiceSum = new Double[jsonArray.length()];
                    comment = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            serverDB_ID[i] = obj.getInt("ID");
                            discountType[i] = obj.getInt("Тип_скидки");
                            discount[i] = obj.getInt("Скидка");
                            author[i] = obj.getString("Автор");

                            if (!resultExists(db, "discount","Автор", "Автор", "admin")){
                                ContentValues cv = new ContentValues();
                                Log.d(LOG_TAG, "--- Insert in discount: ---");
                                cv.put("serverDB_ID", serverDB_ID[i]);
                                cv.put("Тип_скидки", itemNumber[i]);
                                cv.put("Скидка", discountID[i]);
                                cv.put("Автор", author[i]);
                                long rowID = db.insert("discount", null, cv);
                                Log.d(LOG_TAG, "row inserted, ID = " + rowID);
                            } else {
                                Toast.makeText(getApplicationContext(), "Ошибка: MainMenu invoice loadDB", Toast.LENGTH_SHORT).show();
                            }
                        }
                        Toast.makeText(getApplicationContext(), "Накладные загружены", Toast.LENGTH_SHORT).show();
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
                parameters.put("tableName", "invoice");
                return parameters;
            }
        };
        VolleySingleton.getInstance(this).getRequestQueue().add(request);
    }

    private void loadPaymentsFromServerDB(){

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
//            if (!tableExists(db, "salesPartners")) {
                db.execSQL("create table salesPartners ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer,"
                        + "Наименование text,"
                        + "Район integer,"
                        + "Учет text,"
                        + "DayOfTheWeek text,"
                        + "Автор text,"
                        + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");
//            }
//            if (!tableExists(db, "items")) {
                db.execSQL("create table items ("
                        + "id integer primary key autoincrement,"
                        + "Артикул integer,"
                        + "Наименование text,"
                        + "Цена integer,"
                        + "UNIQUE (Артикул) ON CONFLICT REPLACE" + ");");
//            }
//            if (!tableExists(db, "itemsWithDiscount")) {
                db.execSQL("create table itemsWithDiscount ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer,"
                        + "Артикул integer,"
                        + "ID_скидки integer,"
                        + "ID_контрагента integer,"
                        + "Автор text,"
                        + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");
//            }
//            if (!tableExists(db, "discount")) {
                db.execSQL("create table discount ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer,"
                        + "Тип_скидки integer,"
                        + "Скидка integer,"
                        + "Автор текст,"
                        + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");
//            }
//            if (!tableExists(db, "invoice")) {
                db.execSQL("create table invoice ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer,"
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
                        + "Comment text,"
                        + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");
//            }
//            if (!tableExists(db, "payments")) {
                db.execSQL("create table payments ("
                        + "id integer primary key autoincrement,"
                        + "serverDB_ID integer,"
                        + "InvoiceNumber integer,"
                        + "сумма_внесения real,"
                        + "автор text,"
                        + "UNIQUE (serverDB_ID) ON CONFLICT REPLACE" + ");");
//            }
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
}
