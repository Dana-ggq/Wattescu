package com.example.wattescu.adapters;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.wattescu.fragments.ActivitiesFragment;
import com.example.wattescu.fragments.NewsFragment;


public class TabViewHomeFragmentAdapter extends FragmentStateAdapter {

    //define variables that need to be trasfered between fragments and initialize in constructor

    public TabViewHomeFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        if(position == 0){
            //fragment news
            return  new NewsFragment();
        }
        //fragment activities
        return  new ActivitiesFragment();
    }

    @Override
    public int getItemCount() {
        //2 fragments
        return 2;
    }
}

