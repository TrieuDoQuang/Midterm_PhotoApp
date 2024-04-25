package com.example.midterm_PhotoApp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.widget.ImageView;

import com.example.midterm_PhotoApp.Adapters.TabAdapter;
import com.example.midterm_PhotoApp.Fragments.RecyclerFragment;
import com.example.midterm_PhotoApp.Models.DataClass;
import com.example.midterm_PhotoApp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //private ArrayList<DataClass> dataList;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    /*final private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Images");

    public ArrayList<DataClass> getDataList() {
        return dataList;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout=(TabLayout)findViewById(R.id.tabLayout);
        viewPager=(ViewPager2)findViewById(R.id.viewPager);

        tabLayout.addTab(tabLayout.newTab().setText("Recycler"));
        tabLayout.addTab(tabLayout.newTab().setText("Grid"));
        tabLayout.addTab(tabLayout.newTab().setText("Stagger"));
        tabLayout.addTab(tabLayout.newTab().setText("Upload Img"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final TabAdapter adapter = new TabAdapter(this);
        viewPager.setAdapter(adapter);



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Objects.requireNonNull(tabLayout.getTabAt(position)).select();
            }
        });


    }
//    private void loadImagesForTab(int position) {
//        TabAdapter adapter = (TabAdapter) viewPager.getAdapter();
//        if (adapter != null) {
//            // Assuming you have a method in your adapter to get the fragment at a specific position
//            Fragment fragment = adapter.createFragment(position);
//            if (fragment instanceof RecyclerFragment){
//                ImageView imageView = ((RecyclerFragment) fragment).onCreateView();
//
//            }
//        }
//    }
}