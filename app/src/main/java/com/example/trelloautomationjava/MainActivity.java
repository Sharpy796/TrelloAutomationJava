package com.example.trelloautomationjava;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.DialogInterface;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Collections;
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
    final Handler CreateCardRunnableHandler = new Handler();

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

        setUpDropdown(R.id.lists, LISTS, getStringFromStrings(R.string.chosen_list));
        setUpDropdownCheckbox(R.id.labels, LABELS, getStringFromStrings(R.string.chosen_labels));
        setUpCheckBox();
        setUpClock();
        giveCreateCardButtonFunctionality();
    }

    private String getStringFromStrings(int id) {
        return getResources().getString(id);
    }

    private String getTextFromTextView(int id) {
        TextView text = (TextView)findViewById(id);
        return text.getText().toString();
    }

    private String getTextFromSpinner(int id) {
        Spinner spinner_house = (Spinner) findViewById(id);
        return spinner_house.getSelectedItem().toString();
    }

    private void setUpDropdown(int id, String[] arr, String title) {
        TextView textView = findViewById(id);

        boolean[] selecteditems = new boolean[arr.length];
        ArrayList<Integer> itemList = new ArrayList<>();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                // Set title
                builder.setTitle(title);

                builder.setSingleChoiceItems(arr, 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        textView.setText(arr[which]);
                    }
                });

                builder.setMultiChoiceItems(arr, selecteditems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position in itemList
                            itemList.add(i);
                            // Sort array list
                            Collections.sort(itemList);
                        } else {
                            // when checkbox unselected
                            // Remove position from itemList
                            itemList.remove(Integer.valueOf(i));
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });
    }

    private void setUpDropdownCheckbox(int id, String[] arr, String title) {
        TextView textView = findViewById(id);

        boolean[] selecteditems = new boolean[arr.length];
        ArrayList<Integer> itemList = new ArrayList<>();

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                // Set title
                builder.setTitle(title);

                // set dialog non cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(arr, selecteditems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position in itemList
                            itemList.add(i);
                            // Sort array list
                            Collections.sort(itemList);
                        } else {
                            // when checkbox unselected
                            // Remove position from itemList
                            itemList.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // use for loop
                        for (int j = 0; j < itemList.size(); j++) {
                            // concat array value
                            stringBuilder.append(arr[itemList.get(j)]);
                            // check condition
                            if (j != itemList.size() - 1) {
                                // When j value not equal
                                // to itemList size - 1
                                // add comma
                                stringBuilder.append(", ");
                            }
                        }
                        // set text on textView
                        textView.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        dialogInterface.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // use for loop
                        for (int j = 0; j < selecteditems.length; j++) {
                            // remove all selection
                            selecteditems[j] = false;
                            // clear language list
                            itemList.clear();
                            // clear text view value
                            textView.setText("");
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });
    }

    private void postAlertToButton(Button button, String message, int delayMillis) {
        button.setText(message);
        Runnable runnable = () -> button.setText(getStringFromStrings(R.string.create_card_button));
        CreateCardRunnableHandler.removeCallbacksAndMessages(null);
        CreateCardRunnableHandler.postDelayed(runnable, delayMillis);
    }

    private void handleDueDateText(CheckBox checkBox) {
        handleDueDateText(checkBox);
    }

    private void setUpCheckBox() {
        // Declare variables
        // Initialize views
        CheckBox checkBox = findViewById(R.id.duedateenabled);
        Button dueDateButton = findViewById(R.id.duedate);

        // Set initial checkbox status
        if (checkBox.isChecked()) {
            dueDateButton.setEnabled(true);
            dueDateButton.setText(parseDueDateVisual());
        } else {
            dueDateButton.setEnabled(false);
            dueDateButton.setText("No Due Date");
        }

        // Set listener for checkbox changes
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    dueDateButton.setEnabled(true);
                    dueDateButton.setText(parseDueDateVisual());
                } else {
                    dueDateButton.setEnabled(false);
                    dueDateButton.setText("No Due Date");
                }
            }
        });
    }

    private String parseDueDateVisual() {
        return "WIP DUE DATE";
    }

    private void setUpClock() {
        // Initialize TextView and TimePicker from layout
        TextView textView = findViewById(R.id.duedate);
        TimePicker timePicker = findViewById(R.id.timePicker);

        // Set a listener for when the time changes
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay;
                String amPm;

                // Determine AM or PM and adjust hour
                if (hour == 0) {
                    hour += 12;
                    amPm = "AM";
                } else if (hour == 12) {
                    amPm = "PM";
                } else if (hour > 12) {
                    hour -= 12;
                    amPm = "PM";
                } else {
                    amPm = "AM";
                }

                // Format hour and minute for display
                String formattedHour = (hour < 10) ? "0" + hour : String.valueOf(hour);
                String formattedMinute = (minute < 10) ? "0" + minute : String.valueOf(minute);

                // Display the selected time
                String msg = "Time is: " + formattedHour + " : " + formattedMinute + " " + amPm;
                textView.setText(msg);
                textView.setVisibility(ViewGroup.VISIBLE);
            }
        });
    }

    private void giveCreateCardButtonFunctionality() {
        Button createCardButton = (Button) findViewById(R.id.chosen_list_button);
        createCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = getTextFromTextView(R.id.name);
                String desc = getTextFromTextView(R.id.description);
                String list = getTextFromTextView(R.id.lists);
                String[] labels = getTextFromTextView(R.id.labels).split(", ");

                String errmsg = null;
                try {
                    if (name.isEmpty()) {
                        errmsg = "Please enter a card name.";
                    } else {
                        createCardButton.setText("Creating Card...");
                        cardCreator.createTrelloCard(name, list, desc, labels);
                        postAlertToButton(createCardButton, "Card Created!", 3000);
                    }
                } catch (Exception e) {
                    errmsg = e.toString();
                }
                if (errmsg != null) {
                    Log.w(LOG_TAG, "Couldn't create card.");
                    Log.w(LOG_TAG, errmsg);
                    postAlertToButton(createCardButton, errmsg, 1500);
                }
            }
        });
    }
}