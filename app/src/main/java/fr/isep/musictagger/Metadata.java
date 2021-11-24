package fr.isep.musictagger;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.util.Optional;

public class Metadata {
    public Optional<String> title;

    public Metadata(ID3v1 id3v1) {
        title = Optional.ofNullable(id3v1.getTitle());
    }

    public Metadata(ID3v2 id3v2) {
        title = Optional.ofNullable(id3v2.getTitle());
    }

    public static Metadata fromFile(Mp3File file) throws UnsupportedTagException {
        if (file.hasId3v2Tag()) {
            return new Metadata(file.getId3v2Tag());
        } else if (file.hasId3v1Tag()) {
            return new Metadata(file.getId3v1Tag());
        } else {
            throw new UnsupportedTagException();
        }
    }
}
