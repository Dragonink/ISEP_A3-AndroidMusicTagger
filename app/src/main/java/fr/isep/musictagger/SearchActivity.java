package fr.isep.musictagger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Optional;

import fr.isep.musictagger.api.MusicBrainzApi;
import fr.isep.musictagger.api.RecordingResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    private static class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
        private final Context ctx;
        private final Parcelable uri;
        private final RecordingResults recordings;

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

        public ResultAdapter(final Context ctx, final Parcelable uri, final RecordingResults recordings) {
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
            final RecordingResults.Recording recording = recordings.recordings.get(position);

            view.title.setText(recording.title);
            Optional.ofNullable(recording.artistCredit).ifPresent(val -> view.artist.setText(RecordingResults.Recording.ArtistCredit.credit(val)));
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
            return recordings.recordings.size();
        }
    }

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_search);

        final Parcelable uri = getIntent().getParcelableExtra(TagActivity.INTENT_SELECTED_FILE);

        final RecyclerView recycler = findViewById(R.id.recycler);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        recycler.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        findViewById(R.id.search_button).setOnClickListener(btn -> {
            final Context ctx = this;
            final EditText editText = findViewById(R.id.searchbar);
            if (editText.getText().length() > 0) {
                final ProgressBar progress = findViewById(R.id.progress);
                progress.setVisibility(View.VISIBLE);
                btn.setEnabled(false);
                MusicBrainzApi.SERVICE.search(editText.getText().toString()).enqueue(new Callback<RecordingResults>() {
                    @Override
                    public void onResponse(@NonNull Call<RecordingResults> call, @NonNull Response<RecordingResults> response) {
                        progress.setVisibility(View.GONE);
                        Optional.ofNullable(response.body())
                                .flatMap(results -> {
                                    try {
                                        return Optional.of(new RecordingResults(results));
                                    } catch (IllegalArgumentException e) {
                                        Log.e("App", "Invalid results format", e);
                                        new AlertDialog.Builder(ctx)
                                                .setMessage("Sorry ! Something went wrong. Please try again.")
                                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                .show();
                                        return Optional.empty();
                                    }
                                })
                                .ifPresent(recordings -> recycler.setAdapter(new ResultAdapter(ctx, uri, recordings)));
                        btn.setEnabled(true);
                    }

                    @Override
                    public void onFailure(@NonNull Call<RecordingResults> call, @NonNull Throwable t) {
                        progress.setVisibility(View.GONE);
                        Log.e("App", String.format("GET %s failure", call.request().url()), t);
                        new AlertDialog.Builder(ctx)
                                .setMessage("Sorry ! Something went wrong. Please try again.")
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                        btn.setEnabled(true);
                    }
                });
            } else {
                editText.setError("Please enter a query");
            }
        });
    }
}