package skily_leyu.sixbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import skily_leyu.sixbox.service.ServiceApi;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loading), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkNowUseUrl(getSharedPreferences("LoginInfo", MODE_PRIVATE));
    }

    public void checkNowUseUrl(SharedPreferences sharedPreferences) {
        //读取当前使用的数据
        String nowUseUrl = sharedPreferences.getString("nowUseUrl", "");
        String nowUseScene = sharedPreferences.getString("nowUseScene", "");
        if (nowUseScene.isEmpty() || nowUseUrl.isEmpty()) {
            Intent intent = new Intent(LoadingActivity.this, IpSelectActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        MainApplication application = (MainApplication) getApplication();
        if (!nowUseUrl.startsWith("https://")) {
            nowUseUrl = "http://" + nowUseUrl;
        }
        try {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.connectTimeout(500, TimeUnit.MILLISECONDS);

            ServiceApi service = application.initHttpService(nowUseUrl, httpClient.build()).create(ServiceApi.class);
            Call<ResponseBody> call = service.getHome();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.d("SixBox_访问主页结果", response.toString());
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(LoadingActivity.this, MusicBoxActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.w("SixBox_访问主页失败", t.toString());
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.loading_002), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoadingActivity.this, IpSelectActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        } catch (Exception e) {
            Log.e("SixBox_未预期代码错误", "error", e);
        }
    }

}
