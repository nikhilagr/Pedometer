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
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;


    private RecyclerView recyclerView;
    private ToggleButton toggleButton;
    private LinearLayoutManager layoutManager;
    private List<ActivityDetail> activityDetailList;
    private ActivityDetailAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        activityDetailList = new ArrayList<>();
        recyclerView = findViewById(R.id.stepsRecyclerView);
        toggleButton = findViewById(R.id.reverseButton);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ActivityDetailAdapter(this,activityDetailList);
        recyclerView.setAdapter(adapter);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){
                    Collections.reverse(activityDetailList);
                    recyclerView.setAdapter(adapter);

                }else{
                    Collections.reverse(activityDetailList);
                    recyclerView.setAdapter(adapter);
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
            accessGoogleFit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessGoogleFit();
            }
        }
    }



    private void accessGoogleFit(){


        Calendar calender = Calendar.getInstance();
        calender.setTime(new Date());

        long endTime = calender.getTimeInMillis();

        /**
         * Last 14 days.To get last 2 weeks data.
         */
        calender.add(Calendar.DAY_OF_YEAR, -14);

        long startTime = calender.getTimeInMillis();


        /**
         * Create DataReadRequest object.
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
                                        activityDetailList.add(new ActivityDetail(0,"",""));
                                    }



                                    List<DataPoint> dataPoints = dataSet.getDataPoints();
                                    for(DataPoint dataPoint: dataPoints){

                                        Log.d("datapoint  is: ",dataPoint.getValue(Field.FIELD_STEPS).toString());

                                        int steps = dataPoint.getValue(Field.FIELD_STEPS).asInt();
                                        String from = dateFormat.format(bucket.getStartTime(TimeUnit.MILLISECONDS) ) + " ";
                                        String to = dateFormat.format(bucket.getEndTime(TimeUnit.MILLISECONDS) ) + " " ;

                                        ActivityDetail stepDetails = new ActivityDetail(steps,from,to);
                                        activityDetailList.add(stepDetails);

                                    }

                                    recyclerView.setAdapter(adapter);
                                }
                            }




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
