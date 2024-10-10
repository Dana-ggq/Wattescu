package com.example.wattescu.adapters;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.wattescu.fragments.FavoriteAppliancesFragment;
import com.example.wattescu.fragments.FavoriteConfigurationsFragment;


public class TabViewFavoritesFragmentAdapter extends FragmentStateAdapter {

    //define variables that need to be trasfered between fragments and initialize in constructor

    public TabViewFavoritesFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        if(position == 0){
            //fragment news
            return  new FavoriteConfigurationsFragment();
        }
        //fragment activities
        return  new FavoriteAppliancesFragment();
    }

    @Override
    public int getItemCount() {
        //2 fragments
        return 2;
    }
}

