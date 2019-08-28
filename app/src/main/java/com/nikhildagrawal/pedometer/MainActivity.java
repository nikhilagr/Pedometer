package com.nikhildagrawal.pedometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.nikhildagrawal.pedometer.adapters.ActivityDetailAdapter;
import com.nikhildagrawal.pedometer.models.ActivityDetail;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;


    private RecyclerView recyclerView;
    private ToggleButton toggleButton;
    private LinearLayoutManager layoutManager;
    private List<ActivityDetail> currentList;
    private List<ActivityDetail> previousList;
    private ActivityDetailAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        currentList = new ArrayList<>();
        previousList = new ArrayList<>();
        recyclerView = findViewById(R.id.stepsRecyclerView);
        toggleButton = findViewById(R.id.reverseButton);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ActivityDetailAdapter(MainActivity.this, currentList);
        recyclerView.setAdapter(adapter);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    Collections.reverse(previousList);
                    adapter.notifyDataSetChanged();

                }else{
                    Collections.reverse(previousList);
                    adapter.notifyDataSetChanged();
                }
            }
        });



        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();


        /**
         * Check if user is signed in into account and has granted the permission.
         */
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {

            /**
             * Request permission.
             */
            GoogleSignIn.requestPermissions(this, GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);

        }else{

            /**
             * Access data if user has granted permission.
             */

                // Get steps for current day
                accessGoogleFitForToday();

                // get steps for last 13 days
                accessGoogleFit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {


                // Get steps for current day
                accessGoogleFitForToday();
                // get steps for last 13 days
                accessGoogleFit();
            }
        }
    }


    /**
     * This method counts number of steps for current day. we dont use bucketing here it means no
     * aggregated data. so the DataReadRequest data differs from that when we use aggregated data.
     */
    private void accessGoogleFitForToday(){


        Calendar calender = Calendar.getInstance();
        calender.setTime(new Date());

        // end time for todays step count will be current time.
        final long endTime = calender.getTimeInMillis();

        calender.set(Calendar.HOUR_OF_DAY, 0);
        calender.set(Calendar.MINUTE, 0);
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.MILLISECOND, 0);

        // we update start time to be current day's midnight time.
       final long startTime = calender.getTimeInMillis();


        /**
         * Create DataReadRequest object. We use read method on builder an not aggregate unlike in
         * other method.
         */
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {

                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {

                        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");

                        DataSet dataSet = dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);

                                    int totalsteps = 0;
                                    List<DataPoint> dataPoints = dataSet.getDataPoints();

                                    for(DataPoint dataPoint: dataPoints){
                                        int steps = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                                        totalsteps += steps;

                                    }

                        String calcuDate = dateFormat.format(endTime) + " " ;
                        ActivityDetail stepDetails = new ActivityDetail(totalsteps,calcuDate);
                        currentList.add(stepDetails);
                        adapter.notifyDataSetChanged();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {

                    }
                });
    }


    /**
     * Get step counts for last past 13 days. Used bucketing technique here each bucket is of
     * one day (24 hour). Midnight to midnight.
     */

    private void accessGoogleFit(){


        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long endTime = calendar.getTime().getTime();

        calendar.add(Calendar.DAY_OF_YEAR,-13);

        long startTime = calendar.getTime().getTime();



        /**
         * Create DataReadRequest object. Used aggregated data via bucketing technique.
         */
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS).bucketByTime(1, TimeUnit.DAYS)
                .build();



        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {

                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {



                        DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
                        if (dataReadResponse.getBuckets().size() > 0) {

                            for (Bucket bucket : dataReadResponse.getBuckets()) {

                                List<DataSet> dataSets = bucket.getDataSets();

                                for (DataSet dataSet : dataSets) {


                                    if(dataSet == null || dataSet.isEmpty()){
                                        continue;
                                    }

                                    List<DataPoint> dataPoints = dataSet.getDataPoints();
                                    for(DataPoint dataPoint: dataPoints){


                                        int steps = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                                        String  calcuDate = dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS) ) + " ";
                                        ActivityDetail stepDetails = new ActivityDetail(steps,calcuDate);
                                        previousList.add(stepDetails);

                                    }
                                }
                            }


                            previousList.addAll(currentList);
                            Collections.reverse(previousList);
                            adapter = new ActivityDetailAdapter(MainActivity.this,previousList);
                            recyclerView.setAdapter(adapter);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure()", e);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<DataReadResponse> task) {

                    }
                });
    }
}
