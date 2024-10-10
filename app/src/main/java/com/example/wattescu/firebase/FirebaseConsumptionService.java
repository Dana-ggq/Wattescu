package com.example.wattescu.firebase;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FirebaseConsumptionService {

        private static final String CONSUMTION_REFERENCE = "consumption";
        private final DatabaseReference reference;
        private final String userKey;

        private static FirebaseConsumptionService service;

        private FirebaseConsumptionService(String userKey) {
            reference = FirebaseDatabase.getInstance().getReference(CONSUMTION_REFERENCE);
            this.userKey = userKey;
        }

        public static FirebaseConsumptionService getInstance(String userKey) throws Exception {
            if (userKey == null || userKey.trim().isEmpty()) {
                throw new Exception("invalid user key");
            }
            if (service == null) {
                synchronized (FirebaseCurrentConfigurationService.class) {
                    if (service == null) {
                        service = new FirebaseConsumptionService(userKey);
                    }
                }
            }
            return service;
        }

    public void deleteUserConsumtpion() {
        reference.child(userKey).removeValue();
    }

        //get users consumtpion
        public DatabaseReference getCurrentUsersConsumptionReference(){
            try{
                return reference.child(userKey);}
            catch (Exception ex){
                return null;
            }
        }

        //insert appliance
        //BULB OPERATIONS
        public void insertConsumptionDetails(String date, Double value) {
            if(date == null || date.length()==0) {
                return;
            }
            getCurrentUsersConsumptionReference().child(date).setValue(value);
        }


}
