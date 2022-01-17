package com.example.cameratest;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    //Instancing the message that will be exported to the next intent:
    public static String EXTRA_MESSAGE = "com.example.cameratest.MESSAGE";
    //Instancing widgets :
    ImageView preview;
    ImageButton camButton;
    ImageButton scanButton;
    //TextView res ;
    String message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preview = (ImageView) findViewById(R.id.preview);
        camButton = (ImageButton) findViewById(R.id.cameraButton);
        scanButton = (ImageButton) findViewById(R.id.scanButton);
        //res = (TextView) findViewById(R.id.result);
        //Check camera permission :
        if(checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED){
            //Case where camera permission is not granted
            requestPermissions(new String[]{CAMERA}, 101);
        }
    }

    public void takePicture(View view) {
        //Opening the camera :
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 101);
    }

    public void uploadPicture(View view) {
        Intent uploadIntent = new Intent();
        uploadIntent.setType("image/*");
        uploadIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(uploadIntent, 102);
    }

    //Pass to a new Display text intent :
    public void scanPicture(View view) {
        //Instancing an intent to display the image processing results:
        Intent DisplayResultIntent = new Intent(MainActivity.this, Display_Text.class);
        DisplayResultIntent.putExtra(EXTRA_MESSAGE, message);
        startActivity(DisplayResultIntent);
    }

    //Using ML kit text recognition model to extract text from photo :
    public void processImage(Bitmap Data){
        //Create a TextRecognizer instance :
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        InputImage image = InputImage.fromBitmap(Data, 0);

        Task<Text> finalResult;
        finalResult = recognizer.process(image) //processing image and getting content
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        //res.setText(visionText.getText());
                        //DisplayResultIntent.putExtra(EXTRA_MESSAGE, visionText.getText());
                        message = (String) visionText.getText();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //e.printStackTrace();
                        message = "error !";
                    }
                });
    }

    //This method is automatically ran after getting results from either :
    //      - ACTION_IMAGE_CAPTURE Intent (request code 101)
    //      - ACTION_GET_CONTENT Intent (request code 102)
    //it gives the possibility of manipulating the result that we get from each intent
    //the request code allows us to differentiate each intents result
    //it is not necessary if we are working with just one action
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap Data ;
        if (requestCode == 101){
            Bundle bundle = data.getExtras();
            //Extract image data and convert it to bitmap format :
            Data = (Bitmap) bundle.get("data");
            //Put image data to preview
            preview.setImageBitmap(Data);
            //Image processing begins here
            processImage(Data);
            //notifications(1, "Text Scanner","Scanning is complete");
            scanButton.setVisibility(View.VISIBLE);
        }
        if(requestCode == 102){
            Uri imageURI = data.getData();
            try {
                Data = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                preview.setImageBitmap(Data);
                //Image processing begins here
                processImage(Data);
                //notifications(1, "Text Scanner","Scanning is complete");
                scanButton.setVisibility(View.VISIBLE);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}