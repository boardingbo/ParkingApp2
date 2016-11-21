package com.example.user.parkminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import static android.view.View.*;

//test VCS
//test VCS 2
//test VCS 3
public class ParkActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    protected TextView mNextParkingRestrictionTextView;
    protected Spinner mZonesSpinner;
    protected Spinner mStreetsSpinner;

    protected TextView mTestOutputTextView;

    protected NumberPicker mDayNumberPicker;
    protected NumberPicker mHourNumberPicker;
    protected NumberPicker mMinuteNumberPicker;

    private static final String DEBUG_TAG = "HttpExample";

    public String zonesSignIdArray[][];
    private String nextMoveTime = "";

    public static String parkedStreet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        mNextParkingRestrictionTextView = (TextView) findViewById(R.id.next_move_date_textView);
        mZonesSpinner = (Spinner) findViewById(R.id.spinner_zones);
        mStreetsSpinner = (Spinner) findViewById(R.id.spinner_streets);

        mTestOutputTextView = (TextView) findViewById(R.id.test_output_textView);

        mDayNumberPicker = (NumberPicker) findViewById(R.id.dayNumberPicker);
        mHourNumberPicker = (NumberPicker) findViewById(R.id.hourNumberPicker);
        mMinuteNumberPicker = (NumberPicker) findViewById(R.id.minuteNumberPicker);

        mDayNumberPicker.setMinValue(0);
        mDayNumberPicker.setMaxValue(7);

        mHourNumberPicker.setMinValue(0);
        mHourNumberPicker.setMaxValue(12);

        mMinuteNumberPicker.setMinValue(0);
        mMinuteNumberPicker.setMaxValue(60);

        addListenerOnSpinnerItemSelection();
        getNearbyStreets();
