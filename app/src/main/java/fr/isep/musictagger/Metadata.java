package fr.isep.musictagger;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.NotSupportedException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Optional;

public class Metadata {
    private final String path;
    private final File tmpFile;
    private final Mp3File file;

    public Optional<String> title;

    protected void setId3v1(@NonNull ID3v1 id3v1) {
        title = Optional.ofNullable(id3v1.getTitle());
    }

    protected void setId3v2(@NonNull ID3v2 id3v2) {
        title = Optional.ofNullable(id3v2.getTitle());
    }

    public Metadata(@NonNull String path, @NonNull InputStream stream) throws IOException, InvalidDataException, UnsupportedTagException {
        this.path = path;

        tmpFile = File.createTempFile(Instant.now().toString(), ".mp3");
        tmpFile.deleteOnExit();
        final Path fPath = tmpFile.toPath();
        Files.copy(stream, fPath, StandardCopyOption.REPLACE_EXISTING);
        Log.d("App", String.format("Copied contents to %s", fPath));
        stream.close();
        file = new Mp3File(tmpFile);

        if (file.hasId3v2Tag()) {
            setId3v2(file.getId3v2Tag());
        } else if (file.hasId3v1Tag()) {
            setId3v1(file.getId3v1Tag());
        } else {
            throw new UnsupportedTagException();
        }
    }

    public void save() throws IOException, NotSupportedException {
        file.save(path);
        tmpFile.delete();
    }
}
