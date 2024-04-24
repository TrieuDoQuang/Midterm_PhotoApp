package com.example.midterm_PhotoApp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.midterm_PhotoApp.Models.DataClass;
import com.example.midterm_PhotoApp.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class StaggerAdapter extends RecyclerView.Adapter<StaggerAdapter.MyViewHolder> {
    ArrayList<DataClass> dataList;
    Context context;
    public StaggerAdapter(ArrayList<DataClass> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stagger_item, parent, false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(dataList.get(position).getImageURL()).into(holder.staggeredImages);
    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder{
        RoundedImageView staggeredImages;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            staggeredImages = itemView.findViewById(R.id.staggeredImages);
        }
    }
}