//        updateStreetsSpinner();
//        updateZonesSpinner();

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Park Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.user.parkminder/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Park Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.user.parkminder/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public void updateStreetsSpinner( ArrayAdapter<String> arrayAdapter) {

        // Specify the layout to use when the list of choices appears
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mStreetsSpinner.setAdapter(arrayAdapter);

    }

    public void updateZonesSpinner( ArrayAdapter<String> arrayAdapter) {

        // Specify the layout to use when the list of choices appears
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mZonesSpinner.setAdapter(arrayAdapter);

    }

    public void addListenerOnSpinnerItemSelection() {
        StreetSpinnerActivity listener = new StreetSpinnerActivity();
        mStreetsSpinner.setOnItemSelectedListener(listener);
        mStreetsSpinner.setOnTouchListener(listener);

        ZoneSpinnerActivity zoneListener = new ZoneSpinnerActivity();
        mZonesSpinner.setOnItemSelectedListener(zoneListener);
        mZonesSpinner.setOnTouchListener(zoneListener);

    }

    public void checkNextRestriction() {

        String nextRestrictionString = "";
        String zoneSelection;
        zoneSelection = String.valueOf(mZonesSpinner.getSelectedItem());

        Calendar cacheCalendar = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z"); //Wed, 4 Jul 2001 12:08:56 -0700
        String date = df.format(calendar.getTime());
        String moveDate;
        int currentHours = cacheCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = cacheCalendar.get(Calendar.MINUTE);
        int currentDayOfMonth = cacheCalendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = cacheCalendar.get(Calendar.MONTH);
        int year = cacheCalendar.get(Calendar.YEAR);


        int moveHoursOfDay;
        int moveMonth = cacheCalendar.get(Calendar.MONTH);
        int moveYear = cacheCalendar.get(Calendar.YEAR);
        int moveDayOfMonth = cacheCalendar.get(Calendar.DAY_OF_MONTH);

        switch (zoneSelection) {
            case "2 Hour Parking":
                if (currentHours < 15 && currentHours > 8) {
                    // The current time is before two hours before two restriction ends
                    // and after the beginning restriction time
                    // Set reminder in two hours.
                    moveHoursOfDay = currentHours + 2;
                } else {
                    // Current time is after two hours before two hour restriction ends
                    // Set reminder to two hours after two hour begins the next
                    // two hour parking day
                    moveHoursOfDay = 8 + 2;
                    moveDayOfMonth = currentDayOfMonth + 1;

                }

                cacheCalendar.set(Calendar.HOUR_OF_DAY, moveHoursOfDay);
                cacheCalendar.set(Calendar.MINUTE, 0);
                cacheCalendar.set(Calendar.DAY_OF_MONTH, moveDayOfMonth);
                moveDate = df.format(cacheCalendar.getTime());
                nextRestrictionString = (moveDate);

                break;
            case "Street Sweeping 1st Thursday":
                if (currentDayOfMonth > 7) {
                    // Find 1st Thursday in next month
                    moveMonth = currentMonth + 1;
                } else {
                    moveDayOfMonth = getNextFirstDayOfMonth(Calendar.THURSDAY, moveMonth, moveYear);
                    if (moveDayOfMonth > currentDayOfMonth) {
                        // The next restriction day has passed, move on to the next month
                        moveMonth = currentMonth + 1;
                    } else {
                        moveMonth = currentMonth;
                    }
                }

                moveDayOfMonth = getNextFirstDayOfMonth(Calendar.THURSDAY, moveMonth, moveYear);
                cacheCalendar.set(Calendar.DAY_OF_MONTH, moveDayOfMonth);
                cacheCalendar.set(Calendar.MONTH, moveMonth);
                cacheCalendar.set(Calendar.HOUR_OF_DAY, 8);
                cacheCalendar.set(Calendar.MINUTE, 0);
                cacheCalendar.set(Calendar.SECOND, 0);
                moveDate = df.format(cacheCalendar.getTime());

                nextRestrictionString = (moveDate);
                break;
            case "Street Sweeping 1st Friday":
                if (currentDayOfMonth > 7) {
                    // Find 1st Thursday in next month
                    moveMonth = currentMonth + 1;
                } else {
                    moveMonth = currentMonth;
                }

                moveDayOfMonth = getNextFirstDayOfMonth(Calendar.FRIDAY, moveMonth, moveYear);
                cacheCalendar.set(Calendar.DAY_OF_MONTH, moveDayOfMonth);
                cacheCalendar.set(Calendar.MONTH, moveMonth);
                cacheCalendar.set(Calendar.HOUR_OF_DAY, 8);
                cacheCalendar.set(Calendar.MINUTE, 0);
                cacheCalendar.set(Calendar.SECOND, 0);
                moveDate = df.format(cacheCalendar.getTime());

                nextRestrictionString = (moveDate);
                break;
        }
        mNextParkingRestrictionTextView.setText(nextRestrictionString);


    }


    public int getNextFirstDayOfMonth(int day, int month, int year) {

        Calendar cacheCalendar = Calendar.getInstance();
        int nextFirstDayOfMonth;
        cacheCalendar.set(Calendar.DAY_OF_WEEK, day);
        cacheCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);
        cacheCalendar.set(Calendar.MONTH, month);
        cacheCalendar.set(Calendar.YEAR, year);
        nextFirstDayOfMonth = cacheCalendar.get(Calendar.DAY_OF_MONTH);
        return nextFirstDayOfMonth;

    }

    public void getNextParkingRestriction(){

        // http://sinnestates.com/ParkingApp/Reminder_Select.php?SignIDs=SignID%3A%2C%2C757&format=html        // http://sinnestates.com/ParkingApp/Reminder_Select.php?SignIDs=SignID%3A%2C%2C757&format=html

        Long streetSpinnerId = mZonesSpinner.getSelectedItemId();
        // Pull sign IDs from second column of array
        String signIdString = zonesSignIdArray[streetSpinnerId.intValue()][1];
        // Parse signIdString string for 3 digit ID(s)

        String[] signIdArray = signIdString.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        ArrayList<String> signIdList = new ArrayList<String>();

        for (int i = 0; i < signIdArray.length; i++)
        {
            int signIdInt = 0;

            try {
                signIdInt = Integer.parseInt(signIdArray[i]);
            } catch(NumberFormatException nfe) {
                // Do something
            }
            if (signIdInt != 0){
                signIdList.add(String.valueOf(signIdInt));
            }
        }
        mTestOutputTextView.setText(signIdList.get(0));

        String stringUrl = "http://sinnestates.com/ParkingApp/Reminder_Select.php?SignIDs=SignID%3A"; //757%2C%2C&format=html";

        // add sign IDs to URL
        // URL will take up to 3 signs separated by commas
        // Still need commas even for less than 3 signs
        // for loop with 3 iterations so we can get the right number of commas and up to 3 sign IDs
        for (int i = 0; i < 3; i++){
            // check if there are sing IDs in the singIdList
            if (signIdList.size() > i){
                stringUrl = stringUrl + signIdList.get(i);
            }
            // add comma after the sign ID, but we only want two commas
            if (i < 3 - 1){
                stringUrl = stringUrl + "%2C";
            }
        }
        stringUrl = stringUrl + "&format=html";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetMoveTimeFromDatabase().execute(stringUrl);
            mTestOutputTextView.setText("Connected");
        } else {
            mTestOutputTextView.setText("No network connection available.");
        }
        String str = "";
        for (int i = 0; i < signIdList.size(); i++){
            str = str + signIdList.get(i);
        }
        mTestOutputTextView.setText(str);
    }

    public void nextParkingRestrictionButtonHandler(View view) {
        // Respond to Next Parking Restriction button
        getNextParkingRestriction();

    }

    public void getNearbyStreetsButtonHandler(View view) {
        // Respond to Get Nearby Streets button
        getNearbyStreets();
    }

    public void getNearbyStreets(){

//        String stringUrl =
//                "http://sinnestates.com/ParkingApp/Testing/testing.php?format=json";
        String longitude = String.valueOf(MainActivity.mCurrentLocation.getLongitude());
        String latitude = String.valueOf(MainActivity.mCurrentLocation.getLatitude());
        String stringUrl =
                "http://sinnestates.com/ParkingApp/Street_Select.php?Longitude=" + longitude + "&Latitude=" + latitude + "&Accuracy=47&format=json";
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetStreetValuesFromDatabase().execute(stringUrl);
            mTestOutputTextView.setText("Connected");
        } else {
            mTestOutputTextView.setText("No network connection available.");
        }

    }

    public void getZonesButtonHandler(View view) {
        // Respond to Get Zones button
        getZones();
    }

    public void getZones(){
//        URL to send to database
//        http://sinnestates.com/ParkingApp/Zone_Select.php?Longitude=-104.9497835&Street=Steele%20St&Latitude=39.737511000000005&format=json

        String longitude = String.valueOf(MainActivity.mCurrentLocation.getLongitude());
        String latitude = String.valueOf(MainActivity.mCurrentLocation.getLatitude());
        String street = mStreetsSpinner.getSelectedItem().toString();
        String stringUrl =
                "http://sinnestates.com/ParkingApp/Zone_Select.php?Longitude=" + longitude +
                        "&Street=" + street + "&Latitude=" + latitude + "&format=json";
        //replace the spaces in the street string from the street spinner
        stringUrl = stringUrl.replaceAll(" ", "%20");
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new GetZoneValuesFromDatabase().execute(stringUrl);
            mTestOutputTextView.setText("Connected");
        } else {
            mTestOutputTextView.setText("No network connection available.");
        }

    }

    public void setReminderButtonHandler (View view) {
        // Respond to the Set Reminder button

        long nextMoveDateInMillis = 0;
        long nextReminderDateInMillis;

        // Date format to match output from Database
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            Date mDate;
            mDate = dateFormat.parse(nextMoveTime);
            nextMoveDateInMillis = mDate.getTime();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        // Calculate the time to set the notification
        nextReminderDateInMillis =
                nextMoveDateInMillis
                        - TimeUnit.DAYS.toMillis((long) mDayNumberPicker.getValue())
                        - TimeUnit.HOURS.toMillis((long) mHourNumberPicker.getValue())
                        - TimeUnit.MINUTES.toMillis((long) mMinuteNumberPicker.getValue());

        Intent alertIntent = new Intent(this, AlertReceiver.class);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, nextReminderDateInMillis,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nextReminderDateInMillis);
        String str = dateFormat.format(calendar.getTime());

        mTestOutputTextView.setText("You will be reminded at " + str);

        parkedStreet = mStreetsSpinner.getSelectedItem().toString();
    }

    public void setTestReminder(View view) {

        Long alertTime = new GregorianCalendar().getTimeInMillis() + (5 * 1000);

        Intent alertIntent = new Intent(this, AlertReceiver.class);

        AlarmManager alarmManger = (AlarmManager)
                getSystemService(Context.ALARM_SERVICE);

        alarmManger.set(AlarmManager.RTC_WAKEUP, alertTime,
                PendingIntent.getBroadcast(this, 1, alertIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT));

        parkedStreet = mStreetsSpinner.getSelectedItem().toString();
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class GetStreetValuesFromDatabase extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return valuesFromDatabase(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
//            mTestOutputTextView.setText(result);

            try {
                String str = "";
                ArrayList<String> spinnerArray = new ArrayList<>();

                JSONObject jsonTest = new JSONObject(result);
                JSONArray streets = jsonTest.getJSONArray("Streets");
                str = streets.getJSONObject(0).getString("Street");
                mTestOutputTextView.setText(str);

                for (int i = 0; i < jsonTest.getJSONArray("Streets").length(); i++) {
                    str = streets.getJSONObject(i).getString("Street");
                    spinnerArray.add(str);
                }

                ArrayAdapter<String> streetNamesArrayAdapter = new ArrayAdapter<String>(
                        ParkActivity.this,
                        android.R.layout.simple_spinner_item, spinnerArray);
                updateStreetsSpinner(streetNamesArrayAdapter);
            } catch (JSONException e) {
                // Oops
            }
            getZones();
        }
    }
    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class GetZoneValuesFromDatabase extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return valuesFromDatabase(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {
                String str = "";
                ArrayList<String> spinnerArray = new ArrayList<>();

                JSONObject jsonTest = new JSONObject(result);
                JSONArray zonesSignId = jsonTest.getJSONArray("DBValues");
                JSONArray zones = jsonTest.getJSONArray("AppValues");
                str = zones.getJSONObject(0).getString("AppFacing");
                mTestOutputTextView.setText(str);
                str = Integer.toString(jsonTest.getJSONArray("AppValues").length());
                mTestOutputTextView.setText(str);
                // Create new array that is 2 wide and the length of JSONArray
                // To be used for storing zone and corresponding sign ID
                zonesSignIdArray = new String[jsonTest.getJSONArray("AppValues").length()][2];

                for (int i = 0; i < jsonTest.getJSONArray("AppValues").length(); i++) {
                    str = zones.getJSONObject(i).getString("AppFacing");
                    spinnerArray.add(str);
                    zonesSignIdArray[i][0] = str;
                    zonesSignIdArray[i][1] = zonesSignId.getJSONObject(i).getString("DBFacing");
                }

                ArrayAdapter<String> zonesArrayAdapter = new ArrayAdapter<String>(
                        ParkActivity.this,
                        android.R.layout.simple_spinner_item, spinnerArray);
                updateZonesSpinner(zonesArrayAdapter);
            } catch (JSONException e) {
                // Oops
                mTestOutputTextView.setText("JSON error");
            }
            getNextParkingRestriction();
        }
    }
    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class GetMoveTimeFromDatabase extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return valuesFromDatabase(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            mNextParkingRestrictionTextView.setText(result);
            nextMoveTime = result;
        }
    }



    // Given a URL, establishes an HttpUrlConnection and retrieves
// the web page content as a InputStream, which it returns as
// a string.
    private String valuesFromDatabase(String myUrl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 500;

        try {
            URL url = new URL(myUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public class StreetSpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener, OnTouchListener {

        boolean userSelect = false;

        public boolean onTouch(View view, MotionEvent event) {
            userSelect = true;
            return false;
        }

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)

            if (userSelect){
                getZones();
                userSelect = false;
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    public class ZoneSpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener, OnTouchListener {

        boolean userSelect = false;

        public boolean onTouch(View view, MotionEvent event) {
            userSelect = true;
            return false;
        }
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            if (userSelect){
                getNextParkingRestriction();
                userSelect = false;
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}

