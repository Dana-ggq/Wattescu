package com.example.wattescu.firebase;

import com.example.wattescu.async.Callback;
import com.example.wattescu.entities.Provider;

public interface FirebaseProviderOperations {

     void insertGeneralProvider();

     void updateProvider(Provider provider);

     void getProvider(Callback<Provider> callback);

}
