package com.example.midterm_PhotoApp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.midterm_PhotoApp.Fragments.RecyclerFragment;
import com.example.midterm_PhotoApp.Fragments.UploadFragment;

public class TabAdapter extends FragmentStateAdapter {


    public TabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0: return new RecyclerFragment();
            case 1: return new RecyclerFragment();
            case 2: return new RecyclerFragment();
            case 3: return new UploadFragment();
            default: return new RecyclerFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
