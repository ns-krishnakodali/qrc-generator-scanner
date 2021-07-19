package com.techathon.qrc_gen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;

public class Viewdetails extends AppCompatActivity implements Dialog.WarningDialogListener {
    TextView EitemNumber, EitemDescription, ElocationId, ElocationAddress, EproductRack, Eactbulk;
    ImageView image;
    Button delete, update;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    FirebaseStorage storage;
    StorageReference storageReference;
    String iNumber, iDescription, iWareHouseId, iAisleNumber,iProductRack, iActbulk;
    String UiDescription, UiWareHouseId, UiAisleNumber, UiProductRack, UiActbulk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewdetails);
        iNumber = getIntent().getStringExtra("Key");

        EitemNumber = (TextView) findViewById(R.id.Number);
        EitemDescription = (TextView) findViewById(R.id.ExistingDescription);
        ElocationId = (TextView) findViewById(R.id.ExistingId);
        ElocationAddress = (TextView) findViewById(R.id.ExistingAddress);
        EproductRack = (TextView) findViewById(R.id.EproductRack);
        Eactbulk = (TextView)findViewById(R.id.EactBulk);
        image = (ImageView)findViewById(R.id.Image);
        delete = (Button)findViewById(R.id.Delete);
        update = (Button)findViewById(R.id.Update);

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("Users").child(iNumber);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                iDescription = snapshot.child("itemDescription").getValue().toString();
                iWareHouseId = snapshot.child("wareHouseId").getValue().toString();
                iAisleNumber = snapshot.child("aisleNumber").getValue().toString();
                iProductRack = snapshot.child("productRack").getValue().toString();
                iActbulk = snapshot.child("actBulk").getValue().toString();
                EitemNumber.setText(iNumber);
                EitemDescription.setText(iDescription);
                ElocationId.setText(iWareHouseId);
                ElocationAddress.setText(iAisleNumber);
                EproductRack.setText(iProductRack);
                Eactbulk.setText(iActbulk);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Viewdetails.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReferenceFromUrl("gs://qrcgen-93ece.appspot.com/images").child(iNumber);
        try {
            final File file = File.createTempFile("QR-Code","jpg");
            storageReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    image.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Viewdetails.this, "Image failed to load .", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiDescription = EitemDescription.getText().toString();
                UiAisleNumber = ElocationAddress.getText().toString();
                UiWareHouseId = ElocationId.getText().toString();
                UiProductRack = EproductRack.getText().toString();
                UiActbulk = Eactbulk.getText().toString();
                if(UiDescription.equals(iDescription) && UiAisleNumber.equals(iAisleNumber) && UiWareHouseId.equals(iWareHouseId) && UiProductRack.equals(iProductRack) && UiActbulk.equals(iActbulk)){
                    Toast.makeText(Viewdetails.this, "Please change item details in order to update !", Toast.LENGTH_SHORT).show();
                }
                else{
                    reference.child("itemDescription").setValue(UiDescription);
                    reference.child("wareHouseId").setValue(UiWareHouseId);
                    reference.child("aisleNumber").setValue(UiAisleNumber);
                    reference.child("productRack").setValue(UiProductRack);
                    reference.child("actBulk").setValue(UiActbulk);
                    Toast.makeText(Viewdetails.this, "Updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), displayUpdate.class);
                    String number = iNumber;
                    intent.putExtra("Key1", number);
                    startActivity(intent);
                }

            }
        });
    }
    public void openDialog(){
        Dialog dialog = new Dialog();
        dialog.show(getSupportFragmentManager(), "Warning Dialog");
    }

    @Override
    public void onYesClicked() {
        Toast.makeText(Viewdetails.this, "Item Details are removed Succesfully.",Toast.LENGTH_SHORT).show();
        rootNode.getReference("Users").child(iNumber).removeValue();
    }
}