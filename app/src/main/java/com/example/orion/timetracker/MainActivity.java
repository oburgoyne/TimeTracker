package com.example.orion.timetracker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    public static final String MY_PREFS_NAME = "myPrefsFle";
//this is so Russ can get my file
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText empName = (EditText) findViewById(R.id.employeeName);
        empName.requestFocus();

        Button employIn = (Button) findViewById(R.id.employIn);
        employIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                clockIn();
            }
        });

        Button employOut = (Button) findViewById(R.id.employOut);
        employOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clockOut();
            }
        });

        Button jobIn = (Button) findViewById(R.id.jobIn);
        jobIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jobIn();
            }
        });

        Button jobOut = (Button) findViewById(R.id.jobOut);
        jobOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jobOut();
            }
        });

        SharedPreferences sp = getSharedPreferences(MY_PREFS_NAME, Activity.MODE_PRIVATE);
        boolean rememberMe = sp.getBoolean("rememberMe", true);
        String rememberedName = sp.getString("EmployeeInName", "DidNotWork");

        if(rememberMe == true){
            ((EditText)findViewById(R.id.employeeName)).setText(rememberedName);
        }else{
            ((EditText)findViewById(R.id.employeeName)).setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //CLOCK IN METHOD
    public void clockIn(){
        //This changes the text in timeConfirm field when clock in button is clicked
        Editable empName = ((EditText)findViewById(R.id.employeeName)).getText();
        String name = empName.toString();

        //format Date
        String formattedDate = formatDate();

        //get clock in time in Milliseconds for storage
        long timeInMill = System.currentTimeMillis();

        //store into XML file
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("EmployeeInName", name);
        editor.putLong("timeIn", timeInMill);
        editor.putString("clockedInDate", formattedDate);
        boolean remembered = ((CheckBox)findViewById(R.id.rememberMe)).isChecked();
        editor.putBoolean("rememberMe", remembered);
        editor.commit();

        //change text on confirmation
        ((TextView)findViewById(R.id.timeConfirm)).setText(empName + " clocked in at: \r\n" + formattedDate);

    }

    //CLOCK OUT METHOD
    public void clockOut(){
        //Remove text in confirmation field
        ((TextView)findViewById(R.id.timeConfirm)).setText("");

        //This changes the text in timeConfirm field when clock in button is clicked
        Editable empName = ((EditText)findViewById(R.id.employeeName)).getText();
        String name = empName.toString();

        //format Date
        String formattedDate = formatDate();

        //get clock Out time in milliseconds for storage
        long timeOutMill = System.currentTimeMillis();

        //store into XML file
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("EmployeeOutName", name);
        editor.putLong("timeOut", timeOutMill);
        boolean remembered = ((CheckBox)findViewById(R.id.rememberMe)).isChecked();
        editor.putBoolean("rememberMe", remembered);
        editor.commit();

        //pull from XML file
        SharedPreferences sp = getSharedPreferences(MY_PREFS_NAME, Activity.MODE_PRIVATE);
        long myTimeMill = sp.getLong("timeIn", -1);
        String clockedInDate = sp.getString("clockedInDate", "DateDidNotWork");

        //Calculate Time Worked
        String timeWorked = calcTime(myTimeMill, timeOutMill);

        //Send Email
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"goose145@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, empName + " Hours Worked");
        i.putExtra(Intent.EXTRA_TEXT, "Employee: " + empName + "\r\nClock in: " +  "  " + clockedInDate
            + "\r\nClock out: " +  "  " + formattedDate + "\r\nTotal Time: " + timeWorked);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            System.out.println("This didn't work.");
        }

        //change text on confirmation
        ((TextView)findViewById(R.id.timeConfirm)).setText(empName + " clocked out at: \r\n" + formattedDate + "\r\n" + timeWorked);

        //clear XML data
        if(remembered == false){
            editor.putString("EmployeeInName", " ");
        }
        //editor.putString("EmployeeInName", "");
        editor.putLong("timeIn", 0);
        editor.putString("clockedInDate", "");
        editor.putString("EmployeeOutName", "");
        editor.putLong("timeOut", 0);
        editor.commit();
    }

    //JOB IN METHOD
    public void jobIn(){
        //This changes the text in timeConfirm field when clock in button is clicked
        Editable jobName = ((EditText)findViewById(R.id.jobName)).getText();
        String name = jobName.toString();

        //format Date
        String formattedDate = formatDate();

        //get clock in time in Milliseconds for storage
        long timeInMill = System.currentTimeMillis();

        //store into XML file
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("jobInName", name);
        editor.putLong("jobInTime", timeInMill);
        editor.putString("jobInDate", formattedDate);
        editor.commit();

        //change text on confirmation
        ((TextView)findViewById(R.id.jobConfirm)).setText("Job: " + jobName + "\r\nStarted: " + formattedDate);

    }

    //JOB OUT METHOD
    public void jobOut(){
        //Remove text in confirmation field
        ((TextView)findViewById(R.id.jobConfirm)).setText("");

        //This changes the text in timeConfirm field when clock in button is clicked
        Editable jobName = ((EditText)findViewById(R.id.jobName)).getText();
        String name = jobName.toString();

        //format Date
        String formattedDate = formatDate();

        //get clock Out time in milliseconds for storage
        long jobOutMill = System.currentTimeMillis();

        //store into XML file
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("jobOutName", name);
        editor.putLong("jobOutTime", jobOutMill);
        editor.commit();

        //pull from XML file
        SharedPreferences sp = getSharedPreferences(MY_PREFS_NAME, Activity.MODE_PRIVATE);
        long myTimeMill = sp.getLong("jobInTime", -1);
        String empName = sp.getString("EmployeeInName", "NameDitNotWork");
        String jobInDate = sp.getString("jobInDate", "DateDidNotWork");

        //Calculate Time Worked
        String timeWorked = calcTime(myTimeMill, jobOutMill);

        //Send Email
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"goose145@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Job: " + jobName + " Hours Worked");
        i.putExtra(Intent.EXTRA_TEXT, "Employee: " + empName + "\r\nJob: " + jobName + "\r\nStart Time: " + jobInDate
                + "\r\nEnd Time: " +  "  " + formattedDate + "\r\nTotal Time: " + timeWorked);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            System.out.println("This didn't work.");
        }

        //change text on confirmation
        String outText = "Job: " + jobName + "\r\nStart Time: " + jobInDate + "\r\nEnd Time: " +  "  " + formattedDate + "\r\nTotal Time: " + timeWorked;
        ((TextView)findViewById(R.id.jobConfirm)).setText(outText);

        //clear XML file
        editor.putString("jobInName", "");
        editor.putLong("jobInTime", 0);
        editor.putString("jobInDate", "");
        editor.putString("jobOutName", "");
        editor.putLong("jobOutTime", 0);
        editor.commit();
    }


    public String formatDate(){
        //format and calculate date/time
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("hh:mm a MMM dd, yyyy");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public String calcTime(long mill1, long mill2){
        String timeWorked = "time";
        double timeMill;
        if(mill2 <= mill1){
            timeMill = (mill2 + 86400000) - mill2;
        }else{
            timeMill = mill2 - mill1;
        }
        double timeHrs = timeMill / (1000 * 60 * 60);
        String totalTime = String.format("%.2f", timeHrs);
        //timeMill = TimeUnit.MILLISECONDS.toHours(timeMill);
        timeWorked = totalTime + " Hours";
        return timeWorked;
    }


}
