package com.nikhildagrawal.pedometer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nikhildagrawal.pedometer.R;
import com.nikhildagrawal.pedometer.models.ActivityDetail;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ActivityDetailAdapter extends RecyclerView.Adapter<ActivityDetailAdapter.StepsViewHolder> {

    Context mContext;
    List<ActivityDetail> activityDetailList;

    public ActivityDetailAdapter(Context context,List<ActivityDetail> activityDetailList){
        mContext = context;
        this.activityDetailList = activityDetailList;
    }

    @NonNull
    @Override
    public StepsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_steps_rcv,parent,false);
        StepsViewHolder holder = new StepsViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull StepsViewHolder holder, int position) {

        if(holder!=null){

            holder.tvSteps.setText(activityDetailList.get(position).getNoOfSteps()+ " ");
            holder.tvTo.setText(activityDetailList.get(position).getDate());
            holder.iconImage.setImageResource(R.drawable.walk);
        }

    }

    @Override
    public int getItemCount() {

        if(activityDetailList !=null || activityDetailList.size()!= 0){
            return activityDetailList.size();
        }
        return 0;
    }

    public class StepsViewHolder extends RecyclerView.ViewHolder{

        TextView tvSteps;
        ImageView iconImage;
        TextView tvTo;
        public StepsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSteps = itemView.findViewById(R.id.steps);
            iconImage = itemView.findViewById(R.id.icon);
            tvTo = itemView.findViewById(R.id.to);
        }
    }

}
