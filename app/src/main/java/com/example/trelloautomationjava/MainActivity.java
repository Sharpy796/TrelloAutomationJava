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

public class MainActivity extends AppCompatActivity {



    int oldValue;
    public final String LOG_TAG = "HELLO_WORLD";
    CreatingCards cardCreator = new CreatingCards();
    final String[] LISTS = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday","Later"};

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
//            Log.i(LOG_TAG, "success");
//            Log.i(LOG_TAG, json.toString());
//        try {
//            Log.i("HELLO_WORLD", "trying");
//            cardCreator.getItemFromJson(json, "name", "test");
//            Log.i("HELLO_WORLD", "did it");
//        } catch (JSONException e) {
//            Log.e("HELLO_WORLD", "failed");
//            Log.e("HELLO_WORLD", e.toString());
//        } finally {
//            Log.i("HELLO_WORLD", "tried");
//        }


        giveCreateCardButtonFunctionality();
//        changeTextOnce();
        setUpDropdown();
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

    private void setUpDropdown() {
        Spinner mySpinner = findViewById(R.id.spinner_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, LISTS);
        mySpinner.setAdapter(adapter);
    }
}