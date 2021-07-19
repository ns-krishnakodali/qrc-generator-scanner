package com.techathon.qrc_gen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    EditText lemail, lpassword;
    Button login ;
    TextView signIn;
    ProgressBar progressBar;
    FirebaseAuth Lauthentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lemail = (EditText)findViewById(R.id.LoginEmail);
        lpassword = (EditText)findViewById(R.id.LoginPassword);
        login = (Button)findViewById(R.id.Login);
        signIn = (TextView)findViewById(R.id.SignIn);
        progressBar = (ProgressBar)findViewById(R.id.progressBar2);
        Lauthentication = FirebaseAuth.getInstance();

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = lemail.getText().toString().trim();
                String Password = lpassword.getText().toString().trim();
                if(TextUtils.isEmpty(Email)){
                    lemail.setError("Please enter your Email");
                    return;
                }
                if(TextUtils.isEmpty(Password)){
                    lpassword.setError("Please enter your Password");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                Lauthentication.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(new Intent(getApplicationContext(),Options.class));
                            Toast.makeText(MainActivity.this, "Log In Successful", Toast.LENGTH_SHORT).show();
                            MainActivity.this.finish();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
    }
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}