package com.internxt.carcrashmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends Activity {

    FirebaseFirestore db;

    protected boolean checkPasswords(String p1, String p2) {
        // Check if field are empty
        if(p1.isEmpty() || p2.isEmpty())
            return false;

        if(p1.equals(p2) == false)
            return false;

        return true;
    }

    protected void goToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    protected void createUser(String username, String password, final String plate, final String model, final String car_id, final String ephone) {
        final String passwordHash = Base64.encodeToString(password.getBytes(), Base64.DEFAULT).replaceAll("\\s", "");
        final String user = username;

        final ProgressBar pb = findViewById(R.id.progressBar2);
        pb.setVisibility(View.VISIBLE);

        db.collection("user_info")
                .whereEqualTo("username", user)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if( task.isSuccessful() ) {
                            if(task.getResult().size()>0) {
                                pb.setVisibility(View.INVISIBLE);
                                // Username is already in use
                                Toast.makeText(RegisterActivity.this, "Username is unavailable", Toast.LENGTH_SHORT).show();
                            } else {
                                // Add the user to the repository
                                Map<String, Object> data = new HashMap<>();
                                data.put("username", user);
                                data.put("password", passwordHash);
                                data.put("ephone", ephone);

                                db.collection("user_info")
                                        .add(data)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>(){
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                pb.setVisibility(View.INVISIBLE);
                                                Log.d("DATA:", "Created user");
                                                goToLogin();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pb.setVisibility(View.INVISIBLE);
                                                Toast.makeText(RegisterActivity.this, "Unable to create doc. "+e.getMessage(), Toast.LENGTH_LONG).show();
                                                Log.d("DATA:","Error: "+e.getMessage());
                                            }
                                        });
                                Map<String, Object> data1 = new HashMap<>();
                                data1.put("user_id", user);
                                data1.put("plate", plate);
                                data1.put("car_id",car_id);
                                data1.put("model", model);

                                db.collection("user_car")
                                        .add(data1)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>(){
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                pb.setVisibility(View.INVISIBLE);
                                                Log.d("DATA:", "Created user car");
                                                goToLogin();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pb.setVisibility(View.INVISIBLE);
                                                Toast.makeText(RegisterActivity.this, "Unable to create doc. "+e.getMessage(), Toast.LENGTH_LONG).show();
                                                Log.d("DATA:","Error: "+e.getMessage());
                                            }
                                        });
                            }
                        }
                    }
                });
    }
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = FirebaseFirestore.getInstance();

        final Button register = findViewById(R.id.register);
        final EditText name = findViewById(R.id.name_reg);
        final EditText pass = findViewById(R.id.password_reg);
        final EditText user = findViewById(R.id.user_reg);
        final EditText model = findViewById(R.id.model_reg);
        final EditText plate = findViewById(R.id.plate_reg);
        final EditText car_id = findViewById(R.id.car_id);
        final EditText phone = findViewById(R.id.emer_phone);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    createUser(user.getText().toString(),pass.getText().toString(),
                            plate.getText().toString(),model.getText().toString(), car_id.getText().toString(),
                            phone.getText().toString());
                } catch(Exception e) {
                    Toast.makeText(RegisterActivity.this, "Issue:"+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

}
