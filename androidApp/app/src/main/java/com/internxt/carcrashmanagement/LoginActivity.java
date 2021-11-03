package com.internxt.carcrashmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends Activity {

    FirebaseFirestore db;
    boolean isCorrect = false;

    protected void moveToDash() {

        Intent i = new Intent(LoginActivity.this, DashboardActivity.class);
        startActivity(i);
        finish();
    }

    protected boolean checkLoggedIn() {
        //SharedPreferences sharedPref = LoginActivity.this.getPreferences(this.MODE_PRIVATE);
        SharedPreferences sharedPref = getSharedPreferences("user_data", 0);
        boolean isLoggedIn = sharedPref.getBoolean("user_logged", false);

        return isLoggedIn;
    }

    protected void setLoggedIn(String user) {
        //SharedPreferences sharedPref = LoginActivity.this.getPreferences(this.MODE_PRIVATE);
        SharedPreferences sharedPref = getSharedPreferences("user_data", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("user_logged", true);
        editor.putString("user", user);
        //editor.apply();
        editor.commit();
    }

    protected void loginUser(String _user, final String password) {
        final String passwordHash = Base64.encodeToString(password.getBytes(), Base64.DEFAULT).replaceAll("\\s", "");
        //Toast.makeText(LoginActivity.this, "The password: "+passwordHash, Toast.LENGTH_SHORT).show();

        // Show loader
        final ProgressBar bar = findViewById(R.id.log_progress);

        final String user = _user;

        bar.setVisibility(View.VISIBLE);

        System.out.println("Here.");
        System.out.println("Password: "+passwordHash);
        isCorrect = false;
        // Get users with given username and password.

        // Reference to collection
        db.collection("user_info")
                .whereEqualTo("username", user)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        bar.setVisibility(View.INVISIBLE);
                        if( task.isSuccessful() ) {
                            try {
                                System.out.println(task.getResult().size());
                                for(QueryDocumentSnapshot doc: task.getResult()) {
                                    String docPass = doc.get("password").toString();
                                    if(docPass.equals(passwordHash))
                                        isCorrect = true;
                                    break;
                                }

                                /*Check if correct credentials*/
                                if(isCorrect) {
                                    System.out.println("Correct Credentials");
                                    Log.d("DATA:", "Password correct");


                                    /* Store as user logged in */
                                    setLoggedIn(user);
                                    /*Setup Transition to next Activity*/
                                    moveToDash();

                                } else {
                                    System.out.println("Wrong Credentials");
                                    Toast.makeText(LoginActivity.this, "Wrong Username/Password", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "ERROR: "+e, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "No Internet Issue", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        if(checkLoggedIn()) {
            System.out.println("Logged in: "+checkLoggedIn());
            Log.d("DATA:", "Logged in: "+checkLoggedIn());
            moveToDash();
        }

        final Button login = findViewById(R.id.login_button_login);
        final EditText username = findViewById(R.id.user_login);
        final EditText password = findViewById(R.id.password_login);
        final Button register = findViewById(R.id.login_button_login2);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser(username.getText().toString(), password.getText().toString());
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
