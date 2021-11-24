package fr.isep.musictagger;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Object> chooseFile = registerForActivityResult(new ActivityResultContract<Object, String>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Object input) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/mpeg");
            intent = Intent.createChooser(intent, "Choose a music file");
            return intent;
        }

        @Override
        public String parseResult(int resultCode, @Nullable Intent intent) {
            if (intent != null) {
                Uri uri = intent.getData();
                return uri.toString();
            } else {
                return null;
            }
        }
    }, s -> {
        Log.d("App", String.format("Selected file %s", s));
    });

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.open_file_picker).setOnClickListener(button -> chooseFile.launch(null));
    }
}