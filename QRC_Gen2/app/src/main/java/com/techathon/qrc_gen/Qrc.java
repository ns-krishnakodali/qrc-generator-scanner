package com.techathon.qrc_gen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ActivityChooserView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.WriterException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class Qrc extends AppCompatActivity {
    private ImageView imageView;
    private Button button;
    private Uri imageUri;
    private Bitmap bitmap, qrBits;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseDatabase rootNode;
    private DatabaseReference dataReference;
    private String URL ;
    private Boolean bool;

    EditText inumber, idescription, ilocation, ilocationId, productrack, actbulk;
    Button qrgenerator, encryptqrgenerator, qrsave;
    ImageView qrcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrc);

        imageView = (ImageView)findViewById(R.id.imageView2);
        button = (Button)findViewById(R.id.Upload);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        rootNode = FirebaseDatabase.getInstance();
        dataReference = rootNode.getReference("Users");

        inumber = (EditText)findViewById(R.id.ItemNumber);
        idescription = (EditText)findViewById(R.id.Description);
        ilocation = (EditText)findViewById(R.id.Address);
        ilocationId = (EditText)findViewById(R.id.AddressId);
        productrack = (EditText)findViewById(R.id.ProductRack);
        actbulk = (EditText)findViewById(R.id.AcBu);
        qrgenerator = (Button)findViewById(R.id.ButtonQr);
        encryptqrgenerator = (Button)findViewById(R.id.EncryptedQR);
        qrsave = (Button) findViewById(R.id.Save);
        qrcode = (ImageView)findViewById(R.id.QRCodeHolder);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               selectImage(Qrc.this);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        qrgenerator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dataReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean verify = false;
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String id, aisle, rack;
                            id = ilocationId.getText().toString();
                            aisle = ilocation.getText().toString();
                            rack = productrack.getText().toString();
                            if(id.equals(ds.child("wareHouseId").getValue(String.class)) && aisle.equals(ds.child("aisleNumber").getValue(String.class)) && rack.equals(ds.child("productRack").getValue(String.class))){
                                verify = true;
                                break ;
                            }
                        }
                        if(snapshot.child(inumber.getText().toString()).exists() && inumber.getText().toString().length() != 0){
                            inumber.setError("Already Exists");
                            Toast.makeText(Qrc.this, "Item already exists", Toast.LENGTH_SHORT).show();
                        }
                        else if(verify){
                            ilocation.setError("Already filled");   ilocationId.setError("Already filled"); productrack.setError("Already filled");
                            Toast.makeText(Qrc.this, "Location is not available", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            generateQR();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });

        encryptqrgenerator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateEncryptedQR();
            }
        });

        qrsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveToDevice(qrBits);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                userDetails details = new userDetails(inumber.getText().toString(), idescription.getText().toString(), ilocationId.getText().toString(), ilocation.getText().toString(),
                        productrack.getText().toString(), actbulk.getText().toString(), URL);
                dataReference.child(inumber.getText().toString()).setValue(details);
            }
        });
    }

    private void selectImage(Context context) {
        final String[] options = {"Take Photo", "Choose Photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Image");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (options[which].equalsIgnoreCase("Take Photo")) {
                        Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(takePicture, 0);
                } else if (options[which].equalsIgnoreCase("Choose Photo")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);
                } else if (options[which].equalsIgnoreCase("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void uploadImage() {
        if (imageUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();
            final StorageReference filePath = storageReference.child("images/"+ inumber.getText().toString());
            filePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(Qrc.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                        URL = task.getResult().toString();
                    }
                }
            });
        }else {
            Toast.makeText(Qrc.this, "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateQR(){
        final String number, description, location, locationId, productRack, size;
        number= inumber.getText().toString();
        description = idescription.getText().toString();
        location = ilocation.getText().toString();
        locationId = ilocationId.getText().toString();
        productRack = productrack.getText().toString();
        size = actbulk.getText().toString();
        Pattern PlocationId = Pattern.compile("W[0-9]{2}");
        Pattern PaisleNum = Pattern.compile("[0-9]{2}");
        Pattern PproductRack = Pattern.compile("[0-9]{3}");
        
        if(number.isEmpty() || description.isEmpty() || location.isEmpty() || locationId.isEmpty() || productRack.isEmpty() || size.isEmpty() || URL == null) {
            if(number.isEmpty()){
                inumber.setError("Please enter a number");
            }
            else if(description.isEmpty()){
                idescription.setError("Please enter some description");
            }

            else if(location.isEmpty()){
                ilocation.setError("Please enter Aisle number");
            }
            else if (locationId.isEmpty()){
                ilocationId.setError("Please enter the Warehouse Id");
            }
            else if(productRack.isEmpty()){
                productrack.setError("Please enter ProductRack Number");
            }
            else if(size.isEmpty()){
                actbulk.setError("Please enter whether the item is Active or Bulky");
            }
            else{
                Toast.makeText(Qrc.this, "Please upload the image to generate URL.", Toast.LENGTH_SHORT).show();
            }
        }
        else if(number.length() != 7 || !PlocationId.matcher(locationId).matches() || !PaisleNum.matcher(location).matches() || !PproductRack.matcher(productRack).matches()){
            if(number.length()!=7) {
                inumber.setError("Item Number must have 7 digits");
            }
            else if(!PlocationId.matcher(locationId).matches()){
                ilocationId.setError("WarehouseId must be of the form W[0-9][0-9]");
            }
            else if (!PaisleNum.matcher(location).matches()){
                ilocation.setError("Aisle number must only have 2 digits");
            }
            else{
                productrack.setError("ProductRack number must only have 3 digits");
            }
        }
        else{
            String data = "Item Number: "+number+"\n\n"+"Item Description: "+description+"\n\n"+"Warehouse ID: "+locationId+"\n\n"+"Aisle Number: " + location+
                    "\n\n"+"ProductRack: "+ productRack+"\n\n"+"Size: "+size+"\n\n"+ "Image URL: "+URL;
            QRGEncoder qrgEncoder = new QRGEncoder(data, null, QRGContents.Type.TEXT, 500);
            try {
                qrBits = qrgEncoder.encodeAsBitmap();
                qrcode.setImageBitmap(qrBits);
                qrsave.setVisibility(View.VISIBLE);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateEncryptedQR(){
        final String number, description, location, locationId, productRack, size;
        number= inumber.getText().toString();
        description = idescription.getText().toString();
        location = ilocation.getText().toString();
        locationId = ilocationId.getText().toString();
        productRack = productrack.getText().toString();
        size = actbulk.getText().toString();
        Pattern PlocationId = Pattern.compile("W[0-9]{2}");
        Pattern PaisleNum = Pattern.compile("[0-9]{2}");
        Pattern PproductRack = Pattern.compile("[0-9]{3}");

        if(number.isEmpty() || description.isEmpty() || location.isEmpty() || locationId.isEmpty() || productRack.isEmpty() || size.isEmpty() || URL == null) {
            if(number.isEmpty()){
                inumber.setError("Please enter a number");
            }
            else if(description.isEmpty()){
                idescription.setError("Please enter some description");
            }

            else if(location.isEmpty()){
                ilocation.setError("Please enter Aisle number");
            }
            else if (locationId.isEmpty()){
                ilocationId.setError("Please enter the Warehouse Id");
            }
            else if(productRack.isEmpty()){
                productrack.setError("Please enter ProductRack Number");
            }
            else if(size.isEmpty()){
                actbulk.setError("Please enter whether the item is Active or Bulky");
            }
            else{
                Toast.makeText(Qrc.this, "Please upload the image to generate URL.", Toast.LENGTH_SHORT).show();
            }
        }
        else if(number.length() != 7 || !PlocationId.matcher(locationId).matches() || !PaisleNum.matcher(location).matches() || !PproductRack.matcher(productRack).matches()){
            if(number.length()!=7) {
                inumber.setError("Item Number must have 7 digits");
            }
            else if(!PlocationId.matcher(locationId).matches()){
                ilocationId.setError("WarehouseId must be of the form W[0-9][0-9]");
            }
            else if (!PaisleNum.matcher(location).matches()){
                ilocation.setError("Aisle number must only have 2 digits");
            }
            else{
                productrack.setError("ProductRack number must only have 3 digits");
            }
        }
        else{
            String data = "Item Number: "+number+"\n\n"+"Item Description: "+description+"\n\n"+"Warehouse ID: "+locationId+"\n\n"+"Aisle Number: " + location+
                    "\n\n"+"ProductRack: "+ productRack+"\n\n"+"Size: "+size+"\n\n"+ "Image URL: "+URL;
            int k,pattern[]={124,121,111,17,1,53,23,37,115};
            char[] arr = data.toCharArray();
            for(int i=0; i<arr.length; i++){
                k = i%9;
                arr[i] =(char)(((int)arr[i] + pattern[k]) % 128);
            }
            String encodedData = new String(arr);
            QRGEncoder qrgEncoder = new QRGEncoder(encodedData, null, QRGContents.Type.TEXT, 500);
            try {
                qrBits = qrgEncoder.encodeAsBitmap();
                qrcode.setImageBitmap(qrBits);
                qrsave.setVisibility(View.VISIBLE);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveToDevice(Bitmap finalBitmap) throws IOException {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath().toString();
        File myDir = new File(root + "/QR-Codes");
        myDir.mkdir();
        String fname = "Image-" + inumber.getText().toString() + ".jpg";
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
            Toast.makeText(Qrc.this, "QR Saved in " + root + "/QR-Codes", Toast.LENGTH_SHORT).show();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(Qrc.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        } else if ( resultCode == RESULT_OK && requestCode == 0) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            imageUri = getImageUri(Qrc.this, photo);
        }
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("remember", "false");
        editor.apply();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}