package com.example.wattescu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import android.os.Bundle;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;


import com.example.wattescu.fragments.FavoritesFragment;
import com.example.wattescu.fragments.GraphicsFragment;
import com.example.wattescu.fragments.HomeFragment;
import com.example.wattescu.fragments.ProfileFragment;
import com.example.wattescu.fragments.SearchFragment;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

    //views
    BottomNavigationView bottomNavigationView;
    FloatingActionButton fabSearch;
    BottomAppBar bottomAppBar;

    //fragments
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        enableTransitions();

        setContentView(R.layout.activity_main);

        initComponents();

        //setting start fragment
        setStartFragment(savedInstanceState);
    }


    private void setStartFragment(Bundle savedInstanceState) {
        if(savedInstanceState == null){
            currentFragment = new HomeFragment();
            openFragment();
            bottomNavigationView.setSelectedItemId(R.id.menu_home);
        }
    }

    private void openFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame_layout, currentFragment)
                .commit();
    }

    private void initComponents() {
        bottomNavigationView = findViewById(R.id.main_bottom_navigation_view);
        fabSearch = findViewById(R.id.main_fab_search);
        bottomAppBar = findViewById(R.id.main_bottom_app_bar);

        initEvents();
    }

    private void initEvents() {
        fabSearch.setOnClickListener(getFabSearchClickListener());
        bottomNavigationView.setOnNavigationItemSelectedListener(getNavItemSelectedListener());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener getNavItemSelectedListener() {
        return new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_home:
                        currentFragment = new HomeFragment();
                        break;
                    case R.id.menu_graphics:
                        currentFragment = new GraphicsFragment();
                        break;
                    case R.id.menu_saved:
                        currentFragment = new FavoritesFragment();
                        break;
                    case R.id.menu_profile:
                        currentFragment = new ProfileFragment();
                        break;
                }
                openFragment();
                return true;
            }
        };
    }

    private View.OnClickListener getFabSearchClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomNavigationView.getMenu().findItem(R.id.menu_invisible).setChecked(true);
                currentFragment = new SearchFragment();
                openFragment();
            }
        };
    }

    private void enableTransitions() {
        //enable transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        // set transitions
        Explode explode = new Explode();
        explode.setDuration(1000);
        explode.setInterpolator(new DecelerateInterpolator());
        getWindow().setExitTransition(explode);
        getWindow().setEnterTransition(explode);

    }

}