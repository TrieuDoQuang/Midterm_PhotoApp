package com.example.midterm_PhotoApp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.midterm_PhotoApp.Adapters.StaggerAdapter;
import com.example.midterm_PhotoApp.Models.DataClass;
import com.example.midterm_PhotoApp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StaggerFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<DataClass> dataList;
    StaggerAdapter adapter;

    private DatabaseReference databaseReference;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stagger,container,false);
        Log.d("STAGGER VIEW", "CONNECTING TO FIREBASE");
        databaseReference = FirebaseDatabase.getInstance().getReference("Images");
        Log.d("STAGGER VIEW", "CONNECTING TO FIREBASE");
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        dataList = new ArrayList<>();
        adapter = new StaggerAdapter(dataList, getContext());
        recyclerView.setAdapter(adapter);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dataList.clear();
                Log.d("STAGGER VIEW", "STAGGER VIEW IS FETCHING URLS");
                for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                    DataClass dataClass = dataSnapshot.getValue(DataClass.class);
                    dataList.add(dataClass);
                }
                Log.d("STAGGER VIEW", "STAGGER VIEW HAS FINISHED FETCHING URLS");
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return view;
    }
}