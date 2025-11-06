package com.example.trelloautomationjava;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {



    int oldValue;
    public final String LOG_TAG = "HELLO_WORLD";
    CreatingCards cardCreator = new CreatingCards();
    final String[] LISTS = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday","Later"};

    String[] STUFF = new String[1];
    final String[] LABELS = cardCreator.getArrayFromJson(cardCreator.getLabels(), "name").toArray(STUFF);
    public final String[] HOUR_ARR_STR = {"01","02","03","04","05","06","07","08","09","10","11","12"};
//    public final String[]  MIN_ARR_STR = {"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59"};
    public final String[] MIN_ARR_STR = {"00","05","10","15","20","25","30","35","40","45","50","55"};
    public final int[] HOUR_ARR_INT = {1,2,3,4,5,6,7,8,9,10,11,12};
    public final int[] MIN_ARR_INT = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59};

    public MainActivity() throws JSONException, IOException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        giveCreateCardButtonFunctionality();
//        changeTextOnce();
        setUpDropdown(R.id.lists, LISTS);
        setUpDropdown(R.id.labels, LABELS);
        setUpDropdown(R.id.hour, HOUR_ARR_STR);
        setUpDropdown(R.id.min, MIN_ARR_STR);
    }

    private void giveCreateCardButtonFunctionality() {
//        final String[] manyDifferentStrings = LISTS;

//        final TextView changingText = (TextView) findViewById(R.id.chosen_list_button);
        Button createCardButton = (Button) findViewById(R.id.chosen_list_button);

        createCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardCreator.createTrelloCard("test", "Monday", "test", new String[]{"TKE", "KoC"});
//                int random = (int) (Math.random() * manyDifferentStrings.length);
//                if (random == oldValue) {
//                    random = (int) (Math.random() * manyDifferentStrings.length);
//                }
//                changingText.setText(manyDifferentStrings[random]);
//                oldValue = random;
            }
        });
    }

    private void changeTextOnce() {
        final TextView changingText = (TextView) findViewById(R.id.chosen_list_button);
        Button changeTextButton = (Button) findViewById(R.id.chosen_list_button);

        changeTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changingText.setText("Hello");
            }
        });
    }

    private void setUpDropdown(int id, String[] list) {
        Spinner mySpinner = findViewById(id);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        mySpinner.setAdapter(adapter);
    }
}