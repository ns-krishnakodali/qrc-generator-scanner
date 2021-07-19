package com.techathon.qrc_gen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class displayUpdate extends AppCompatActivity {
    FirebaseDatabase root;
    DatabaseReference reference;
    String iNumber, iDescription, iWareHouseId, iAisleNumber,iProductRack, iActbulk, URL, data;
    ImageView qr;
    Button qrsave;
    Bitmap qrBits;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_update);
        iNumber = getIntent().getStringExtra("Key1");
        qr = (ImageView)findViewById(R.id.QR);
        qrsave = (Button)findViewById(R.id.uSave);
        root = FirebaseDatabase.getInstance();
        reference = root.getReference("Users").child(iNumber);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                iDescription = snapshot.child("itemDescription").getValue(String.class);
                iWareHouseId = snapshot.child("wareHouseId").getValue(String.class);
                iAisleNumber = snapshot.child("aisleNumber").getValue(String.class);
                iProductRack = snapshot.child("productRack").getValue(String.class);
                iActbulk = snapshot.child("actBulk").getValue(String.class);
                URL = snapshot.child("imageURL").getValue(String.class);
                data = "Item Number: "+iNumber+"\n\n"+"Item Description: "+iDescription+"\n\n"+"Warehouse ID: "+iWareHouseId+"\n\n"+"Aisle Number: " + iAisleNumber+
                        "\n\n"+"ProductRack: "+ iProductRack+"\n\n"+"Size: "+iActbulk+"\n\n"+ "Image URL: "+URL;
                QRGEncoder qrgEncoder = new QRGEncoder(data, null, QRGContents.Type.TEXT, 800);
                try {
                    qrBits = qrgEncoder.encodeAsBitmap();
                    qr.setImageBitmap(qrBits);
                    qr.setVisibility(View.VISIBLE);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
                qrsave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            saveToDevice(qrBits);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveToDevice(Bitmap finalBitmap) throws IOException {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath().toString();
        File myDir = new File(root + "/QR-Codes");
        myDir.mkdir();
        String fname = "Image-" + iNumber + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            qrBits.compress(Bitmap.CompressFormat.JPEG, 90, out);
            try{
                Thread.sleep(500);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            Toast.makeText(displayUpdate.this, "QR Saved in " + root + "/QR-Codes", Toast.LENGTH_SHORT).show();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(displayUpdate.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
