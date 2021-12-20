package fr.isep.musictagger.api;

import java.util.List;

import fr.isep.musictagger.SearchActivity;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MusicBrainzApi {
    String BASE_URL = "https://musicbrainz.org/ws/2/";
    String USER_AGENT = "User-Agent: fr.isep.musictagger/1.0.0 (tanguy.berthoud@eleve.isep.fr)";
    String ACCEPT = "Accept: application/json";

    MusicBrainzApi SERVICE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MusicBrainzApi.class);

    @Headers({USER_AGENT, ACCEPT})
    @GET("recording")
    Call<RecordingResults> search(@Query("query") final String query);

    @Headers({USER_AGENT, ACCEPT})
    @GET("recording/{id}?inc=artist-credits+releases")
    Call<Object> get(@Path("id") final String id);
}
