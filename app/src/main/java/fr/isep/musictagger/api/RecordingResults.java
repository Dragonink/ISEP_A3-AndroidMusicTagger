package fr.isep.musictagger.api;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecordingResults implements Serializable {
    public static class Recording implements Serializable {
        public static class ArtistCredit implements Serializable {
            public String name;
            @SerializedName("joinphrase")
            public String joinPhrase;

            @NonNull
            public static String credit(@NonNull final List<ArtistCredit> credits) {
                final StringBuilder sb = new StringBuilder();
                credits.forEach(artist -> {
                    sb.append(artist.name);
                    Optional.ofNullable(artist.joinPhrase).ifPresent(sb::append);
                });
                return sb.toString();
            }
        }

        public static class Release implements Serializable {
            public static class Medium implements Serializable {
                public int position;
                @SerializedName("track-count")
                public int trackCount;
                @SerializedName("track-offset")
                public int trackOffset;
            }

            public String id;
            public String title;
            @SerializedName("artist-credit")
            public List<ArtistCredit> artistCredits;
            public List<Medium> media;
        }

        public String title;
        @SerializedName("artist-credit")
        public List<ArtistCredit> artistCredit;
        public List<Release> releases;
        public Release release;
    }

    public int count;
    public int offset;
    public List<Recording> recordings;

    public RecordingResults(@NonNull final RecordingResults results) {
        count = results.count;
        offset = results.offset;
        recordings = new ArrayList<>();
        results.recordings.forEach(recording -> recording.releases.forEach(release -> {
            Recording newRecording = new Recording();
            newRecording.title = recording.title;
            newRecording.artistCredit = recording.artistCredit;
            newRecording.release = release;
            recordings.add(newRecording);
        }));
    }
}
