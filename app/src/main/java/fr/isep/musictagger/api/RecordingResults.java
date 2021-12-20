package fr.isep.musictagger.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecordingResults implements Serializable {
    public static class Recording implements Serializable {
        public static class ArtistCredit implements Serializable {
            public String name;
            public String joinPhrase;
        }

        public static class Release implements Serializable {
            public static class ReleaseGroup implements Serializable {
                public String id;
                public String title;
            }

            public String id;
            public String title;
            public ReleaseGroup releaseGroup;
        }

        public String id;
        public String title;
        public List<ArtistCredit> artistCredit;
        public List<Release> releases;
        public Release release;

        public String artists() {
            final StringBuilder sb = new StringBuilder();
            artistCredit.forEach(artist -> {
                sb.append(artist.name);
                if (artist.joinPhrase != null) {
                    sb.append(artist.joinPhrase);
                }
            });
            return sb.toString();
        }
    }

    public int count;
    public int offset;
    public List<Recording> recordings;

    public RecordingResults(final RecordingResults results) {
        count = results.count;
        offset = results.offset;
        recordings = new ArrayList<>();
        results.recordings.forEach(recording -> {
            recording.releases.forEach(release -> {
                RecordingResults.Recording newRecording = new Recording();
                newRecording.id = recording.id;
                newRecording.title = recording.title;
                newRecording.artistCredit = recording.artistCredit;
                newRecording.release = release;
                recordings.add(newRecording);
            });
        });
    }
}
