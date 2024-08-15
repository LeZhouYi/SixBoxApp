package skily_leyu.sixbox;

import android.app.Application;
import android.util.Log;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainApplication extends Application {

    private Retrofit httpService = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setHttpService(Retrofit httpService){
        this.httpService = httpService;
    }

    public Retrofit getHttpService() {
        return httpService;
    }

    public Retrofit initHttpService(String baseUrl, OkHttpClient client){
        httpService = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return httpService;
    }
}
