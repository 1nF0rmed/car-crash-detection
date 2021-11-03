package com.internxt.carcrashmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class DashboardActivity extends Activity {

    FirebaseFirestore db;

    // Get the logged username
    protected String getLoggedUser() {
        //SharedPreferences sharedPref = LoginActivity.this.getPreferences(this.MODE_PRIVATE);
        SharedPreferences sharedPref = getSharedPreferences("user_data", 0);
        String name = sharedPref.getString("user", "Demo");

        return name;
    }

    protected void setLoggedOut(String user) {
        //SharedPreferences sharedPref = LoginActivity.this.getPreferences(this.MODE_PRIVATE);
        SharedPreferences sharedPref = getSharedPreferences("user_data", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("user_logged", false);
        editor.putString("user", "-");
        //editor.apply();
        editor.commit();
    }

    protected void hasCar(String _user) {
        final String user = _user;
        db.collection("user_car")
                .whereEqualTo("user_id", user)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            int size = task.getResult().size();
                            if(size==0) {
                                // Then load the AddCarActivity
                                Intent i = new Intent(DashboardActivity.this, AddCarActivity.class);
                                startActivity(i);
                                finish();
                            }
                        } else {
                            Toast.makeText(DashboardActivity.this, "Internet Issue", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    protected void updateDash() {
        final TextView _model = findViewById(R.id.car_model_dash);
        final TextView _plate = findViewById(R.id.car_plate_dash);

        String user = getLoggedUser();

        db.collection("user_car")
                .whereEqualTo("user_id", user)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if( task.isSuccessful() ) {
                            System.out.println("[LOG] Trying to get car details");
                            for(QueryDocumentSnapshot doc:task.getResult()) {
                                // Get the computer information
                                String model = doc.get("model").toString();
                                String plate = doc.get("plate").toString();

                                Log.d("DATA:", "model: "+model+" plate: "+plate);

                                _model.setText(model);
                                _plate.setText(plate);
                            }
                        } else {
                            Toast.makeText(DashboardActivity.this, "Firebase Internet Issue", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = FirebaseFirestore.getInstance();

        // Check if user has car
        final String user = getLoggedUser();
        hasCar(user);
        updateDash();

        final Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // log out
                setLoggedOut(user);
                Intent i = new Intent(DashboardActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }
}
