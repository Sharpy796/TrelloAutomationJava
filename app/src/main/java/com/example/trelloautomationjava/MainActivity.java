package com.example.trelloautomationjava;

import static java.time.ZonedDateTime.now;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {
    public final String LOG_TAG = "HELLO_WORLD";
    CreatingCards cardCreator = new CreatingCards();
    final String[] LISTS = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday","Later"};

    String[] STUFF = new String[1];
    final String[] LABELS = cardCreator.getArrayFromJson(cardCreator.getLabels(), "name").toArray(STUFF);
    public final String[] HOUR_ARR_STR = {"01","02","03","04","05","06","07","08","09","10","11","12"};
//    public final String[]  MIN_ARR_STR = {"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59"};
    public final String[] MIN_ARR_STR = {"00","05","10","15","20","25","30","35","40","45","50","55"};
    public final int[] HOUR_ARR_INT = {1,2,3,4,5,6,7,8,9,10,11,12};
    public final int[] MIN_ARR_INT = {0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55};
    final Handler CreateCardRunnableHandler = new Handler();
    ZoneId zoneId = ZoneId.of("America/Chicago");
    final ZonedDateTime todayDate = now(zoneId);
    ZonedDateTime dueDate = now(zoneId);
    DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    MyRecyclerViewAdapter adapter;

    public MainActivity() throws JSONException, IOException {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        updateDueDateValue(todayDate.getYear(), todayDate.getMonthValue(), todayDate.getDayOfMonth(), 19, 0);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setUpListPicker(R.id.listPicker, LISTS, todayDate.getDayOfWeek().getValue()-1, Gravity.LEFT);
        setUpDropdownCheckbox(R.id.labels, LABELS, getStringFromStrings(R.string.chosen_labels));
        setUpCheckBox();
        setUpDueDateButton();
        giveCreateCardButtonFunctionality();
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    private void openDueDatePicker() {
        LayoutInflater inflater = LayoutInflater.from(this); // or getLayoutInflater() in an Activity
        View linlayout = inflater.inflate(R.layout.datepicker_view, null); // The second argument is the parent ViewGroup, null for now.
        DatePicker dp = (DatePicker) linlayout.findViewById(R.id.datepicker);
        dp.updateDate(dueDate.getYear(), dueDate.getMonthValue()-1, dueDate.getDayOfMonth());
        FlexibleNumberPicker hp = (FlexibleNumberPicker) linlayout.findViewById(R.id.hours);
        FlexibleNumberPicker mp = (FlexibleNumberPicker) linlayout.findViewById(R.id.mins);
        setUpListPicker(hp, HOUR_ARR_STR, dueDate.getHour()-(dueDate.getHour() > 12 ? 13 : 1), Gravity.CENTER_HORIZONTAL);
        setUpListPicker(mp, MIN_ARR_STR, dueDate.getMinute()/5, Gravity.CENTER_HORIZONTAL);
        Button buttonAM = linlayout.findViewById(R.id.button_am);
        Button buttonPM = linlayout.findViewById(R.id.button_pm);
        Button buttonToday = linlayout.findViewById(R.id.button_today);

        int[] is_pm = new int[1];
        is_pm[0] = (dueDate.getHour() >= 12 ? 1 : 0);

        buttonToday.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "reset to today");
                hp.setValue(6);
                mp.setValue(0);
                buttonPM.setBackgroundTintList(getResources().getColorStateList(R.color.button_selected, null));
                buttonPM.setTextColor(getResources().getColorStateList(R.color.text_success, null));
                buttonAM.setBackgroundTintList(getResources().getColorStateList(R.color.button_unselected, null));
                buttonAM.setTextColor(getResources().getColorStateList(R.color.text_danger, null));
                is_pm[0] = 1;
                dp.updateDate(todayDate.getYear(), todayDate.getMonthValue()-1, todayDate.getDayOfMonth());
            }
        });

        if (is_pm[0] == 0) {
            buttonAM.setBackgroundTintList(getResources().getColorStateList(R.color.button_selected, null));
            buttonAM.setTextColor(getResources().getColorStateList(R.color.text_success, null));
            buttonPM.setBackgroundTintList(getResources().getColorStateList(R.color.button_unselected, null));
            buttonPM.setTextColor(getResources().getColorStateList(R.color.text_danger, null));
        }

        buttonAM.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "am pressed");
                buttonAM.setBackgroundTintList(getResources().getColorStateList(R.color.button_selected, null));
                buttonAM.setTextColor(getResources().getColorStateList(R.color.text_success, null));
                buttonPM.setBackgroundTintList(getResources().getColorStateList(R.color.button_unselected, null));
                buttonPM.setTextColor(getResources().getColorStateList(R.color.text_danger, null));
                is_pm[0] = 0;
            }
        });
        buttonPM.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "pm pressed");
                buttonPM.setBackgroundTintList(getResources().getColorStateList(R.color.button_selected, null));
                buttonPM.setTextColor(getResources().getColorStateList(R.color.text_success, null));
                buttonAM.setBackgroundTintList(getResources().getColorStateList(R.color.button_unselected, null));
                buttonAM.setTextColor(getResources().getColorStateList(R.color.text_danger, null));
                is_pm[0] = 1;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Due Date");
        builder.setView(linlayout);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int year = dp.getYear();
                int monthOfYear = dp.getMonth()+1;
                int dayOfMonth = dp.getDayOfMonth();
                int hourOfDay = HOUR_ARR_INT[hp.getValue()];
                if (hourOfDay == 12) {
                    if (is_pm[0] == 0) {hourOfDay -= 12;}
                } else {hourOfDay += 12*(is_pm[0]);}
                int minute = MIN_ARR_INT[mp.getValue()];
                updateDueDateText(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                updateDueDateValue(year, monthOfYear, dayOfMonth, hourOfDay, minute);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    private void updateDueDateText() {
        updateDueDateText(
                dueDate.getYear(),
                dueDate.getMonthValue(),
                dueDate.getDayOfMonth(),
                dueDate.getHour(),
                dueDate.getMinute());
    }

    private void updateDueDateValue(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute) {
        dueDate = ZonedDateTime.of(year,monthOfYear,dayOfMonth,hourOfDay,minute,0,0,zoneId);
    }

    private String parseDueDate() {
        // 2025-11-14T19:00:00-06:00
        String message = dateFormatter.format(dueDate);
        Log.i(LOG_TAG, "Date:\t"+message);
        return message;
    }

    private void updateDueDateText(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute) {
        Button dueDateButton = (Button) findViewById(R.id.duedate);
        String message = monthOfYear+"/"+dayOfMonth+"/"+year+" @";

        String AMPM = "AM";
        if (hourOfDay > 11) {
            hourOfDay -= 12;
            AMPM = "PM";
        }
        if (hourOfDay == 0) {
            hourOfDay = 12;
        }

        if (hourOfDay < 10) {
            message += "0";
        } message += hourOfDay+":";

        if (minute < 10) {
            message += "0";
        } message += minute+" ";
        message += AMPM;
        dueDateButton.setText(message);
    }

    private String getStringFromStrings(int id) {
        return getResources().getString(id);
    }

    private String getTextFromTextView(int id) {
        TextView text = (TextView)findViewById(id);
        return text.getText().toString();
    }

    private void setUpListPicker(int id, String[] arr, int default_val, int gravity) {
        setUpListPicker((FlexibleNumberPicker) findViewById(id), arr, default_val, gravity);
    }

    private void setUpListPicker(FlexibleNumberPicker picker, String[] arr, int default_val, int gravity) {
        try {
            picker.setDisplayedValues(arr);
            picker.setMinValue(0);
            picker.setMaxValue(arr.length - 1);
            picker.setValue(default_val);
            picker.setWrapSelectorWheel(false);
            picker.setGravity(gravity);
            picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        } catch (Exception e) {
            Log.wtf(LOG_TAG, e);
        }
    }

    private String getSelectedList(int id) {
        FlexibleNumberPicker picker = (FlexibleNumberPicker) findViewById(id);
        return LISTS[picker.getValue()];
    }

    private void setUpDropdownCheckbox(int id, String[] arr, String title) {
        TextView textView = findViewById(id);

        boolean[] selecteditems = new boolean[arr.length];
        ArrayList<Integer> itemList = new ArrayList<>();

        textView.setOnClickListener(new OnClickListener() {
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

    private void postAlertToButton(Button button, String message, int delayMillis, int code) {
        button.setText(message);
        if (code == 1) {

            button.setBackgroundTintList(getResources().getColorStateList(R.color.button_success, null));
            button.setTextColor(getResources().getColorStateList(R.color.text_success, null));
        } if (code == 2) {
            button.setBackgroundTintList(getResources().getColorStateList(R.color.button_danger, null));
            button.setTextColor(getResources().getColorStateList(R.color.text_danger, null));
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                button.setText(getStringFromStrings(R.string.create_card_button));
                button.setBackgroundTintList(getResources().getColorStateList(R.color.button_primary, null));
                button.setTextColor(getResources().getColorStateList(R.color.text_on_primary, null));
            }
        };
        CreateCardRunnableHandler.removeCallbacksAndMessages(null);
        CreateCardRunnableHandler.postDelayed(runnable, delayMillis);
    }

    private void setUpCheckBox() {
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
                    checkBox.setButtonTintList(getResources().getColorStateList(R.color.text_on_primary, null));
                    dueDateButton.setEnabled(true);
                    dueDateButton.setBackgroundTintList(getResources().getColorStateList(R.color.button_primary, null));
                    dueDateButton.setTextColor(getResources().getColorStateList(R.color.text_on_primary, null));
                    updateDueDateText();
                } else {
                    checkBox.setButtonTintList(getResources().getColorStateList(android.R.color.darker_gray, null));
                    dueDateButton.setEnabled(false);
                    dueDateButton.setBackgroundTintList(getResources().getColorStateList(R.color.grey, null));
                    dueDateButton.setTextColor(getResources().getColorStateList(android.R.color.background_dark, null));
                    dueDateButton.setText("No Due Date");
                }
            }
        });
    }

    private String parseDueDateVisual() {
        return "WIP DUE DATE";
    }

    private void setUpDueDateButton() {
        Button dueDateButton = (Button) findViewById(R.id.duedate);
        updateDueDateText();
        dueDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openDueDatePicker();
            }
        });
    }

    private void giveCreateCardButtonFunctionality() {
        Button createCardButton = (Button) findViewById(R.id.chosen_list_button);
        createCardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int delayMills = 3000;
                String name = getTextFromTextView(R.id.name);
                String desc = getTextFromTextView(R.id.description);
                String list = getSelectedList(R.id.listPicker);
                String[] labels = getTextFromTextView(R.id.labels).split(", ");
                String date = "";
                boolean dueDateEnabled = ((CheckBox)findViewById(R.id.duedateenabled)).isChecked();
                if (dueDateEnabled) {
                    date = parseDueDate();
                    Log.d(LOG_TAG,date);
                }

                String errmsg = null;
                try {
                    if (name.isEmpty()) {
                        errmsg = "Please enter a card name.";
                        delayMills = 1500;
                    } else {
                        createCardButton.setText("Creating Card...");
                        int errcode = cardCreator.createTrelloCard(name, list, desc, labels, date);
                        if (errcode == 200) {
                            postAlertToButton(createCardButton, "Card Created!", delayMills, 1);
                        } else {
                            errmsg = "Error Code "+errcode+" - ";
                            switch(errcode){
                                case 400:   errmsg += "Bad Response"; break;
                                default:    errmsg += "What is this code????";
                            }
                            postAlertToButton(createCardButton, errmsg, delayMills, 2);
                        }
                    }
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed creating card");
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String sStackTrace = sw.toString(); // stack trace as a string
                    Log.e(LOG_TAG, sStackTrace);
                    errmsg = e.toString();
                    Log.e(LOG_TAG, errmsg);
                    delayMills = 3000;
                }
                if (errmsg != null) {
                    Log.w(LOG_TAG, "Couldn't create card.");
                    Log.w(LOG_TAG, errmsg);
                    postAlertToButton(createCardButton, errmsg, delayMills, 2);
                }
            }
        });
    }
}