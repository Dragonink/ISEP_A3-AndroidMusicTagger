package fr.isep.musictagger.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface CoverArtArchiveApi {
    String BASE_URL = "https://coverartarchive.org/";

    CoverArtArchiveApi SERVICE = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
            .create(CoverArtArchiveApi.class);

    @Headers({MusicBrainzApi.USER_AGENT})
    @GET("release/{id}/front")
    Call<ResponseBody> get(@Path("id") final String id);
}
