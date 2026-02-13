package com.example.trelloautomationjava;

import android.animation.TimeInterpolator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Carousel;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    final LocalDateTime todayDate = getToday();
    int[] dueDate = new int[6];

    MyRecyclerViewAdapter adapter;

    public MainActivity() throws JSONException, IOException {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updateDueDateValue(todayDate.getYear(), todayDate.getMonthValue(), todayDate.getDayOfMonth(), 19, 0, 1);
        }
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setUpListPicker(R.id.listPicker, LISTS, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ? todayDate.getDayOfWeek().getValue()-1 : 0, Gravity.LEFT);
        setUpDropdownCheckbox(R.id.labels, LABELS, getStringFromStrings(R.string.chosen_labels));
        setUpCheckBox();
        setUpDueDateButton();
        giveCreateCardButtonFunctionality();
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    private LocalDateTime getToday() {
        LocalDateTime today = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            today = LocalDateTime.now();
        } else {
            Log.w(LOG_TAG, "Picking today didn't work.");
        }
        return today;
    }

    private void openDueDatePicker() {
//        LocalDateTime today = getToday();
        LayoutInflater inflater = LayoutInflater.from(this); // or getLayoutInflater() in an Activity
        View linlayout = inflater.inflate(R.layout.datepicker_view, null); // The second argument is the parent ViewGroup, null for now.
        DatePicker dp = (DatePicker) linlayout.findViewById(R.id.datepicker);
        dp.updateDate(dueDate[0], dueDate[1]-1, dueDate[2]);
        FlexibleNumberPicker hp = (FlexibleNumberPicker) linlayout.findViewById(R.id.hours);
        FlexibleNumberPicker mp = (FlexibleNumberPicker) linlayout.findViewById(R.id.mins);
        setUpListPicker(hp, HOUR_ARR_STR, dueDate[3]-(dueDate[3] > 12 ? 13 : 1), Gravity.CENTER_HORIZONTAL);
        setUpListPicker(mp, MIN_ARR_STR, dueDate[4]/5, Gravity.CENTER_HORIZONTAL);
        Button buttonAM = linlayout.findViewById(R.id.button_am);
        Button buttonPM = linlayout.findViewById(R.id.button_pm);
        Button buttonToday = linlayout.findViewById(R.id.button_today);

        int[] ampm = new int[1];
        ampm[0] = dueDate[5];

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
                ampm[0] = 1;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dp.updateDate(todayDate.getYear(), todayDate.getMonthValue()-1, todayDate.getDayOfMonth());
                } else {
                    Log.w(LOG_TAG, "BWAHHHH I HATE THISSS");
                }
            }
        });

        if (ampm[0] == 0) {
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
                ampm[0] = 0;
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
                ampm[0] = 1;
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
                    if (ampm[0] == 0) {hourOfDay -= 12;}
                } else {hourOfDay += 12*(ampm[0]);}
                Log.w(LOG_TAG, "hour:"+hourOfDay);
                Log.w(LOG_TAG, "ampm:"+ampm[0]);
                int minute = MIN_ARR_INT[mp.getValue()];
                updateDueDateText(year, monthOfYear, dayOfMonth, hourOfDay, minute);
                updateDueDateValue(year, monthOfYear, dayOfMonth, hourOfDay, minute, ampm[0]);
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
        updateDueDateText(dueDate[0],dueDate[1],dueDate[2],dueDate[3],dueDate[4]);
    }

    private void updateDueDateValue(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minute, int ampm) {
        dueDate[0] = year;
        dueDate[1] = monthOfYear;
        dueDate[2] = dayOfMonth;
        dueDate[3] = hourOfDay;
        dueDate[4] = minute;
        dueDate[5] = ampm; // 0 = AM, 1 = PM
    }

    private String parseDueDate() {
        // 2025-11-14T19:00:00-06:00
        String message = "";
        message += dueDate[0]+"-"; // year
        if (dueDate[1] < 10) { // month
            message += "0";
        } message += dueDate[1]+"-";
        if (dueDate[2] < 10) { // day
            message += "0";
        } message += dueDate[2]+"T";
        if (dueDate[3] < 10) { // hour
            message += "0";
        } message += dueDate[3]+":";
        if (dueDate[4] < 10) { // minute
            message += "0";
        } message += dueDate[4]+":00-06:00";
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

            picker.setOnValueChangedListener((np, ildVal, newVal) -> {
                String selected = arr[newVal];
                if (!selected.isEmpty()) {
                    Log.d(LOG_TAG, "Selected word: " + selected);
                }
            });
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
                }

                String errmsg = null;
                try {
                    if (name.isEmpty()) {
                        errmsg = "Please enter a card name.";
                        delayMills = 1500;
                    } else {
                        createCardButton.setText("Creating Card...");
                        cardCreator.createTrelloCard(name, list, desc, labels, date);
                        postAlertToButton(createCardButton, "Card Created!", delayMills, 1);
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