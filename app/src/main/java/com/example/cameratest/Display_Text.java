package com.example.cameratest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import java.util.Hashtable;

public class Display_Text extends AppCompatActivity {

    public static String scannedText;
    public static String Code;
    Intent prev ;
    TextView ScannedTextView;
    TextView langID;
    ImageButton TranslateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_text);
        //instancing and assigning parent intent :
        prev = getIntent();
        //Get message from parent :
        scannedText = prev.getStringExtra(MainActivity.EXTRA_MESSAGE);
        //Display scanned text from image in this textview :
        ScannedTextView = (TextView) findViewById(R.id.scannedTextView);

        //Putting scanned text in a container:
        ScannedTextView.setText(scannedText);
        ImageButton ClipBoardButton = (ImageButton) findViewById(R.id.ClipBoard);
        TranslateButton = (ImageButton) findViewById(R.id.Translate);

        //Calling LandIden to identify language ID from scannedText;
        LangIden(scannedText);

        //Adding text to clipboard after clicking ClipBoardButton :
        ClipBoardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager ClipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Scanned Text", scannedText);
                ClipBoard.setPrimaryClip(clip);

                Snackbar snackbar = Snackbar.make(v, "Text is copied to clipboard !", BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.show();
            }
        });

        TranslateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent TranslateView = new Intent(Display_Text.this, TranslationActivity.class);
                startActivity(TranslateView);
            }
        });

    }

    public void LangIden(String text){
        //Creating language Codes dictionary :
        Hashtable<String, String> Languages = new Hashtable<String, String>();
        Languages.put("en", "English");
        Languages.put("es", "Spanish");
        Languages.put("fr", "French");
        Languages.put("und", "Undetermined");

        //Display Language ID in this textview :
        langID = (TextView) findViewById(R.id.langId);
        //Call LanguageIdentification.getClient() to get an instance of LanguageIdentifier :
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient(
                new LanguageIdentificationOptions.Builder()
                        //Decrease confidence to 34 % :
                        .setConfidenceThreshold(0.34f)
                        .build()
        );
        //Passing text to identifyLanguage() to get language Code :
        languageIdentifier.identifyLanguage(text).addOnSuccessListener(new OnSuccessListener<String>() {

            @Override
            public void onSuccess(@Nullable String LanguageCode) {
                //Case where the language is not identified :
                Code = LanguageCode;
                if(Code.equals("und")){
                    langID.setText("Can't identify language");
                    TranslateButton.setVisibility(View.INVISIBLE);
                }
                //Case where the language is identified :
                else{
                    langID.setText("Language : " + Languages.get(Code));
                    TranslateButton.setVisibility(View.VISIBLE);
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }
}