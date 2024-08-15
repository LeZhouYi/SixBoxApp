package skily_leyu.sixbox.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ServiceApi {

    @GET("home.html")
    Call<ResponseBody> getHome();

}
