package fr.isep.musictagger;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Optional;

public class MainActivity extends AppCompatActivity {

    private Uri uri;
    private final ActivityResultLauncher<Object> chooseFile = registerForActivityResult(new ActivityResultContract<Object, Uri>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull final Context context, final Object input) {
            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/mpeg");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            return Intent.createChooser(intent, "Choose a music file");
        }

        @Override
        public Uri parseResult(final int resultCode, @Nullable final Intent intent) {
            return Optional.ofNullable(intent).map(Intent::getData).orElse(null);
        }
    }, uri -> {
        this.uri = uri;
        Log.d("App", String.format("Selected file %s", uri));

        TextView tv = findViewById(R.id.selected_file);
        String[] path = uri.getLastPathSegment().replace(":", "/").split("/");
        tv.setText(path[path.length - 1]);
        tv.setVisibility(View.VISIBLE);
        findViewById(R.id.main_ll2).setVisibility(View.VISIBLE);
    });

    private @Nullable
    Intent navigate(Class<?> cls) {
        if (Optional.ofNullable(uri).isPresent()) {
            final Intent intent = new Intent(this, cls);
            intent.putExtra(TagActivity.INTENT_SELECTED_FILE, uri);
            return intent;
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("No file selected.\nPlease select one before trying again.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return null;
        }
    }

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        findViewById(R.id.open_file_picker).setOnClickListener(button -> chooseFile.launch(null));
        findViewById(R.id.search_metadata).setOnClickListener(button -> startActivity(navigate(SearchActivity.class)));
        findViewById(R.id.manually_tag).setOnClickListener(button -> startActivity(navigate(TagActivity.class)));
    }
}