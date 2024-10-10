package com.example.wattescu;

import static com.example.wattescu.R.drawable.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseSavedShopApplianceService;
import com.example.wattescu.firebase.FirebaseShopAppliancesService;
import com.example.wattescu.fragments.SearchFragment;
import com.example.wattescu.adapters.StaggeredRecycleViewApplianceAdapter;
import com.example.wattescu.entities.SavedShopAppliance;
import com.example.wattescu.entities.ShopAppliance;
import com.example.wattescu.enums.CategoryFilterType;
import com.example.wattescu.enums.RoomType;
import com.example.wattescu.enums.SortType;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SearchAppliancesActivity extends AppCompatActivity {

    private static final String TAG = "SearchAppliancesActivity";
    private static final int NUM_COLUMNS_RV = 2;

    private RoomType currentRoom = RoomType.ALL;
    private SortType sortType = SortType.NONE;
    private CategoryFilterType filterField = CategoryFilterType.NONE;

    //views
    private RadioGroup rgRoom;
    private ImageView ivSort;
    private ImageView ivFilter;
    private TextInputEditText tietSearch;
    private TextInputLayout tilSearch;
    private LinearLayout llNotFound;
    private TextView tvNoAppliancesFound;
    private RecyclerView rvAppliances;
    private CircularDotsLoader circularDotsLoader;

    //filter
    int filterId =0;
    //sort
    int sortId=0;

    //firebase
    private ArrayList<ShopAppliance> appliances = new ArrayList<>();
    private FirebaseShopAppliancesService firebaseService;

    private ArrayList<SavedShopAppliance> savedAppliances = new ArrayList<>();
    private FirebaseSavedShopApplianceService firebaseSavedShopApplianceService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableTransitions();
        setContentView(R.layout.activity_search_appliances);

        setRoom();
        initComponents();
        initFirebase();
    }

    private void initFirebase() {
        try {
            firebaseSavedShopApplianceService = FirebaseSavedShopApplianceService.getInstance(FirebaseAuth.getInstance().getCurrentUser().getUid());
            StaggeredRecycleViewApplianceAdapter adapter = (StaggeredRecycleViewApplianceAdapter) rvAppliances.getAdapter();
            adapter.setFirebaseSavedShopApplianceService(firebaseSavedShopApplianceService);
            firebaseSavedShopApplianceService.getSavedShopAppliancesListener(getSavedAppliancesCallback());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        firebaseService = FirebaseShopAppliancesService.getInstance();
        getAppliancesFromDatabase();
    }

    private Callback<List<SavedShopAppliance>> getSavedAppliancesCallback() {
        return new Callback<List<SavedShopAppliance>>() {
            @Override
            public void runResultOnUiThread(List<SavedShopAppliance> result) {
                if(result!=null){
                    savedAppliances.clear();
                    savedAppliances.addAll(result);
                    StaggeredRecycleViewApplianceAdapter adapter = (StaggeredRecycleViewApplianceAdapter) rvAppliances.getAdapter();
                    adapter.setSavedShopAppliances(savedAppliances);
                    if(appliances!=null && !appliances.isEmpty() && appliances.size() > 0){
                        notifyRvAdapter();
                    }
                }
            }
        };
    }

    private void getAppliancesFromDatabase() {
        circularDotsLoader.setVisibility(View.VISIBLE);
        firebaseService.attachDataChangedListener(getDataChangedListenerCallback(), currentRoom, filterField);
    }

    private Callback<List<ShopAppliance>> getDataChangedListenerCallback() {
        return new Callback<List<ShopAppliance>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void runResultOnUiThread(List<ShopAppliance> result) {
                clearProgressBar();
                if(result!=null){
                    itemsfound();
                    appliances.clear();
                    appliances.addAll(result);
                    notifyRvAdapter();
                    sortAppliances();

                    scrollRvToTop();

                }else{
                    noItemFound();
                }
            }

        };
    }

    private void clearProgressBar() {
        circularDotsLoader.clearAnimation();
        circularDotsLoader.animate()
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        circularDotsLoader.setVisibility(View.GONE);
                    }
                }).start();
        circularDotsLoader.setVisibility(View.GONE);
    }

    private void itemsfound() {
        tvNoAppliancesFound.setVisibility(View.VISIBLE);
        rvAppliances.setVisibility(View.VISIBLE);
        llNotFound.setVisibility(View.GONE);
    }

    private void noItemFound() {
        tvNoAppliancesFound.setVisibility(View.GONE);
        rvAppliances.setVisibility(View.GONE);
        llNotFound.setVisibility(View.VISIBLE);
    }

    private void notifyRvAdapter() {
        if(appliances != null) {
            itemsfound();
            StaggeredRecycleViewApplianceAdapter adapter = (StaggeredRecycleViewApplianceAdapter) rvAppliances.getAdapter();
            adapter.notifyDataSetChanged();
            initNoApplianceFound();
            if(appliances.isEmpty() || appliances.size()==0){
                noItemFound();
            }
        } else{
            noItemFound();
        }
    }

    private void scrollRvToTop() {
        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) rvAppliances.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(0, 0);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sortAppliances() {
        if(appliances == null){
            return;
        }
        List<ShopAppliance> sortedAppliances= new ArrayList<>();
        switch (sortType){
            case PRICE_ASCENDING:
                sortedAppliances= appliances
                        .parallelStream()
                        .sorted(Comparator.comparingDouble(ShopAppliance::getPrice))
                        .collect(Collectors.toList());
                appliances.clear();
                appliances.addAll(sortedAppliances);
                notifyRvAdapter();
                break;
            case LATEST_ADDED:
                sortedAppliances= appliances
                        .parallelStream()
                        .sorted(Comparator.comparingLong(ShopAppliance::getId).reversed())
                        .collect(Collectors.toList());
                appliances.clear();
                appliances.addAll(sortedAppliances);
                notifyRvAdapter();
                break;
            case PRICE_DESCENDING:
                sortedAppliances= appliances
                        .parallelStream()
                        .sorted(Comparator.comparingDouble(ShopAppliance::getPrice).reversed())
                        .collect(Collectors.toList());
                appliances.clear();
                appliances.addAll(sortedAppliances);
                notifyRvAdapter();
                break;
            default:
                break;
        }
    }

    private void initRecycleView(){
        rvAppliances = findViewById(R.id.search_appliance_rv_appliances);
        StaggeredRecycleViewApplianceAdapter adapter = new StaggeredRecycleViewApplianceAdapter(appliances, SearchAppliancesActivity.this);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS_RV, LinearLayoutManager.VERTICAL);
        rvAppliances.setLayoutManager(layoutManager);
        rvAppliances.setAdapter(adapter);
    }

    private  void initNoApplianceFound(){
        tvNoAppliancesFound.setText(getString(R.string.format_no_appliances_found, appliances.size()));
    }

    private void setRoom() {
        Intent intent = getIntent();
        if(intent.hasExtra(SearchFragment.ROOM)){
            currentRoom = RoomType.valueOf(intent.getStringExtra(SearchFragment.ROOM));
        }
    }

    private void initComponents() {
        rgRoom = findViewById(R.id.search_appliance_rg_room);
        ivSort = findViewById(R.id.search_appliance_iv_sort);
        ivFilter = findViewById(R.id.search_appliance_iv_filter);
        tietSearch = findViewById(R.id.search_appliance_tiet_search);
        tilSearch = findViewById(R.id.search_appliance_til_search);
        llNotFound = findViewById(R.id.search_appliance_ll_not_found);
        tvNoAppliancesFound = findViewById(R.id.search_appliance_tv_no_results);
        circularDotsLoader = findViewById(R.id.search_appliance_progress_bar);
        circularDotsLoader.clearAnimation();
        initRecycleView();
        setCurrentRoomRadioButton();
        initEvents();
    }

    private void setCurrentRoomRadioButton() {
        if(currentRoom == RoomType.ALL){
            return;
        }
        switch (currentRoom){
            case bathroom:
                rgRoom.check(R.id.search_appliance_rb_bathroom);
                break;
            case office:
                rgRoom.check(R.id.search_appliance_rb_office);
                break;
            case kitchen:
                rgRoom.check(R.id.search_appliance_rb_kitchen);
                break;
            case living_room:
                rgRoom.check(R.id.search_appliance_rb_living_room);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + currentRoom);
        }
    }

    private void initEvents() {
        rgRoom.setOnCheckedChangeListener(getRbRoomCheckChangedListener());
        ivFilter.setOnClickListener(gerIvFilterCLickedListener());
        ivSort.setOnClickListener(getIvSortClickedListener());
        tietSearch.addTextChangedListener(getTietSearchTextChangedListener());
    }

    private TextWatcher getTietSearchTextChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void afterTextChanged(Editable s) {
                if(tietSearch.getText()!=null && tietSearch.getText().toString().trim().length() >=3 ){
                    //search
                    rgRoom.setVisibility(View.GONE);
                    tilSearch.setElevation(10f);
                    ivFilter.setVisibility(View.GONE);
                    ivSort.setVisibility(View.GONE);
                    rgRoom.setClickable(false);

                    firebaseService.attachDataChangedListener(getDataChangedFilterListenerCallback(), RoomType.ALL, CategoryFilterType.NONE);

                }else if(tietSearch.getText()!=null && tietSearch.getText().toString().trim().length()==0){
                    //clear search
                    tilSearch.setElevation(0f);
                    rgRoom.setVisibility(View.VISIBLE);
                    ivFilter.setVisibility(View.VISIBLE);
                    ivSort.setVisibility(View.VISIBLE);
                    rgRoom.setClickable(true);

                    getAppliancesFromDatabase();
                }
            }
        };
    }

    private Callback<List<ShopAppliance>> getDataChangedFilterListenerCallback() {
        return new Callback<List<ShopAppliance>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void runResultOnUiThread(List<ShopAppliance> result) {
                if(result!=null){
                    try {
                        List<ShopAppliance> filtList = result.parallelStream()
                                .filter(value -> value.getName().toLowerCase().
                                        contains(tietSearch.getText().toString().trim().toLowerCase())).
                                        collect(Collectors.toList());
                        appliances.clear();
                        appliances.addAll(filtList);
                    } catch (Exception ex){
                        appliances = new ArrayList<>();
                    }
                    notifyRvAdapter();
                }
            }
        };
    }

    private View.OnClickListener gerIvFilterCLickedListener() {
        return new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(getApplicationContext(),R.style.MyPopUp);
                createPopUpMenu(wrapper, v,getFilterItemsMenuClickListener(), R.menu.popup_filter_menu, filterId);
            }
        };
    }

    private PopupMenu.OnMenuItemClickListener getFilterItemsMenuClickListener() {
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                filterId = item.getItemId();
                ivFilter.setImageResource(ic_filter_selected);
                switch (item.getItemId()){
                   case R.id.menu_filter_dish_washer:
                       filterField = CategoryFilterType.dish_washer;
                       break;
                   case R.id.menu_filter_ac:
                       filterField = CategoryFilterType.AC;
                       break;
                   case R.id.menu_filter_boiler:
                       filterField = CategoryFilterType.boiler;
                       break;
                   case R.id.menu_filter_display:
                       filterField = CategoryFilterType.desktop_screen;
                       break;
                   case R.id.menu_filter_pc:
                       filterField = CategoryFilterType.desktop_pc;
                       break;
                   case R.id.menu_filter_freezer:
                       filterField = CategoryFilterType.freezer;
                       break;
                   case R.id.menu_filter_fridge:
                       filterField = CategoryFilterType.fridge;
                       break;
                   case R.id.menu_filter_microwaves:
                       filterField = CategoryFilterType.microwave;
                       break;
                   case R.id.menu_filter_oven:
                       filterField = CategoryFilterType.electric_oven;
                       break;
                   case R.id.menu_filter_washer:
                       filterField = CategoryFilterType.washer;
                       break;
                   case R.id.menu_filter_printer:
                       filterField = CategoryFilterType.printer;
                       break;
                   case R.id.menu_filter_purifier:
                       filterField = CategoryFilterType.air_purifier;
                       break;
                   case R.id.menu_filter_tv:
                       filterField = CategoryFilterType.TV;
                       break;
                   case R.id.menu_filter_clear:
                       filterField = CategoryFilterType.NONE;
                       filterId = 0;
                       ivFilter.setImageResource(ic_filter);
                       break;
               }
               getAppliancesFromDatabase();
               return true;
            }
        };
    }

    private View.OnClickListener getIvSortClickedListener() {
        return new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                createPopUpMenu(getApplicationContext(), v, getSortItemsMenuClickListener(), R.menu.popup_sort_menu, sortId);
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("RestrictedApi")
    private void createPopUpMenu(Context context, View v, PopupMenu.OnMenuItemClickListener menuClickListener, int layout_id, int selectedField) {
        PopupMenu popupMenu = new PopupMenu(context, v);
        popupMenu.setOnMenuItemClickListener(menuClickListener);
        popupMenu.inflate(layout_id);
        if(selectedField!=0){
            popupMenu.getMenu().findItem(selectedField).setIcon(ic_check);
        }

        popupMenu.setForceShowIcon(true);
        popupMenu.show();
    }

    private PopupMenu.OnMenuItemClickListener getSortItemsMenuClickListener() {
        return new PopupMenu.OnMenuItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                sortId = item.getItemId();
                switch (item.getItemId()){
                    case R.id.menu_sort_last_added:
                        sortType = SortType.LATEST_ADDED;
                        sortAppliances();
                        break;
                    case R.id.menu_sort_price_ascending:
                        sortType = SortType.PRICE_ASCENDING;
                        sortAppliances();
                        break;
                    case R.id.menu_sort_price_descending:
                        sortType = SortType.PRICE_DESCENDING;
                        sortAppliances();
                        break;
                }
                return true;
            }
        };
    }

    private RadioGroup.OnCheckedChangeListener getRbRoomCheckChangedListener() {
        return new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                clearFilters();
                switch (checkedId){
                    case R.id.search_appliance_rb_bathroom:
                        currentRoom = RoomType.bathroom;
                        break;
                    case R.id.search_appliance_rb_living_room:
                        currentRoom = RoomType.living_room;
                        break;
                    case R.id.search_appliance_rb_kitchen:
                        currentRoom = RoomType.kitchen;
                        break;
                    case R.id.search_appliance_rb_office:
                        currentRoom = RoomType.office;
                        break;
                    case R.id.search_appliance_rb_all:
                        currentRoom = RoomType.ALL;
                        break;
                }
                getAppliancesFromDatabase();
            }
        };
    }

    private void clearFilters() {
        sortType = SortType.NONE;
        sortId = 0;
        filterField = CategoryFilterType.NONE;
        filterId = 0;
        ivFilter.setImageResource(ic_filter);
    }

    private void enableTransitions() {
        //enable transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        // set transitions
        Explode explode = new Explode();
        explode.setDuration(1000);
        explode.setInterpolator(new DecelerateInterpolator());
        getWindow().setEnterTransition(explode);
        getWindow().setExitTransition(explode);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof TextInputEditText || v instanceof TextInputLayout) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    if(tietSearch.getText().toString().trim().length()<3){
                        tietSearch.setText("");
                    }
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}