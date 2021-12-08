package fr.isep.musictagger;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Predicate;

public class TagActivity extends AppCompatActivity {

    public static final String INTENT_SELECTED_FILE = "selectedFile";

    private Metadata originalMeta;
    private Metadata newMeta;

    private static final Predicate<Context> hasPermissions = ctx -> ctx.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

    private void newAlertDialog(String text) {
        new AlertDialog.Builder(this)
                .setMessage(text)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }

    private void init() {
        final Uri selectedFile = getIntent().getParcelableExtra(INTENT_SELECTED_FILE);
        try {
            final String path = PathUtils.getPath(this, selectedFile);
            originalMeta = new Metadata(path, getContentResolver().openInputStream(selectedFile));
        } catch (IOException e) {
            Log.e("App", "IO exception occurred", e);
            newAlertDialog("Sorry ! An error occurred while trying to open the file.");
        } catch (UnsupportedTagException e) {
            Log.e("App", "Unsupported tags", e);
            newAlertDialog("Sorry ! The tags in this file are not supported.");
        } catch (InvalidDataException e) {
            Log.e("App", "Invalid data", e);
            newAlertDialog("The data in this file are invalid.");
        } catch (Exception e) {
            Log.wtf("App", e);
            newAlertDialog("Something went wrong...");
        }
        Log.d("App", "Loaded file and its metadata");

        originalMeta.title.ifPresent(title -> ((EditText) findViewById(R.id.local_title)).setText(title));
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        if (!hasPermissions.test(this)) {
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("App", "Storage permission granted");
                    init();
                } else {
                    Log.w("App", "Storage permission denied");
                    newAlertDialog("Cannot access storage without permission.\nPlease grant storage permission to use this app.");
                }
            }).launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            init();
        }
    }
}