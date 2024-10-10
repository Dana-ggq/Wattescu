package com.example.wattescu.news;

import android.content.Context;
import android.widget.Toast;

import com.example.wattescu.R;
import com.example.wattescu.util.DateConverter;

import java.util.Calendar;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class NewsRequestManager {
    public static final String API_KEY = "a89076cafbf2435999229afd7083b859";
    public static final String QUERY = "electricitate";
    public static final String LANGUAGE = "ro";
    private Context context;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public NewsRequestManager(Context context) {
        this.context = context;
    }

    public void getNewsHeadlinse(OnFetchDataListener listener){
        CallNewsApi callNewsApi = retrofit.create(CallNewsApi.class);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date result = cal.getTime();

        Call<NewsApiResponse> call = callNewsApi.callHeadlines(QUERY, LANGUAGE, DateConverter.fromDate(new Date()), DateConverter.fromDate(result), API_KEY);
        try {
            call.enqueue(new Callback<NewsApiResponse>() {
                @Override
                public void onResponse(Call<NewsApiResponse> call, Response<NewsApiResponse> response) {
                    if(!response.isSuccessful()){
                        Toast.makeText(context, R.string.msg_news_fetching_error, Toast.LENGTH_SHORT).show();
                    }
                    listener.onFetchData(response.body().getArticles(),response.message());
                }
                @Override
                public void onFailure(Call<NewsApiResponse> call, Throwable t) {
                    listener.onError(String.valueOf(R.string.msg_news_fetching_error));
                }
            });
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }

    public interface CallNewsApi{
        @GET("everything")
        Call<NewsApiResponse> callHeadlines(
                @Query("q") String query,
                @Query("language") String language,
                @Query("from") String from,
                @Query("to") String to,
                @Query("apiKey") String apiKey

        );
    }
}
