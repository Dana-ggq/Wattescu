package com.example.wattescu.async;

public interface Callback<R> {
    void runResultOnUiThread(R result);
}
