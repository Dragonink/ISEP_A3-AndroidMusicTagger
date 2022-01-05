package fr.isep.musictagger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import fr.isep.musictagger.api.CoverArtArchiveApi;
import fr.isep.musictagger.api.RecordingResults;
import fr.isep.musictagger.fragments.ImageTag;
import fr.isep.musictagger.fragments.PartOfSetTag;
import fr.isep.musictagger.fragments.StringTag;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TagActivity extends AppCompatActivity {

    public static final String INTENT_SELECTED_FILE = "selectedFile";
    public static final String INTENT_IMPORTED_METADATA = "recording";

    public interface TagFragment {
        void copyImported();

        void resetLocal();
    }

    private final Predicate<String> checkPermission = perm -> checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
    private final ActivityResultLauncher<Object> saveFile = registerForActivityResult(new ActivityResultContract<Object, Uri>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull final Context context, final Object input) {
            final Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("audio/mpeg");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            metadata.getTitle().ifPresent(title -> intent.putExtra(Intent.EXTRA_TITLE, title));
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, path);
            return Intent.createChooser(intent, "Choose a save location");
        }

        @Override
        public Uri parseResult(final int resultCode, @Nullable final Intent intent) {
            return Optional.ofNullable(intent).map(Intent::getData).orElse(null);
        }
    }, uri -> {
        Log.d("App", String.format("Selected location %s", uri));

        try {
            final OutputStream os = this.getContentResolver().openOutputStream(uri, "wt");
            this.metadata.save(os);
            Log.i("App", "File saved");
            Toast.makeText(this, "File saved !", Toast.LENGTH_SHORT).show();
            finish();
        } catch (IOException e) {
            Log.e("App", "IO exception occurred", e);
            new AlertDialog.Builder(this)
                    .setMessage("Sorry ! An error occurred while trying to save the file.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    });

    private String path;
    private Metadata metadata;
    private String coverMime;

    private ImageTag cover;
    private StringTag title;
    private StringTag artist;
    private StringTag album;
    private StringTag albumArtist;
    private PartOfSetTag track;
    private PartOfSetTag disc;

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

        metadata.setCover(cover.getValue(), coverMime);
        metadata.setTitle(title.getValue());
        metadata.setArtist(artist.getValue());
        metadata.setAlbum(album.getValue());
        metadata.setAlbumArtist(albumArtist.getValue());
        metadata.setTrack(track.getValue());
        metadata.setDisc(disc.getValue());

        fab.setImageDrawable(null);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        saveFile.launch(null);
    }

    private void init() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        cover = (ImageTag) fragmentManager.findFragmentById(R.id.cover);
        title = (StringTag) fragmentManager.findFragmentById(R.id.title);
        artist = (StringTag) fragmentManager.findFragmentById(R.id.artist);
        album = (StringTag) fragmentManager.findFragmentById(R.id.album);
        albumArtist = (StringTag) fragmentManager.findFragmentById(R.id.albumartist);
        track = (PartOfSetTag) fragmentManager.findFragmentById(R.id.track);
        disc = (PartOfSetTag) fragmentManager.findFragmentById(R.id.disc);

        Optional.ofNullable((RecordingResults.Recording) getIntent().getSerializableExtra(INTENT_IMPORTED_METADATA)).ifPresent(recording -> {
            Optional.ofNullable(recording.release.id).ifPresent(id -> CoverArtArchiveApi.SERVICE.get(id).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (Optional.ofNullable(response.body()).isPresent()) {
                        try {
                            cover.setImportedValue(response.body().bytes());
                            coverMime = response.headers().get("Content-Type");
                        } catch (IOException e) {
                            Log.e("App", "IO exception occurred", e);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.w("App", String.format("GET %s failure", call.request().url()), t);
                }
            }));
            title.setImportedValue(recording.title);
            Optional.ofNullable(recording.artistCredit).ifPresent(val -> artist.setImportedValue(RecordingResults.Recording.ArtistCredit.credit(val)));
            Optional.ofNullable(recording.release.title).ifPresent(title::setImportedValue);
            Optional.ofNullable(recording.release.artistCredits).ifPresent(val -> albumArtist.setImportedValue(RecordingResults.Recording.ArtistCredit.credit(val)));
            Optional.ofNullable(recording.release.media.get(0)).ifPresent(medium -> {
                disc.setImportedValue(new Metadata.PartOfSet(String.format(Locale.getDefault(), "%d", medium.position)));
                track.setImportedValue(new Metadata.PartOfSet(String.format(Locale.getDefault(), "%d/%d", medium.trackOffset + 1, medium.trackCount)));
            });
        });

        ((FloatingActionButton) findViewById(R.id.action_copy_all)).setOnClickListener(btn -> {
            cover.copyImported();
            title.copyImported();
            artist.copyImported();
            album.copyImported();
            albumArtist.copyImported();
            track.copyImported();
            disc.copyImported();
        });
        ((FloatingActionButton) findViewById(R.id.action_reset_all)).setOnClickListener(btn -> {
            cover.resetLocal();
            title.resetLocal();
            artist.resetLocal();
            album.resetLocal();
            albumArtist.resetLocal();
            track.resetLocal();
            disc.resetLocal();
        });

        final Uri uri = getIntent().getParcelableExtra(INTENT_SELECTED_FILE);
        try {
            path = PathUtils.getPath(this, uri);
            metadata = new Metadata(getContentResolver().openInputStream(uri));
            Log.d("App", "Loaded file and its metadata");

            metadata.getCover().ifPresent(Objects.requireNonNull(cover)::setDefaultValue);
            metadata.getTitle().ifPresent(Objects.requireNonNull(title)::setDefaultValue);
            metadata.getArtist().ifPresent(Objects.requireNonNull(artist)::setDefaultValue);
            metadata.getAlbum().ifPresent(Objects.requireNonNull(album)::setDefaultValue);
            metadata.getAlbumArtist().ifPresent(Objects.requireNonNull(albumArtist)::setDefaultValue);
            metadata.getTrack().ifPresent(Objects.requireNonNull(track)::setDefaultValue);
            metadata.getDisc().ifPresent(Objects.requireNonNull(disc)::setDefaultValue);
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
        findViewById(R.id.action_save).setOnClickListener(btn -> {
            if (!checkPermission.test(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                writePermissionDialog.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                save();
            }
        });
    }

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
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