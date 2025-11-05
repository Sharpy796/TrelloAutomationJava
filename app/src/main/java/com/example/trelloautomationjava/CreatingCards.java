package com.example.trelloautomationjava;


import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

interface OnTitleReceivedListener {
    void onTitleReceived(String title);
}

public class CreatingCards {

//    curl 'https://api.trello.com/1/boards/6040fb56e1cc44275b26b304/lists/open?key=03c5d67dcea27e151276beb83bba0276&token=ATTA1cbc8390856886fbe9d0e86fe73671511fae892672664d2e8989274217144e0aE5CC667D'
    public final String TRELLO_ENDPOINT = "https://api.trello.com/1/";
    public final String TRELLO_KEY = "03c5d67dcea27e151276beb83bba0276";
    public final String TRELLO_TOKEN = "ATTA1cbc8390856886fbe9d0e86fe73671511fae892672664d2e8989274217144e0aE5CC667D";
    public final String TRELLO_AUTH = "?key="+TRELLO_KEY+"&token="+TRELLO_TOKEN;
    public final String TRELLO_PLANNER_ID = "6040fb56e1cc44275b26b304";
    public final String TRELLO_LIST_MONDAY = "6040fb65d9906a3b4da09cbd";
    public final String[] HOUR_ARR_STR = {"01","02","03","04","05","06","07","08","09","10","11","12"};
//    publ c final String[]  MIN_ARR_STR = {"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59"};
    public final String[] MIN_ARR_STR = {"00","05","10","15","20","25","30","35","40","45","50","55"};
    public final int[] HOUR_ARR_INT = {1,2,3,4,5,6,7,8,9,10,11,12};
    public final int[] MIN_ARR_INT = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59};
    public final String LOG_TAG = "HELLO_WORLD";
    OkHttpClient client = new OkHttpClient();

    private void fixStrictMode() {
        StrictMode.ThreadPolicy gfgPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(gfgPolicy);
    }
    /**
     *
     * @return A JSONArray file
     */
    public JSONArray getLists() throws IOException {
        fixStrictMode();
        String get_endpoint = TRELLO_ENDPOINT+"boards/"+TRELLO_PLANNER_ID+"/lists/open"+TRELLO_AUTH;
        Request request = new Request.Builder().url(get_endpoint).build();
        Call call = client.newCall(request);
        Log.i(LOG_TAG, call.toString());
        Response response = call.execute();
        JSONArray json = null;
        try {
            json = new JSONArray(response.body().string());
        } catch (JSONException | IOException e) {
            Log.e(LOG_TAG, e.toString());
        }
        return json;
    }

    /**
     *
     * @return A JSONArray of labels
     */
    public JSONArray getLabels() throws IOException, JSONException {
        fixStrictMode();
        String get_endpoint = TRELLO_ENDPOINT+"boards/"+TRELLO_PLANNER_ID+"/labels"+TRELLO_AUTH;
        Request request = new Request.Builder().url(get_endpoint).build();
        Call call = client.newCall(request);
        Log.i(LOG_TAG, call.toString());
        Response response = call.execute();
        return new JSONArray(response.body().string());
    }

    public String getItemFromJson(JSONArray json, String search_attr, String search_name, String return_attr) throws JSONException {
        JSONObject temp = null;
        for (int i = 0; i < json.length(); i++) {
            temp = json.getJSONObject(i);
            if (temp == null) {
                Log.w(LOG_TAG, "did temp fail?");
            } else if (temp.getString(search_attr).equals(search_name)) {
                Log.i(LOG_TAG, "Found return value:\t"+temp.getString(return_attr));
                return temp.getString(return_attr);
            }
        }
        Log.w(LOG_TAG, "Failed to find return value.");
        return null;
    }

    public JSONObject getItemFromJson(JSONArray json, String search_attr, String search_name) throws JSONException {
        JSONObject temp = null;
        for (int i = 0; i < json.length(); i++) {
            temp = json.getJSONObject(i);
            if (temp.getString(search_attr) == search_name) {
                Log.i(LOG_TAG, "Found return value:\t"+temp);
                return temp;
            }
        }
        Log.w(LOG_TAG, "Failed to find return value.");
        return null;
    }

    public Stack getArrayFromJson(JSONArray json, String search_attr, String[] search_names, String return_attr) throws JSONException {
        Stack newArray = new Stack();
        JSONObject temp = null;
        for (int i = 0; i < json.length(); i++) {
            temp = json.getJSONObject(i);
            if (temp == null) {
                Log.w(LOG_TAG, "did temp fail?");
            } else if (Arrays.asList(search_names).contains(temp.getString(search_attr))) {
                Log.i(LOG_TAG, "Found return value:\t" + temp.getString(return_attr));
                newArray.push(temp.getString(return_attr));
            }
        }
        return newArray;
    }

    private String stackToString(Stack stack) {
        String val = "";
        boolean firstTime = true;
        for (Object each : stack) {
            if (!firstTime && each != "") {
                val += ",";
            }
            firstTime = false;
            val += each;
        }
        Log.i(LOG_TAG,"Converting stack to string:\t"+val);
        return val;
    }

    public void createTrelloCard(String name, String list, String desc, String[] labels, String dueDate) {
        try {
            fixStrictMode();
            String create_card_endpoint = TRELLO_ENDPOINT+"cards";
            Map<String, String> jsonMap = new HashMap<>();
            RequestBody card = new FormBody.Builder()
                    .add("key", TRELLO_KEY)
                    .add("token", TRELLO_TOKEN)
                    .add("idList",getItemFromJson(getLists(), "name", list, "id"))
                    .add("name", name)
                    .add("desc", desc)
                    .add("idLabels", stackToString(getArrayFromJson(getLabels(), "name", labels, "id")))
//                    .add("due", dueDate)
                    // TODO: Add checklist functionality
                    .build();
            Request request = new Request.Builder()
                    .url(create_card_endpoint)
                    .post(card)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            Log.i(LOG_TAG,"Card created!");
            Log.i(LOG_TAG,response.body().string());
        } catch (IOException | JSONException | RuntimeException e) {
            Log.e(LOG_TAG, "Failed creating card");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString(); // stack trace as a string
            Log.e(LOG_TAG, sStackTrace);
            Log.e(LOG_TAG, e.toString());
        }
    }

    public void createTrelloCard(String name, String list, String desc, String[] labels) {
        createTrelloCard(name, list, desc, labels, null);
    }
    public void createTrelloCard(String name, String list, String desc) {
        createTrelloCard(name, list, desc, new String[0], null);
    }
    public void createTrelloCard(String name, String list) {
        createTrelloCard(name, list, "", new String[0], null);
    }
}
