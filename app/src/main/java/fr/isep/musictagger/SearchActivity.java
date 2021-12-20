package fr.isep.musictagger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fr.isep.musictagger.api.MusicBrainzApi;
import fr.isep.musictagger.api.RecordingResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    private RecordingResults recordings;

    private static class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
        private final Context ctx;
        private final Serializable uri;
        private final List<RecordingResults.Recording> recordings;

        private static class ViewHolder extends RecyclerView.ViewHolder {
            public View view;
            public TextView title;
            public TextView artist;
            public TextView album;

            public ViewHolder(final View view) {
                super(view);

                this.view = view;
                title = view.findViewById(R.id.title);
                artist = view.findViewById(R.id.artist);
                album = view.findViewById(R.id.album);
            }
        }

        public ResultAdapter(final Context ctx, final Serializable uri, final List<RecordingResults.Recording> recordings) {
            this.ctx = ctx;
            this.uri = uri;
            this.recordings = recordings;
        }

        @Override
        public @NonNull
        ViewHolder onCreateViewHolder(@NonNull final ViewGroup group, final int type) {
            final View item = LayoutInflater.from(group.getContext()).inflate(R.layout.item_result, group, false);
            return new ViewHolder(item);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder view, final int position) {
            final RecordingResults.Recording recording = recordings.get(position);

            view.title.setText(recording.title);
            view.artist.setText(recording.artists());
            view.album.setText(recording.release.title);
            view.view.setOnClickListener(_view -> {
                final Intent intent = new Intent(ctx, TagActivity.class);
                intent.putExtra(TagActivity.INTENT_SELECTED_FILE, uri);
                intent.putExtra(TagActivity.INTENT_IMPORTED_METADATA, recording);
                ctx.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return recordings.size();
        }
    }

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_search);

        final RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new ResultAdapter(this, bundle.getSerializable(TagActivity.INTENT_SELECTED_FILE), recordings.recordings));

        ((MaterialButton) findViewById(R.id.search_button)).setOnClickListener(btn -> {
            final Context ctx = this;
            final EditText editText = findViewById(R.id.searchbar);
            if (editText.getText().length() > 0) {
                MusicBrainzApi.SERVICE.search(editText.getText().toString()).enqueue(new Callback<RecordingResults>() {
                    @Override
                    public void onResponse(@NonNull Call<RecordingResults> call, @NonNull Response<RecordingResults> response) {
                        recordings = Optional.ofNullable(response.body()).map(RecordingResults::new).orElse(null);
                    }

                    @Override
                    public void onFailure(@NonNull Call<RecordingResults> call, @NonNull Throwable t) {
                        Log.e("App", "An error occurred while trying to request from API", t);
                        new AlertDialog.Builder(ctx)
                                .setMessage("Sorry ! Something went wrong. Please try again.")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                });
            } else {
                editText.setError("Please enter a query");
            }
        });
    }
}