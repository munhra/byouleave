package com.example.vntraal.byouleave;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.util.Calendar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by vntants on 8/15/17.
 */

public class CalendarManager extends Activity implements EasyPermissions.PermissionCallbacks {

    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    private Button mCallApiButton;
    private Handler mHandler;
    ProgressDialog mProgress;
    private List<String> mCalendarResults = new ArrayList<>();

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    private static final CalendarManager ourInstance = new CalendarManager();
    private String dayInSeek;
    private int daysToJump = 7;


    public static CalendarManager getInstance() {
        return ourInstance;
    }

    private CalendarManager() {
        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                MainActivity.getContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        mHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    public String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11 ) {
            month = months[num];
        }
        return month;
    }

    public ArrayMap<String, String> getWeekDays(){
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dayFormat = new SimpleDateFormat("E", Locale.US);
        String weekDay = dayFormat.format(calendar.getTime());

        String today = "" + (calendar.get(Calendar.DATE));

        if(today.length() == 1){
            today = "0" + today;
        }

        Log.e("KIDDO", calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));

        String actualmonth = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);


        Log.e("Testing WeekDay",weekDay);

        ArrayMap<String,String> dias = new ArrayMap<>();
        dias.add("today",today);
        dias.add("month",actualmonth);
        dias.add("weekday",weekDay);

        switch(weekDay){
            case "Mon":
                dias.add("Mon",today);
                dias.add("Tue",((calendar.get(Calendar.DATE) + 1) + ""));
                dias.add("Wed",((calendar.get(Calendar.DATE) + 2) + ""));
                dias.add("Thu",((calendar.get(Calendar.DATE) + 3) + ""));
                dias.add("Fri",((calendar.get(Calendar.DATE) + 4) + ""));
                dias.add("Sat",((calendar.get(Calendar.DATE) + 5) + ""));
                dias.add("Sun",((calendar.get(Calendar.DATE) + 6) + ""));
                break;

            case "Tue":
                dias.add("Mon",((calendar.get(Calendar.DATE) -1) + ""));
                dias.add("Tue",today);
                dias.add("Wed",((calendar.get(Calendar.DATE) + 1) + ""));
                dias.add("Thu",((calendar.get(Calendar.DATE) + 2) + ""));
                dias.add("Fri",((calendar.get(Calendar.DATE) + 3) + ""));
                dias.add("Sat",((calendar.get(Calendar.DATE) + 4) + ""));
                dias.add("Sun",((calendar.get(Calendar.DATE) + 5) + ""));
                break;

            case "Wed":
                dias.add("Mon",((calendar.get(Calendar.DATE) -2) + ""));
                dias.add("Tue",((calendar.get(Calendar.DATE) -1) + ""));
                dias.add("Wed",today);
                dias.add("Thu",((calendar.get(Calendar.DATE) + 1) + ""));
                dias.add("Fri",((calendar.get(Calendar.DATE) + 2) + ""));
                dias.add("Sat",((calendar.get(Calendar.DATE) + 3) + ""));
                dias.add("Sun",((calendar.get(Calendar.DATE) + 4) + ""));
                break;

            case "Thu":
                dias.add("Mon",((calendar.get(Calendar.DATE) -3) + ""));
                dias.add("Tue",((calendar.get(Calendar.DATE) -2) + ""));
                dias.add("Wed",((calendar.get(Calendar.DATE) -1) + ""));
                dias.add("Thu",today);
                dias.add("Fri",((calendar.get(Calendar.DATE) + 1) + ""));
                dias.add("Sat",((calendar.get(Calendar.DATE) + 2) + ""));
                dias.add("Sun",((calendar.get(Calendar.DATE) + 3) + ""));
                break;

            case "Fri":
                dias.add("Mon",((calendar.get(Calendar.DATE) -4) + ""));
                dias.add("Tue",((calendar.get(Calendar.DATE) -3) + ""));
                dias.add("Wed",((calendar.get(Calendar.DATE) -2) + ""));
                dias.add("Thu",((calendar.get(Calendar.DATE) -1) + ""));
                dias.add("Fri",today);
                dias.add("Sat",((calendar.get(Calendar.DATE) + 1) + ""));
                dias.add("Sun",((calendar.get(Calendar.DATE) + 2) + ""));
                break;

            case "Sat":
                dias.add("Mon",((calendar.get(Calendar.DATE) -5) + ""));
                dias.add("Tue",((calendar.get(Calendar.DATE) -4) + ""));
                dias.add("Wed",((calendar.get(Calendar.DATE) -3) + ""));
                dias.add("Thu",((calendar.get(Calendar.DATE) -2) + ""));
                dias.add("Fri",((calendar.get(Calendar.DATE) -1) + ""));
                dias.add("Sat",today);
                dias.add("Sun",((calendar.get(Calendar.DATE) + 1) + ""));
                break;

            case "Sun":
                dias.add("Mon",((calendar.get(Calendar.DATE) -6) + ""));
                dias.add("Tue",((calendar.get(Calendar.DATE) -5) + ""));
                dias.add("Wed",((calendar.get(Calendar.DATE) -4) + ""));
                dias.add("Thu",((calendar.get(Calendar.DATE) -3) + ""));
                dias.add("Fri",((calendar.get(Calendar.DATE) -2) + ""));
                dias.add("Sat",((calendar.get(Calendar.DATE) -1) + ""));
                dias.add("Sun",today);
                break;

            default:
                Log.e("CALENDAR", "CALENDAR HAS FAILED");
                break;
        }

        return dias;


    }

    public List<String> getCalendarRetults() {

        return mCalendarResults;
    }

    public String getCalculatedDate(String dateFormat, int days) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat s = new SimpleDateFormat(dateFormat);
        cal.add(Calendar.DAY_OF_YEAR, days);
        try {
            return s.format(cal.getTime());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("TAG", "Error in Parsing Date : " + e.getMessage());
        }
        return null;
    }

    public String getNextWeekEvents() throws IOException, ParseException{
        //Log.e("Time", "Seeking for events in the next week");
        daysToJump+=7;
        //Log.e("Checking",dayInSeek);
        String nextWeek = getCalculatedDate("yyyy-MM-dd", daysToJump);
        dayInSeek = nextWeek;
        //Log.e("Next Week", nextWeek);
        return nextWeek;
    }

    public String getPastWeekEvents() throws IOException, ParseException{
        //Log.e("Time", "Seeking for events in the next week");
        daysToJump-=7;
        //Log.e("Checking",dayInSeek);
        String pastweek = getCalculatedDate("yyyy-MM-dd", daysToJump);
        dayInSeek = pastweek;
        //Log.e("Past Week", pastweek);

        return pastweek;
    }

    Runnable mStatusChecker = new Runnable() {

        @Override

        public void run() {

            try {

                new MakeRequestTask(mCredential).execute();

//                getResultsFromApi();

                //updateStatus(); //this function can change value of mInterval.

            } finally {

                // 100% guarantee that this always happens, even if

                // your update method throws an exception

                mHandler.postDelayed(mStatusChecker, 5000);

            }

        }

    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {

        if (!isGooglePlayServicesAvailable()) {

            acquireGooglePlayServices();

        }else if (!EasyPermissions.hasPermissions(

                MainActivity.getContext(), Manifest.permission.GET_ACCOUNTS)) {

            EasyPermissions.requestPermissions(

                    MainActivity.getActivity(),

                    "This app needs to access your Google account (via Contacts).",

                    REQUEST_PERMISSION_GET_ACCOUNTS,

                    Manifest.permission.GET_ACCOUNTS);

        } else if (mCredential.getSelectedAccountName() == null) {

            chooseAccount();

        } else if (!isDeviceOnline()) {

//            mOutputText.setText("No network connection available.");

        } else {

//            new MakeRequestTask(mCredential).execute();

            startRepeatingTask();

        }

    }

    void startTask() {

        getResultsFromApi();

    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                MainActivity.getContext(), Manifest.permission.GET_ACCOUNTS)) {
            String accountName = MainActivity.getSettings().getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                MainActivity.getActivity().startActivityForResult(mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    MainActivity.getActivity(),
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
//                    mOutputText.setText(
//                            "This app requires Google Play Services. Please install " +
//                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = MainActivity.getSettings();
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr = MainActivity.getConnMgr();
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(MainActivity.getContext());
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.getActivity(),
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         *
         * @return List of Strings describing returned events.
         * @throws IOException
         */




        private List<String> getDataFromApi() throws IOException, ParseException {
            // List the next 10 events from the primary calendar.
            Log.e("Time", "Getting the time");
            DateTime now = new DateTime(System.currentTimeMillis());

            Calendar calendar = Calendar.getInstance();

            if(dayInSeek == null || dayInSeek == ""){
                Log.e("Defining","Setting Day in Week");
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
                String formattedDate = df.format(calendar.getTime());
                dayInSeek = formattedDate;
            }

            String thisYear = "" + calendar.get(Calendar.YEAR);
            Log.e("Year", "# thisYear : " + thisYear);

            String thisMonth = "" + (calendar.get(Calendar.MONTH) + 1);
            Log.e("Month", "@ thisMonth : " + thisMonth);

            String today = "" + (calendar.get(Calendar.DATE));
            if(today.length() == 1){
                today = "0" + today;
            }
            Log.e("Day", "$ today : " + today);

            String timeNow = "" + (calendar.get(Calendar.HOUR_OF_DAY));
            if(timeNow.length() == 1){
                timeNow = "0" + timeNow;
            }
            Log.e("Day", "$ time now : " + timeNow);

            String nextDay = "" + (calendar.get(Calendar.DATE) + 1);
            if(nextDay.length() == 1){
                nextDay = "0" + nextDay;
            }
            Log.e("Day", "$ nextDay : " + nextDay);

            Log.e("Sps",thisYear + "-" + thisMonth + "-" + today + "T" + timeNow + ":00:00Z");
            Log.e("Sps",thisYear + "-" + thisMonth + "-" + nextDay + "T03:00:00Z");
            //Log.e("Sps","2017-10-07T03:00:00Z");

            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setTimeMin(new DateTime(thisYear + "-" + thisMonth + "-" + today + "T" + timeNow + ":00:00Z"))
                    .setTimeMax(new DateTime(thisYear + "-" + thisMonth + "-" + nextDay + "T03:00:00Z")) //GMT Brasília -3 ex:"2017-10-07T03:00:00Z"
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();


            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }

                String unformattedTime = String.format("%s", event.getStart());
                eventStrings.add(
                        "{" +
                                "\"" + "time" + "\"" + ":" + "\"" + unformattedTime.substring(24,29) + "\"" + "," +
                                "\"" + "eventName" + "\"" + ":" + "\"" + String.format("%s", event.getSummary()) + "\"" + "," +
                                "\"" + "description" + "\"" + ":" + "\"" + String.format("%s", event.getDescription()) + "\"" + "}");

            }



            return eventStrings;
        }

        private List<String> getEventsOnClick(String thisWeek, String nextWeek, String time) throws IOException, ParseException {

            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setTimeMin(new DateTime(thisWeek + "T" + time + ":00:00Z"))
                    .setTimeMax(new DateTime(nextWeek + "T03:00:00Z")) //GMT Brasília -3 ex:"2017-10-07T03:00:00Z"
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
//            mOutputText.setText("");
//            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
//            mProgress.hide();
            if (output == null || output.size() == 0) {
                mCalendarResults.clear();
                mCalendarResults.add(0, "No results returned.");
//                mOutputText.setText("No results returned.");
            } else {
                mCalendarResults = output;
//                output.add(0, "Data retrieved using the Google Calendar API:");
//                mOutputText.setText(TextUtils.join("\n", output));
            }
        }

        @Override
        protected void onCancelled() {
//            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    MainActivity.getActivity().startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            CalendarManager.REQUEST_AUTHORIZATION);
                } else {
//                    mOutputText.setText("The following error occurred:\n"
//                            + mLastError.getMessage());
                }
            } else {
//                mOutputText.setText("Request cancelled.");
            }
        }
    }
}
