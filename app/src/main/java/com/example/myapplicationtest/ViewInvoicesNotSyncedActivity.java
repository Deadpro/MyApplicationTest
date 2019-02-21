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
import android.view.WindowManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewInvoicesNotSyncedActivity extends AppCompatActivity implements View.OnClickListener {

    List<DataInvoiceLocal> listTmp = new ArrayList<>();
    final String LOG_TAG = "myLogs";
    DBHelper dbHelper;
    SQLiteDatabase db;
    Button btnSaveInvoiceToLocalDB;
    SharedPreferences sPrefDBName, sPrefDBPassword, sPrefDBUser, sPrefLogin, sPrefAccountingTypeDefault,
            sPrefArea, sPrefAreaDefault, sPrefInvoiceNumberLast;
    String paymentStatus, invoiceNumbers = "", dbName, dbUser, dbPassword,
            requestUrlSaveRecord = "https://caiman.ru.com/php/saveNewInvoice_new.php",
            loginSecurity, statusSave = "", areaDefault, invoiceNumberLast, invoiceNumberChosen, accTypeSPChosen;
    final String SAVED_DBName = "dbName";
    final String SAVED_DBUser = "dbUser";
    final String SAVED_DBPassword = "dbPassword";
    final String SAVED_LOGIN = "Login";
    final String SAVED_ACCOUNTINGTYPEDEFAULT = "AccountingTypeDefault";
    final String SAVED_AREA = "Area";
    final String SAVED_AREADEFAULT = "areaDefault";
    final String SAVED_InvoiceNumberLast = "invoiceNumberLast";
    ArrayList<String> arrItems, invoiceNumberServerTmp, dateTimeDocServer, summaryListTmp, accTypeListTmp,
            accTypeSPListTmp, invoiceNumberListTmp, summaryListTmpSecond, accTypeListTmpSecond,
            accTypeSPListTmpSecond, invoiceNumberListTmpSecond;
    ArrayList<Double> arrQuantity, arrExchange, arrReturn, arrSum;
    ArrayList<Integer> arrPriceChanged, invoiceNumbersList;
    List<DataInvoice> dataArray;
    String[] requestMessage, summaryList, summaryListSecond;
    Boolean saveMenuTrigger = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invoices_not_synced);

        dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();

        dataArray = new ArrayList<>();
        arrItems = new ArrayList<>();
        arrSum = new ArrayList<>();
        arrQuantity = new ArrayList<>();
        arrExchange = new ArrayList<>();
        arrReturn = new ArrayList<>();
        arrPriceChanged = new ArrayList<>();
        invoiceNumbersList = new ArrayList<>();
        invoiceNumberServerTmp = new ArrayList<>();
        dateTimeDocServer = new ArrayList<>();
        summaryListTmp = new ArrayList<>();
        accTypeSPListTmp = new ArrayList<>();
        accTypeListTmp = new ArrayList<>();
        invoiceNumberListTmp = new ArrayList<>();
        summaryListTmpSecond = new ArrayList<>();
        accTypeSPListTmpSecond = new ArrayList<>();
        accTypeListTmpSecond = new ArrayList<>();
        invoiceNumberListTmpSecond = new ArrayList<>();

        btnSaveInvoiceToLocalDB = findViewById(R.id.buttonSyncInvoicesWithServer);
        btnSaveInvoiceToLocalDB.setOnClickListener(this);

        sPrefDBName = getSharedPreferences(SAVED_DBName, Context.MODE_PRIVATE);
        sPrefDBUser = getSharedPreferences(SAVED_DBUser, Context.MODE_PRIVATE);
        sPrefDBPassword = getSharedPreferences(SAVED_DBPassword, Context.MODE_PRIVATE);
        sPrefLogin = getSharedPreferences(SAVED_LOGIN, Context.MODE_PRIVATE);
        sPrefAccountingTypeDefault = getSharedPreferences(SAVED_ACCOUNTINGTYPEDEFAULT, Context.MODE_PRIVATE);
        sPrefArea= getSharedPreferences(SAVED_AREA, Context.MODE_PRIVATE);
        sPrefAreaDefault  = getSharedPreferences(SAVED_AREADEFAULT, Context.MODE_PRIVATE);
        sPrefInvoiceNumberLast = getSharedPreferences(SAVED_InvoiceNumberLast, Context.MODE_PRIVATE);

        areaDefault = sPrefAreaDefault.getString(SAVED_AREADEFAULT, "");
        dbName = sPrefDBName.getString(SAVED_DBName, "");
        dbUser = sPrefDBUser.getString(SAVED_DBUser, "");
        dbPassword = sPrefDBPassword.getString(SAVED_DBPassword, "");
        loginSecurity = sPrefLogin.getString(SAVED_LOGIN, "");
        invoiceNumberLast = sPrefInvoiceNumberLast.getString(SAVED_InvoiceNumberLast, "");

        setInitialData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSyncInvoicesWithServer:
                if (statusSave.equals("Saved")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Внимание")
                            .setMessage("У вас нет несинхронизированных накладных")
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
                    saveMenu();
                }
                break;
            default:
                break;
        }
    }

    private void setInitialData() {
//        Integer count;
//        String sql = "SELECT COUNT(*) FROM itemsToInvoiceTmp ";
//        Cursor cursor = db.rawQuery(sql, null);
//        if (!cursor.moveToFirst()) {
//            cursor.close();
//            count = 0;
//        } else {
//            count = cursor.getInt(0);
//        }
//        cursor.close();
//        tmpCount = count;
        if (statusSave.equals("Saved")){
            Toast.makeText(getApplicationContext(), "StatusSave", Toast.LENGTH_SHORT).show();
        } else {
            invoiceNumberServerTmp.add(String.valueOf(0));
            dateTimeDocServer.add("");
        }

//        if (resultExists(db, "syncedInvoice","invoiceNumber")){
//            String sql = "SELECT DISTINCT invoiceLocalDB.invoiceNumber FROM invoiceLocalDB " +
//                    "WHERE NOT EXISTS (SELECT syncedInvoice.invoiceNumber FROM syncedInvoice " +
//                    "WHERE invoiceLocalDB.invoiceNumber LIKE  syncedInvoice.invoiceNumber) ";
//            Cursor c = db.rawQuery(sql, null);
//            if (c.moveToFirst()) {
//                int iNumber = c.getColumnIndex("invoiceNumber");
//                do {
//                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber) ;
//                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
//                } while (c.moveToNext());
//            } else {
//                alreadySyncedPrompt();
//            }
//            c.close();
//            Toast.makeText(getApplicationContext(), "Несинхронизированные: " + invoiceNumbers, Toast.LENGTH_SHORT).show();
//        } else {
        if (resultExists(db, "invoiceLocalDB", "invoiceNumber")){
            String sql = "SELECT DISTINCT invoiceNumber FROM invoiceLocalDB ";
            Cursor c = db.rawQuery(sql, null);
            if (c.moveToFirst()) {
                int iNumber = c.getColumnIndex("invoiceNumber");
                do {
                    invoiceNumbers = invoiceNumbers + "----" + c.getString(iNumber);
                    invoiceNumbersList.add(Integer.parseInt(c.getString(iNumber)));
                } while (c.moveToNext());
            }
            c.close();
            for (int i = 0; i < invoiceNumbersList.size(); i++){
                Toast.makeText(getApplicationContext(), "Ничего не синхронизировано: " + invoiceNumbers, Toast.LENGTH_SHORT).show();
            }
        } else {
            alreadySyncedPrompt();
        }

        for (int i = 0; i < invoiceNumbersList.size(); i++){
            String sql = "SELECT salesPartnerName, accountingTypeDoc, dateTimeDocLocal, invoiceSum" +
                    " FROM invoiceLocalDB WHERE invoiceNumber LIKE ?";
            Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
            if (c.moveToFirst()) {
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                int dateTimeDocLocalTmp = c.getColumnIndex("dateTimeDocLocal");
                int invoiceSumTmp = c.getColumnIndex("invoiceSum");
                String salesPartnerName = c.getString(salesPartnerNameTmp);
                String accountingTypeDoc = c.getString(accountingTypeDocTmp);
                String dateTimeDocLocal = c.getString(dateTimeDocLocalTmp);
                Double invoiceSum = Double.parseDouble(c.getString(invoiceSumTmp));
                paymentStatus = "";
                listTmp.add(new DataInvoiceLocal(salesPartnerName, accountingTypeDoc,
                        Integer.parseInt(invoiceNumberServerTmp.get(0)), dateTimeDocServer.get(0), dateTimeDocLocal,
                        invoiceSum, paymentStatus));
                c.moveToNext();
            }
            c.close();
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerViewInvoicesLocal);
        DataAdapterViewInvoicesFromLocalDB adapter = new DataAdapterViewInvoicesFromLocalDB(this, listTmp);
        recyclerView.setAdapter(adapter);

        saveMenuTrigger = true;
        saveInvoicesToServerDB();
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

    private void saveMenu(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Синхронизация");
        builder.setPositiveButton("Синхронизировать",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveInvoicesToServerDB();
//                        finish();
                        dialog.cancel();
                    }
                });
        builder.setNegativeButton("Контроль",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listWithoutIDFilter();
                        manageMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(summaryList, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                invoiceNumberChosen = invoiceNumberListTmp.get(item);
                Toast.makeText(getApplicationContext(), "Накладная №: " + invoiceNumberChosen, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(alert.getWindow().getAttributes());
//        lp.width = 500;
//        lp.height = 1100;
//        lp.x = -300;
//        alert.getWindow().setAttributes(lp);
    }

    private void manageMenu(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Сырой список. Несовпадений: " + (summaryListSecond.length - summaryList.length));
        builder.setNegativeButton("Назад",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        saveMenu();
                        dialog.cancel();
                    }
                });
        builder.setItems(summaryListSecond, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                invoiceNumberChosen = invoiceNumberListTmpSecond.get(item);
                accTypeSPChosen = accTypeSPListTmpSecond.get(item);
                Toast.makeText(getApplicationContext(), "Накладная №: " + invoiceNumberChosen, Toast.LENGTH_SHORT).show();
                changeInvoice();
                finish();
            }
        });
        builder.setCancelable(true);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void changeInvoice(){
        String accTypeTmp;
        if (accTypeSPChosen.equals("непровод")){
            accTypeTmp = "провод";
        } else {
            accTypeTmp = "непровод";
        }
        ContentValues cv = new ContentValues();
        Log.d(LOG_TAG, "--- update invoiceLocalDB: ---");
        cv.put("accountingTypeSP", accTypeTmp);
        long rowID = db.update("invoiceLocalDB", cv, "invoiceNumber = ?",
                new String[]{invoiceNumberChosen});
        Log.d(LOG_TAG, "row updated, ID = " + rowID);
    }

    private void saveInvoicesToServerDB(){
        for (int i = 0; i < invoiceNumbersList.size(); i++){
//            dataArray.clear();
            String sql = "SELECT invoiceLocalDB.*, salesPartners.serverDB_ID FROM invoiceLocalDB " +
                    "INNER JOIN salesPartners ON invoiceLocalDB.salesPartnerName LIKE salesPartners.Наименование " +
                    "AND invoiceLocalDB.areaSP LIKE salesPartners.Район AND invoiceLocalDB.accountingTypeSP " +
                    "LIKE salesPartners.Учет WHERE invoiceLocalDB.invoiceNumber LIKE ? ";
//            String sql = "SELECT * FROM invoiceLocalDB WHERE invoiceLocalDB.invoiceNumber LIKE ? ";
            Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
            if (c.moveToFirst()) {
                int invoiceNumberLocalTmp = c.getColumnIndex("invoiceNumber");
                int agentIDTmp = c.getColumnIndex("agentID");
                int areaSPTmp = c.getColumnIndex("areaSP");
                int salesPartnerIDTmp = c.getColumnIndex("serverDB_ID");
                int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                int accountingTypeSPTmp = c.getColumnIndex("accountingTypeSP");
                int itemNameTmp = c.getColumnIndex("itemName");
                int quantityTmp = c.getColumnIndex("quantity");
                int priceTmp = c.getColumnIndex("price");
                int totalCostTmp = c.getColumnIndex("totalCost");
                int exchangeTmp = c.getColumnIndex("exchangeQuantity");
                int returnTmp = c.getColumnIndex("returnQuantity");
                int dateTimeDocLocalTmp = c.getColumnIndex("dateTimeDocLocal");
                int invoiceSumTmp = c.getColumnIndex("invoiceSum");
                int commentTmp = c.getColumnIndex("comment");
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                summaryListTmp.add(c.getString(salesPartnerNameTmp));
                accTypeListTmp.add(c.getString(accountingTypeDocTmp));
                accTypeSPListTmp.add(c.getString(accountingTypeSPTmp));
                invoiceNumberListTmp.add(c.getString(invoiceNumberLocalTmp));
                do {
                    Integer invoiceNumberLocal = Integer.parseInt(c.getString(invoiceNumberLocalTmp));
                    Integer agentID = Integer.parseInt(c.getString(agentIDTmp));
                    Integer areaSP = c.getInt(areaSPTmp);
                    Integer salesPartnerID = c.getInt(salesPartnerIDTmp);
                    String accountingTypeDoc = c.getString(accountingTypeDocTmp);
                    String accountingTypeSP = c.getString(accountingTypeSPTmp);
                    String itemName = c.getString(itemNameTmp);
                    Double quantity = Double.parseDouble(c.getString(quantityTmp));
                    Double price = Double.parseDouble(c.getString(priceTmp));
                    Double totalCost = Double.parseDouble(c.getString(totalCostTmp));
                    Double exchangeQuantity = Double.parseDouble(c.getString(exchangeTmp));
                    Double returnQuantity = Double.parseDouble(c.getString(returnTmp));
                    String dateTimeDocLocal = c.getString(dateTimeDocLocalTmp);
                    Double invoiceSum = Double.parseDouble(c.getString(invoiceSumTmp));
                    String comment = c.getString(commentTmp);

                    Log.d(LOG_TAG, "invoiceNumber: " + invoiceNumberLocal.toString());

                    DataInvoice dt = new DataInvoice(accountingTypeDoc, accountingTypeSP,
                            itemName, dateTimeDocLocal, comment, salesPartnerID, invoiceNumberLocal, agentID, areaSP, price,
                            quantity, totalCost, exchangeQuantity, returnQuantity, invoiceSum);
                    dataArray.add(dt);

                } while (c.moveToNext());
            }
            c.close();
        }
        summaryList = new String[summaryListTmp.size()];
        for (int i = 0; i < summaryListTmp.size(); i++) {
            summaryList[i] = "№." + invoiceNumberListTmp.get(i) + " " + summaryListTmp.get(i);
//                    + " accSP: " + accTypeSPListTmp.get(i) + " accDoc: " + accTypeListTmp.get(i);
        }

        if (saveMenuTrigger == false) {
            sendToServer();
        }
        saveMenuTrigger = false;
    }

    private void listWithoutIDFilter(){
        for (int i = 0; i < invoiceNumbersList.size(); i++){
            String sql = "SELECT * FROM invoiceLocalDB WHERE invoiceLocalDB.invoiceNumber LIKE ? ";
            Cursor c = db.rawQuery(sql, new String[]{invoiceNumbersList.get(i).toString()});
            if (c.moveToFirst()) {
                int invoiceNumberLocalTmp = c.getColumnIndex("invoiceNumber");
                int accountingTypeDocTmp = c.getColumnIndex("accountingTypeDoc");
                int accountingTypeSPTmp = c.getColumnIndex("accountingTypeSP");
                int salesPartnerNameTmp = c.getColumnIndex("salesPartnerName");
                summaryListTmpSecond.add(c.getString(salesPartnerNameTmp));
                accTypeListTmpSecond.add(c.getString(accountingTypeDocTmp));
                accTypeSPListTmpSecond.add(c.getString(accountingTypeSPTmp));
                invoiceNumberListTmpSecond.add(c.getString(invoiceNumberLocalTmp));
                do {

                } while (c.moveToNext());
            }
            c.close();
        }
        summaryListSecond = new String[summaryListTmpSecond.size()];
        for (int i = 0; i < summaryListTmpSecond.size(); i++) {
            summaryListSecond[i] = "№." + invoiceNumberListTmpSecond.get(i) + " " + summaryListTmpSecond.get(i) +
                    System.lineSeparator() + "тип Точки: " + accTypeSPListTmpSecond.get(i) + System.lineSeparator() +
                    "тип документа: " + accTypeListTmpSecond.get(i);
        }
    }

    private void sendToServer(){
        Gson gson = new Gson();
        final String newDataArray = gson.toJson(dataArray);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        StringRequest request = new StringRequest(Request.Method.POST,
                requestUrlSaveRecord, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try{
                    JSONArray jsonArray = new JSONArray(response);
                    requestMessage = new String[jsonArray.length()];

                    if (jsonArray.length() > 0){
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            requestMessage[i] = obj.getString("requestMessage");
                        }
                        if (requestMessage[0].equals("New record created successfully")){
                            builder.setTitle("Поздравляю")
                                    .setMessage("Синхронизировано успешно")
                                    .setCancelable(false)
                                    .setNegativeButton("Круто",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
//                                                    invoiceNumbersList.clear();
                                                    finish();
                                                    dialog.cancel();
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    } else {
                        builder.setTitle("Внимание")
                                .setMessage("Возможно все синхронизировано, но сервер выкаблучивается. Обратитесь к Создателю и больше не жмите до талого!")
                                .setCancelable(false)
                                .setNegativeButton("Ясно",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                finish();
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                        Toast.makeText(getApplicationContext(), "Ошибка загрузки. Проверьте Интернет или Учётку", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e1) {
                    e1.printStackTrace();
                }
                Log.d("response", "result: " + response);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                onConnectionFailed();
                Toast.makeText(getApplicationContext(), "Сообщите об этой ошибке. Код 001", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "Error " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("dbName", dbName);
                parameters.put("dbUser", dbUser);
                parameters.put("dbPassword", dbPassword);
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

    private void clearTable(String tableName){
        Log.d(LOG_TAG, "--- Clear " + tableName + " : ---");
        // удаляем все записи
        int clearCount = db.delete(tableName, null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);
        Toast.makeText(getApplicationContext(), "<<< Таблицы очищены >>>", Toast.LENGTH_SHORT).show();
    }
}
