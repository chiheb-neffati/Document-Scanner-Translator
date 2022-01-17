package com.example.cameratest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.lang.annotation.Target;
import java.util.Hashtable;

public class TranslationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Intent prev;
    String scannedText, SourceLanguageCode;
    Spinner LanguageList;
    TextView translatedText;
    TextView downloadStatus;
    Hashtable<String, String> LanguageCodes = new Hashtable<String, String>();
    Translator translator;
    ImageButton ClipBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translation_activity);

        prev = getIntent();
        scannedText = Display_Text.scannedText;
        SourceLanguageCode = Display_Text.Code;//prev.getStringExtra(Display_Text.LANG_CODE);
        translatedText = (TextView) findViewById(R.id.translatedText);
        ClipBoard = (ImageButton) findViewById(R.id.ClipBoard);

        //making a dropdown menu to select the target language for the translator :
        LanguageList = (Spinner) findViewById(R.id.LanguageList);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.Langauges,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        LanguageList.setAdapter(adapter);
        LanguageList.setOnItemSelectedListener(this);

        ClipBoard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ClipboardManager ClipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Scanned Text", translatedText.getText());
                ClipBoard.setPrimaryClip(clip);

                Snackbar snackbar = Snackbar.make(v, "Text is copied to clipboard !", BaseTransientBottomBar.LENGTH_SHORT);
                snackbar.show();
            }
        });
    }

    //This function is called when an item is selected :
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String targetLanguage = parent.getItemAtPosition(position).toString();

        LanguageCodes.put("French", "fr");
        LanguageCodes.put( "English", "en");
        LanguageCodes.put("Spanish", "es");

        downloadStatus = findViewById(R.id.download);
        downloadStatus.setText("Downloading model ...");
        translationPreparation(SourceLanguageCode, LanguageCodes.get(targetLanguage));
    }

    //This function is called when no item is selected :
    @Override
    public void onNothingSelected(AdapterView parent) {
        translatedText.setText("Please choose a target language !");
    }

    //This function uses the ML kit Translation model to translate a string "text"
    //The "source" parameter contains the language code of the scanned text
    //The "target" parameter contains the language code of the language we want to translate to :
    public void translationPreparation(String source, String target){
        downloadStatus = findViewById(R.id.download);
        //Creating a Translator object, configuring it with the source and target languages:
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.fromLanguageTag(source))
                        .setTargetLanguage(TranslateLanguage.fromLanguageTag(target))
                        .build();
        translator = Translation.getClient(options);
        getLifecycle().addObserver(translator);

        //Making sure the required translation model has been downloaded to the device :
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        downloadStatus.setText("downloaded");
                        translate();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        downloadStatus.setText(e.getMessage());
                    }
                });

    }

    public void translate(){
        //Finally translating the "text" put in this function's parameters :
        translator.translate(scannedText)
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(@NonNull String s) {
                        translatedText.setText(s);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        translatedText.setText(e.getMessage());
                    }
                });
    }
}