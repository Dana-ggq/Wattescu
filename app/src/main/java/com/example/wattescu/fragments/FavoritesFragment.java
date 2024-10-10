package com.example.wattescu.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wattescu.R;
import com.example.wattescu.adapters.TabViewFavoritesFragmentAdapter;
import com.google.android.material.tabs.TabLayout;


public class FavoritesFragment extends Fragment {

    //TabLayout
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabViewFavoritesFragmentAdapter fragmentAdapter;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_favorites, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        tabLayout = view.findViewById(R.id.favorites_tabLayout);
        viewPager = view.findViewById(R.id.favorites_viewPager);
        if(getContext()!=null){
            //create fragment manager for view pager
            createFragmentManager();
            initEvents();
        }
    }

    private void createFragmentManager() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentAdapter = new TabViewFavoritesFragmentAdapter(fragmentManager, getLifecycle());
        viewPager.setAdapter(fragmentAdapter);
    }

    private void initEvents() {
        tabLayout.addOnTabSelectedListener(getTabSelectedListner());
        viewPager.registerOnPageChangeCallback(getPageChangeCallback());
    }

    //connect tabLayout and viewPager
    private ViewPager2.OnPageChangeCallback getPageChangeCallback() {
        return new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        };
    }
    private TabLayout.OnTabSelectedListener getTabSelectedListner() {
        return new TabLayout.OnTabSelectedListener() {
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
        };
    }
}