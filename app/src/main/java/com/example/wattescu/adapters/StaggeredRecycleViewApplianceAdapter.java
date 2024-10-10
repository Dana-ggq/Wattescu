package com.example.wattescu.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.agrawalsuneet.dotsloader.loaders.TrailingCircularDotsLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.wattescu.R;
import com.example.wattescu.ShopApplianceDetailsActivity;
import com.example.wattescu.firebase.FirebaseSavedShopApplianceService;
import com.example.wattescu.entities.SavedShopAppliance;
import com.example.wattescu.entities.ShopAppliance;

import java.util.ArrayList;

public class StaggeredRecycleViewApplianceAdapter extends RecyclerView.Adapter<StaggeredRecycleViewApplianceAdapter.ViewHolder>{

    private static final String TAG = "StaggereRecycleViewAd";
    public static final String SAVED_APPLIANCE = "SAVED_APPLIANCE";
    public static final String NOT_SAVED_APPLIANCE = "NOT_SAVED_APPLIANCE";

    private ArrayList<ShopAppliance> appliances = new ArrayList<>();
    private Context mContext;

    //saved
    private ArrayList<SavedShopAppliance> savedShopAppliances = new ArrayList<>();
    private FirebaseSavedShopApplianceService firebaseSavedShopApplianceService;

    public StaggeredRecycleViewApplianceAdapter(ArrayList<ShopAppliance> appliances, Context mContext) {
        this.appliances = appliances;
        this.mContext = mContext;
    }

    public ArrayList<SavedShopAppliance> getSavedShopAppliances() {
        return savedShopAppliances;
    }

    public void setSavedShopAppliances(ArrayList<SavedShopAppliance> savedShopAppliances) {
        this.savedShopAppliances = savedShopAppliances;
    }

    public FirebaseSavedShopApplianceService getFirebaseSavedShopApplianceService() {
        return firebaseSavedShopApplianceService;
    }

    public void setFirebaseSavedShopApplianceService(FirebaseSavedShopApplianceService firebaseSavedShopApplianceService) {
        this.firebaseSavedShopApplianceService = firebaseSavedShopApplianceService;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_grid_search_appliance_item,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
       // Log.d(TAG,"onBindViewHolder: called.");

        initComponents(holder, position);
        holder.cvAppliance.setOnTouchListener(getOnTouchItemListener(holder, position));

    }

    private View.OnTouchListener getOnTouchItemListener(ViewHolder holder, int position) {

        GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(((CharSequence) holder.ivSave.getTag()).equals(mContext.getString(R.string.not_saved))){
                    //save
                    saveAppliance(holder, position);
                    return super.onDoubleTapEvent(e);

                }else {
                    //remove
                    deleteAppliance(holder, position);
                    return super.onDoubleTapEvent(e);

                }

            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //open details activity
                Intent intent = new Intent(mContext, ShopApplianceDetailsActivity.class);

                //if appliance saved send saved
                // if not send normal
                sendAppliance(intent, position);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent, getActivityOptions(holder).toBundle());

                return super.onSingleTapConfirmed(e);
            }
        });

        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        };
    }

    private ActivityOptions getActivityOptions(ViewHolder holder) {
        Pair[] pairs = new Pair[1];
        pairs[0] = new Pair<View, String>(holder.ivImage, mContext.getString(R.string.animImage));
        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext, pairs);
        return activityOptions;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void sendAppliance(Intent intent, int position) {
        SavedShopAppliance savedShopAppliance = savedShopAppliances.parallelStream()
                .filter(appliance -> appliance.getId() == appliances.get(position).getId())
                .findAny()
                .orElse(null);
        if(savedShopAppliance!= null){
            intent.putExtra(SAVED_APPLIANCE, savedShopAppliance);
        } else {
            intent.putExtra(NOT_SAVED_APPLIANCE, appliances.get(position));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteAppliance(ViewHolder holder, int position) {
        holder.ivSave.setImageResource(R.drawable.ic_favorite_border);
        holder.ivSave.setTag(mContext.getString(R.string.not_saved));

        SavedShopAppliance savedShopAppliance = savedShopAppliances.parallelStream()
                .filter(appliance -> appliance.getId() == appliances.get(position).getId())
                .findAny()
                .orElse(null);
        if(savedShopAppliance!= null){
            firebaseSavedShopApplianceService.removeAppliance(savedShopAppliance);
        }
    }

    private void saveAppliance(ViewHolder holder, int position) {
        holder.ivSave.setTag(mContext.getString(R.string.saved));
        holder.ivSave.setImageResource(R.drawable.ic_favorite);

        SavedShopAppliance savedShopAppliance =new SavedShopAppliance(appliances.get(position));
        firebaseSavedShopApplianceService.insertShopAppliance(savedShopAppliance);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initComponents(@NonNull ViewHolder holder, int position) {
        setImage(holder, position);
        populateTextViewContent(holder.tvTitle, appliances.get(position).getName());
        populateTextViewContent(holder.tvPrice, mContext.getString(R.string.price_format,appliances.get(position).getPrice()));
        setIsSaved(holder.ivSave, appliances.get(position).getId());
        if(appliances.get(position).getPower()!=null){
            populateTextViewContent(holder.tvPower, mContext.getString(R.string.format_power, Math.round(appliances.get(position).getPower()*1000)));
        } else{
            populateTextViewContent(holder.tvPower, mContext.getString(R.string.format_yearly_consumption, Math.round(appliances.get(position).getYearlyConsumption())));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setIsSaved(ImageView ivSave, long id) {
        SavedShopAppliance savedShopAppliance = savedShopAppliances.parallelStream()
                .filter(appliance -> appliance.getId()==id)
                .findAny()
                .orElse(null);
        if(savedShopAppliance!= null){
            ivSave.setImageResource(R.drawable.ic_favorite);
        } else{
            ivSave.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void populateTextViewContent(TextView textView, String value){
        if(value != null && !value.trim().isEmpty()){
            textView.setText((value));
        }else{
            textView.setText("");
        }
    }

    private void setImage(@NonNull ViewHolder holder, int position) {
        holder.progressBar.setVisibility(View.VISIBLE);
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.white_placeholder);

        Glide.with(mContext)
                .load(appliances.get(position).getImageUrl())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.ivImage);
    }

    @Override
    public int getItemCount() {
        return appliances.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        //views
        ImageView ivImage;
        TextView tvTitle;
        TextView tvPrice;
        TextView tvPower;
        ImageView ivSave;
        CardView cvAppliance;
        TrailingCircularDotsLoader progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            initComponents(itemView);
        }
        private void initComponents(View itemView) {
            this.ivImage = itemView.findViewById(R.id.layout_grid_appliance_iv_image);
            this.tvTitle = itemView.findViewById(R.id.layout_grid_appliance_tv_title);
            this.tvPrice = itemView.findViewById(R.id.layout_grid_appliance_tv_price);
            this.tvPower = itemView.findViewById(R.id.layout_grid_appliance_tv_power);
            this.ivSave = itemView.findViewById(R.id.layout_grid_appliance_iv_save);
            this.cvAppliance = itemView.findViewById(R.id.layout_grid_appliance_cv);
            this.progressBar = itemView.findViewById(R.id.layout_grid_appliance_progress_bar);
        }
    }
}
