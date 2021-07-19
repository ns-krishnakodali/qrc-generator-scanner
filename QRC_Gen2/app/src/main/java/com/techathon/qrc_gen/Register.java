package com.techathon.qrc_gen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {
    EditText name, email, password, phone ;
    Button register;
    TextView loginbtn;
    ProgressBar progressbar;
    FirebaseAuth authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name = (EditText)findViewById(R.id.Name);
        email = (EditText)findViewById(R.id.Email);
        phone = (EditText)findViewById(R.id.Number);
        password = (EditText)findViewById(R.id.Password);
        register = (Button)findViewById(R.id.Register);
        loginbtn = (TextView)findViewById(R.id.ReLogin);

        authentication = FirebaseAuth.getInstance();
        progressbar = (ProgressBar)findViewById(R.id.ProgressBar);

        if(authentication.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email = email.getText().toString().trim();
                String Password = password.getText().toString().trim();

                if(TextUtils.isEmpty(Email)){
                    email.setError("Please enter your Email");
                    return;
                }
                if(TextUtils.isEmpty(Password)){
                    password.setError("Please enter your Password");
                    return;
                }
                if(Password.length() < 6){
                    password.setError("Password must have more than 5 characters ");
                    return;
                }

                progressbar.setVisibility(View.VISIBLE);
                authentication.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Register.this, "User Created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),Options.class));
                            Register.this.finish();
                        }
                        else{
                            Toast.makeText(Register.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this, MainActivity.class));
                Register.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}