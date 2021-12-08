package fr.isep.musictagger;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import fr.isep.musictagger.fragments.StringTag;

public class TagActivity extends AppCompatActivity {

    public static final String INTENT_SELECTED_FILE = "selectedFile";

    private final Predicate<String> checkPermission = perm -> checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;

    private String path;
    private Metadata metadata;

    private StringTag title;
    private StringTag artist;
    private StringTag album;
    private StringTag albumArtist;

    private void newAlertDialog(String text) {
        new AlertDialog.Builder(this)
                .setMessage(text)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    Optional.ofNullable(metadata).ifPresent(Metadata::recycle);
                    finish();
                })
                .show();
    }

    private void save() {
        final FloatingActionButton fab = findViewById(R.id.action_save);
        fab.setEnabled(false);

        metadata.setTitle(title.getValue());
        metadata.setArtist(artist.getValue());
        metadata.setAlbum(album.getValue());
        metadata.setAlbumArtist(albumArtist.getValue());

        try {
            fab.setImageDrawable(null);
            ((ProgressBar) findViewById(R.id.progress)).setVisibility(View.VISIBLE);
            final Uri uri = Uri.parse("file://" + path);
            final OutputStream stream = getContentResolver().openOutputStream(uri, "wt");
            metadata.save(stream);
            Log.i("App", "File saved");
            Toast.makeText(this, "File saved !", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IOException e) {
            Log.e("App", "IO exception occurred", e);
            newAlertDialog("Sorry ! An error occurred while trying to write the file.");
        }
    }

    private void init() {
        final Uri uri = getIntent().getParcelableExtra(INTENT_SELECTED_FILE);
        try {
            path = PathUtils.getPath(this, uri);
            metadata = new Metadata(getContentResolver().openInputStream(uri));
            Log.d("App", "Loaded file and its metadata");

            final FragmentManager fragmentManager = getSupportFragmentManager();
            title = (StringTag) fragmentManager.findFragmentById(R.id.title);
            artist = (StringTag) fragmentManager.findFragmentById(R.id.artist);
            album = (StringTag) fragmentManager.findFragmentById(R.id.album);
            albumArtist = (StringTag) fragmentManager.findFragmentById(R.id.albumartist);
            metadata.getTitle().ifPresent(Objects.requireNonNull(title)::setDefaultValue);
            metadata.getArtist().ifPresent(Objects.requireNonNull(artist)::setDefaultValue);
            metadata.getAlbum().ifPresent(Objects.requireNonNull(album)::setDefaultValue);
            metadata.getAlbumArtist().ifPresent(Objects.requireNonNull(albumArtist)::setDefaultValue);
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

        final ActivityResultLauncher<String> writePermissionDialog = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d("App", "WRITE STORAGE permission granted");
                save();
            } else {
                Log.w("App", "WRITE STORAGE permission denied");
                newAlertDialog("Cannot access storage without permission.\nPlease grant storage permission to use this app.");
            }
        });
        ((FloatingActionButton) findViewById(R.id.action_save)).setOnClickListener(btn -> {
            if (!checkPermission.test(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                writePermissionDialog.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                save();
            }
        });
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);

        if (!checkPermission.test(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("App", "READ STORAGE permission granted");
                    init();
                } else {
                    Log.w("App", "READ STORAGE permission denied");
                    newAlertDialog("Cannot access storage without permission.\nPlease grant storage permission to use this app.");
                }
            }).launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            init();
        }
    }
}