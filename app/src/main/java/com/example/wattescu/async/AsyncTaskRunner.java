package com.example.wattescu.async;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncTaskRunner {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private  final Executor executor = Executors.newCachedThreadPool();


    public <R> void executeAsync(Callable<R> asyncOperation, Callback<R> mainThreadOperation){
        try{
            executor.execute((runAsyncOperation(asyncOperation, mainThreadOperation)));
        }catch (Exception e){
            Log.e("AsyngTaskRunner" ,"failed call executeasync" + e.getMessage());
        }
    }

    private <R> Runnable runAsyncOperation(Callable<R> asyncOperation, Callback<R> mainThreadOperation) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    R result = asyncOperation.call();
                    handler.post(runMainThreadOperation(result, mainThreadOperation));
                } catch (Exception e) {
                    Log.e("AsyngTaskRunner" ,"failed call executeasync" + e.getMessage());
                }
            }
        };
    }

    private <R> Runnable runMainThreadOperation(R result, Callback<R> mainThreadOperation) {
        return new Runnable() {
            @Override
            public void run() {
                mainThreadOperation.runResultOnUiThread(result);
            }
        };
    }
}
