package fr.isep.musictagger.api;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CoverArtArchiveApi {
    String BASE_URL = "https://coverartarchive.org/";

    CoverArtArchiveApi SERVICE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoverArtArchiveApi.class);

    @GET("release/{id}/front")
    Call<byte[]> get(@Path("id") final String id);
}
