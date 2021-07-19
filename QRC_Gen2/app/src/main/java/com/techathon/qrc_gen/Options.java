package com.techathon.qrc_gen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Options extends AppCompatActivity {
    TextView newDetails, existingDetails;
    Button go;
    EditText itemNumber;
    FirebaseDatabase rootNode;
    DatabaseReference keyData;
    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        newDetails = (TextView) findViewById(R.id.Ndetails);
        existingDetails = (TextView) findViewById(R.id.Edetails);
        itemNumber = (EditText)findViewById(R.id.ItemNumber);
        go = (Button)findViewById(R.id.Go);
        progressbar = (ProgressBar)findViewById(R.id.progressBar);

        rootNode =FirebaseDatabase.getInstance();
        keyData = rootNode.getReference("Users");

        newDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Qrc.class));
            }
        });

        existingDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemNumber.setVisibility(View.VISIBLE);
                go.setVisibility(View.VISIBLE);
            }
        });

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressbar.setVisibility(View.VISIBLE);
                String number = itemNumber.getText().toString();
                if(number.isEmpty() || number.length() != 7){
                    if(number.isEmpty()) {
                        itemNumber.setError("Please enter a number");
                    }
                    else{
                        itemNumber.setError("Item Number must contain 7 digits");
                    }
                    progressbar.setVisibility(View.INVISIBLE);
                }
                else{
                    keyData.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.child(itemNumber.getText().toString()).exists()){
                                Intent intent = new Intent(getApplicationContext(), Viewdetails.class);
                                String number = itemNumber.getText().toString();
                                intent.putExtra("Key", number);
                                startActivity(intent);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                progressbar.setVisibility(View.INVISIBLE);
                            }
                            else{
                                Toast.makeText(Options.this, "This Item number is not registered", Toast.LENGTH_SHORT).show();
                                progressbar.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(Options.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            progressbar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        });
    }

}