package com.example.myapplicationtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class CreateInvoiceFilterFirstActivity extends AppCompatActivity implements View.OnClickListener{

    ListView listViewArea, listViewAccountingType, listViewDayOfTheWeek;
    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice_filter_first);

        btnNext = findViewById(R.id.buttonNext);

        //Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String agentName = intent.getStringExtra(MainMenu.EXTRA_AGENTNAMENEXT);

        //Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textViewAgent);
        textView.setText(agentName);

        listViewArea = findViewById(R.id.listViewArea);
        listViewAccountingType = findViewById(R.id.listViewAccountingType);
        listViewDayOfTheWeek = findViewById(R.id.listViewDayOfTheWeek);

        loadListArea();
        loadListAccountingType();
        loadListDayOfTheWeek();

        listViewArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // selected item
                String product = ((TextView) view).getText().toString();

                Toast.makeText(getApplicationContext(), "Selected Item :" +product, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadListArea(){
        String[] areas = new String[5];
        for (int i = 0; i < 5; i++) {

            areas[i] = "Район № " + (i + 1);
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, areas);
        listViewArea.setAdapter(arrayAdapter);
    }

    private void loadListAccountingType(){
        String[] accountingTypes = new String[2];
        accountingTypes[0] = "Тип учёта ПРОВОД";
        accountingTypes[1] = "Тип учёта НЕпровод";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, accountingTypes);
        listViewAccountingType.setAdapter(arrayAdapter);
    }

    private void loadListDayOfTheWeek(){
        String[] dayOfTheWeek = new String[3];
        dayOfTheWeek[0] = "Понедельник-Пятница";
        dayOfTheWeek[1] = "Вторник-Четверг";
        dayOfTheWeek[2] = "Среда";
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dayOfTheWeek);
        listViewDayOfTheWeek.setAdapter(arrayAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonNext:
                filterFirst();
                break;
            default:
                break;
        }
    }

    private void filterFirst(){


    }
}
