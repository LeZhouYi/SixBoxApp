package skily_leyu.sixbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import skily_leyu.sixbox.service.ServiceApi;
import skily_leyu.sixbox.widget.CustomTouchLayout;

public class IpSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ip_select);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ipSelect), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CustomTouchLayout rootLayout = findViewById(R.id.ipSelect);
        EditText sceneInput = findViewById(R.id.ipSelect_006);
        EditText urlInput = findViewById(R.id.ipSelect_007);
        TextView urlErrorView = findViewById(R.id.ipSelect_012);
        TextView sceneErrorView = findViewById(R.id.ipSelect_013);
        Button saveButton = findViewById(R.id.ipSelect_010);
        Button comfirmButton = findViewById(R.id.ipSelect_009);
        FrameLayout loadingLayout = findViewById(R.id.ipSelect_014);
        SharedPreferences sharedPreferences = getSharedPreferences("LoginInfo", MODE_PRIVATE);

        urlInput.setText(sharedPreferences.getString("nowUseUrl", ""));
        sceneInput.setText(sharedPreferences.getString("nowUseScene", ""));

        //事件处理
        loadingLayout.setOnClickListener(view -> {
        });
        rootLayout.setOnTouchListener((view, motionEvent) -> {
            View nowView = IpSelectActivity.this.getCurrentFocus();
            if (nowView instanceof EditText) {
                Rect outRect = new Rect();
                nowView.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY())) {
                    InputMethodManager imm = (InputMethodManager) IpSelectActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(nowView.getWindowToken(), 0);
                    }
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
            }
            return false;
        });
        urlInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                checkUrlInput(urlInput, urlErrorView);
            } else {
                urlErrorView.setVisibility(View.INVISIBLE);
            }
        });
        sceneInput.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                checkSceneInput(sceneInput, sceneErrorView);
            }else{
                sceneErrorView.setVisibility(View.INVISIBLE);
            }
        });
        saveButton.setOnClickListener(view -> {
            String urlText = urlInput.getText().toString();
            if (checkUrlInput(urlInput, urlErrorView)) {
                String routeText = sceneInput.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (routeText.isEmpty()) {
                    editor.putString("default", urlText);
                } else {
                    editor.putString(routeText, urlText);
                }
                editor.apply();
            }
        });
        comfirmButton.setOnClickListener(view -> {
            if (checkUrlInput(urlInput, urlErrorView) && checkSceneInput(sceneInput, sceneErrorView)) {
                String urlText = urlInput.getText().toString();
                String routeText = sceneInput.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("nowUseUrl", urlText);
                editor.putString("nowUseScene", routeText);
                editor.apply();
                checkNowUseUrl(getSharedPreferences("LoginInfo", MODE_PRIVATE), urlErrorView);
            }
        });
    }

    public void checkNowUseUrl(SharedPreferences sharedPreferences, TextView urlErrorView) {
        //读取当前使用的数据
        String nowUseUrl = sharedPreferences.getString("nowUseUrl", "");
        String nowUseScene = sharedPreferences.getString("nowUseScene", "");
        if (nowUseScene.isEmpty() || nowUseUrl.isEmpty()) {
            return;
        }
        MainApplication application = (MainApplication) getApplication();
        if (!nowUseUrl.startsWith("https://")) {
            nowUseUrl = "http://" + nowUseUrl;
        }
        FrameLayout loadingLayout = findViewById(R.id.ipSelect_014);
        loadingLayout.setVisibility(View.VISIBLE);
        try {
            ServiceApi service = application.getHttpService().create(ServiceApi.class);
            Call<ResponseBody> call = service.getHome();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    Log.d("SixBox_访问主页结果", response.toString());
                    loadingLayout.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(IpSelectActivity.this, MusicBoxActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.w("SixBox_访问主页失败", t.toString());
                    loadingLayout.setVisibility(View.GONE);
                    urlErrorView.setVisibility(View.VISIBLE);
                    urlErrorView.setText(R.string.ipSelect_012);
                }
            });
        } catch (Exception e) {
            loadingLayout.setVisibility(View.GONE);
            Log.e("SixBox_未预期代码错误", "error", e);
        }
    }

    protected boolean checkSceneInput(EditText sceneInput, TextView sceneErrorView) {
        String inputText = sceneInput.getText().toString();
        if (inputText.isEmpty()) {
            sceneErrorView.setVisibility(View.VISIBLE);
            sceneErrorView.setText(R.string.ipSelect_011);
        } else {
            return true;
        }
        return false;
    }

    protected boolean checkUrlInput(EditText urlInput, TextView urlErrorView) {
        String inputText = urlInput.getText().toString();
        if (inputText.isEmpty()) {
            urlErrorView.setVisibility(View.VISIBLE);
            urlErrorView.setText(R.string.ipSelect_009);
        } else if (isUrlInvalid(inputText)) {
            urlErrorView.setVisibility(View.VISIBLE);
            urlErrorView.setText(R.string.ipSelect_010);
        } else {
            return true;
        }
        return false;
    }

    protected boolean isUrlInvalid(String inputText) {
        /*
        校验域名输入框的内容
         */
        String regex = "((https://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*|(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(inputText);
        return !matcher.find();
    }

}