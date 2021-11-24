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
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Object> chooseFile = registerForActivityResult(new ActivityResultContract<Object, Uri>() {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Object input) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/mpeg");
            return Intent.createChooser(intent, "Choose a music file");
        }

        @Override
        public Uri parseResult(int resultCode, @Nullable Intent intent) {
            if (intent != null) {
                return intent.getData();
            } else {
                return null;
            }
        }
    }, uri -> {
        Log.d("App", String.format("Selected file %s", uri));

        TextView tv = findViewById(R.id.selected_file);
        String[] path = uri.getLastPathSegment().split("/");
        tv.setText(path[path.length - 1]);
        tv.setVisibility(View.VISIBLE);
        findViewById(R.id.main_ll2).setVisibility(View.VISIBLE);
    });

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.open_file_picker).setOnClickListener(button -> chooseFile.launch(null));
    }
}