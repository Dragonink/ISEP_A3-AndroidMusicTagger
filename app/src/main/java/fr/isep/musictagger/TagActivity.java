package fr.isep.musictagger;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;

public class TagActivity extends AppCompatActivity {

    public static final String INTENT_SELECTED_FILE = "selectedFile";

    private Uri selectedFile;
    private Mp3File file;
    private Metadata originalMeta;
    private Metadata newMeta;

    private void newAlertDialog(String text) {
        new AlertDialog.Builder(this)
                .setMessage(text)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        selectedFile = (Uri) getIntent().getParcelableExtra(INTENT_SELECTED_FILE);
        try {
            File fs = new File(selectedFile.getPath());
            String path = fs.getAbsolutePath();
            file = new Mp3File(fs);
            originalMeta = Metadata.fromFile(file);
        } catch (IOException e) {
            Log.e("App", "IO exception occurred", e);
            e.printStackTrace();
            newAlertDialog("Sorry ! An error occurred while trying to open the file.");
        } catch (UnsupportedTagException e) {
            Log.e("App", "Unsupported tags", e);
            e.printStackTrace();
            newAlertDialog("Sorry ! The tags in this file are not supported.");
        } catch (InvalidDataException e) {
            Log.e("App", "Invalid data", e);
            e.printStackTrace();
            newAlertDialog("The data in this file are invalid.");
        }
        Log.d("App", "Loaded file and its metadata");

        originalMeta.title.ifPresent(title -> {
            ((EditText) findViewById(R.id.local_title)).setText(title);
        });
    }
}