package fr.isep.musictagger;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

public class Metadata {

    public static class PartOfSet {
        private final Integer total;

        public Optional<Integer> getTotal() {
            return Optional.ofNullable(total);
        }

        private final int number;

        public int getNumber() {
            return number;
        }

        public PartOfSet(@NonNull String s) {
            String[] split = s.split("/");

            number = Integer.parseInt(split[0]);
            if (split.length > 1) {
                total = Integer.parseInt(split[1]);
            } else {
                total = null;
            }
        }

        @Override
        @NonNull
        public String toString() {
            return getTotal().map(total -> String.format(Locale.getDefault(), "%d/%d", number, total)).orElse(String.valueOf(number));
        }
    }

    private final File tmpFile;
    private final Mp3File file;

    private byte[] cover;
    private String coverMime;

    public Optional<byte[]> getCover() {
        return Optional.ofNullable(cover);
    }

    public void setCover(final byte[] cover, final String coverMime) {
        this.cover = cover;
        this.coverMime = coverMime;
    }

    private String title;

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    private String artist;

    public Optional<String> getArtist() {
        return Optional.ofNullable(artist);
    }

    public void setArtist(final String artist) {
        this.artist = artist;
    }

    private String album;

    public Optional<String> getAlbum() {
        return Optional.ofNullable(album);
    }

    public void setAlbum(final String album) {
        this.album = album;
    }

    private String albumArtist;

    public Optional<String> getAlbumArtist() {
        return Optional.ofNullable(albumArtist);
    }

    public void setAlbumArtist(final String albumArtist) {
        this.albumArtist = albumArtist;
    }

    private PartOfSet track;

    public Optional<PartOfSet> getTrack() {
        return Optional.ofNullable(track);
    }

    public void setTrack(final PartOfSet track) {
        this.track = track;
    }

    private PartOfSet disc;

    public Optional<PartOfSet> getDisc() {
        return Optional.ofNullable(disc);
    }

    public void setDisc(final PartOfSet disc) {
        this.disc = disc;
    }

    private void getId3v1(@NonNull ID3v1 id3v1) {
        cover = null;
        title = id3v1.getTitle();
        artist = id3v1.getArtist();
        album = id3v1.getAlbum();
        albumArtist = null;
        track = Optional.ofNullable(id3v1.getTrack()).map(PartOfSet::new).orElse(null);
        disc = null;
    }

    private void getId3v2(@NonNull ID3v2 id3v2) {
        cover = id3v2.getAlbumImage();
        title = id3v2.getTitle();
        artist = id3v2.getArtist();
        album = id3v2.getAlbum();
        albumArtist = id3v2.getAlbumArtist();
        track = Optional.ofNullable(id3v2.getTrack()).map(PartOfSet::new).orElse(null);
        disc = Optional.ofNullable(id3v2.getPartOfSet()).map(PartOfSet::new).orElse(null);
    }

    public Metadata(@NonNull InputStream stream) throws IOException, InvalidDataException, UnsupportedTagException {
        tmpFile = File.createTempFile(Instant.now().toString(), ".mp3");
        tmpFile.deleteOnExit();
        final Path fPath = tmpFile.toPath();
        Files.copy(stream, fPath, StandardCopyOption.REPLACE_EXISTING);
        stream.close();
        Log.d("App", String.format("Copied contents to %s", fPath));
        file = new Mp3File(tmpFile);

        if (file.hasId3v2Tag()) {
            getId3v2(file.getId3v2Tag());
        } else if (file.hasId3v1Tag()) {
            getId3v1(file.getId3v1Tag());
            file.removeId3v1Tag();
        } else {
            throw new UnsupportedTagException();
        }
    }

    public void recycle() {
        tmpFile.delete();
    }

    public void save(@NonNull OutputStream stream) throws IOException {
        final ID3v2 id3v2 = file.getId3v2Tag();
        id3v2.setAlbumImage(cover, coverMime);
        id3v2.setTitle(title);
        id3v2.setArtist(artist);
        id3v2.setAlbum(album);
        id3v2.setAlbumArtist(albumArtist);
        getTrack().ifPresent(track -> id3v2.setTrack(track.toString()));
        getDisc().ifPresent(disc -> id3v2.setPartOfSet(disc.toString()));

        Files.copy(tmpFile.toPath(), stream);
        stream.flush();
        stream.close();
        recycle();
    }
}
