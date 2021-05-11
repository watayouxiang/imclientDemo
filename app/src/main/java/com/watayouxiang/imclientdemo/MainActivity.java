package com.watayouxiang.imclientdemo;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.watayouxiang.httpclient.TioHttpClient;
import com.watayouxiang.httpclient.callback.TioCallback;
import com.watayouxiang.httpclient.callback.TioCallbackImpl;
import com.watayouxiang.httpclient.listener.OnCookieListener;
import com.watayouxiang.httpclient.model.request.ConfigReq;
import com.watayouxiang.httpclient.model.request.ImServerReq;
import com.watayouxiang.httpclient.model.request.LoginReq;
import com.watayouxiang.httpclient.model.response.ConfigResp;
import com.watayouxiang.httpclient.model.response.ImServerResp;
import com.watayouxiang.httpclient.model.response.LoginResp;
import com.watayouxiang.httpclient.preferences.CookieUtils;
import com.watayouxiang.httpclient.preferences.HttpPreferences;
import com.watayouxiang.imclient.TioIMClient;
import com.watayouxiang.imclient.app.AppIMKickOutListener;
import com.watayouxiang.imclient.client.IMConfig;
import com.watayouxiang.imclient.packet.TioCommand;
import com.watayouxiang.imclient.packet.TioHandshake;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import okhttp3.Cookie;

public class MainActivity extends AppCompatActivity {

    public static final String BASE_URL = "https://test.tiocloud.com";
    public static final String HAND_SHAKE_KEY = "TesOt0T";
    public static final String ACCOUNT = null;// TODO input test account
    public static final String PWD = null;// TODO input test pwd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTioHttpClient();
        initTioIMClient();
        setIMConfig1();
        setHandshake1();
    }

    // ====================================================================================
    // 初始化
    // ====================================================================================

    private void initTioIMClient() {
        // 开启 TioIMClient 调试
        TioIMClient.setDebug(true);
        // 多端登录踢出监听
        TioIMClient.getInstance().setKickOutListener(new AppIMKickOutListener() {
            @Override
            public void onKickOut(String s) {

            }
        });
        // 进入后台自动断开连接
        // 默认为true，一般不做修改
        TioIMClient.getInstance().setAutoDisconnectOnAppBackground(false);
        // 恢复有网自动连接
        // 默认为true，一般不做修改
        TioIMClient.getInstance().setAutoConnectOnConnected(false);
        // 返回前台自动连接
        // 默认为true，一般不做修改
        TioIMClient.getInstance().setAutoConnectOnForeground(false);
    }

    private void initTioHttpClient() {
        // 初始化 TioHttpClient
        TioHttpClient httpClient = TioHttpClient.getInstance();
        // 开启 httpClient 调试
        httpClient.setDebug(true);
        // 修改 baseUrl 地址
        HttpPreferences.saveBaseUrl(BASE_URL);
        // token 变更监听
        TioHttpClient.getInstance().setOnCookieListener(new OnCookieListener() {
            @Override
            public void onSaveTioCookiesFromResponse(@NonNull @NotNull List<Cookie> cookies) {
                // 获取新 token
                String newTioCookie = CookieUtils.getCookie(cookies);
                // 同步更新 TioIMClient 中的 token
                if (newTioCookie != null) {
                    updateIMClientToken(newTioCookie);
                }
            }
        });
    }

    private void updateIMClientToken(String newToken) {
        TioIMClient.getInstance().updateToken(newToken);
    }

    // ====================================================================================
    // 配置 IMConfig
    // ====================================================================================

    private void setIMConfig1() {
        IMConfig config = TioIMClient.getInstance().getConfig();
        if (config != null) return;

        new ImServerReq().get(new TioCallback<ImServerResp>() {
            @Override
            public void onTioSuccess(ImServerResp imServerResp) {
                String ip = imServerResp.ip;
                int port = imServerResp.port;
                boolean openSsl = imServerResp.ssl == 1;
                long heartBeatInterval = imServerResp.timeout;
                heartBeatInterval = Math.max(heartBeatInterval / 2, IMConfig.HEARTBEAT_INTERVAL_MIN);

                setIMConfig2(ip, port, openSsl, heartBeatInterval);
            }

            @Override
            public void onTioError(String s) {

            }
        });
    }

    private void setIMConfig2(String ip, int port, boolean openSsl, long heartBeatInterval) {
        IMConfig imConfig = new IMConfig.Builder(ip, port)
                .setHeartBeatInterval(heartBeatInterval)
                .setOpenSsl(openSsl)
                .build();
        TioIMClient.getInstance().setConfig(imConfig);
    }

    // ====================================================================================
    // 配置 TioHandshake
    // ====================================================================================

    private void setHandshake1() {
        TioHandshake handshake = TioIMClient.getInstance().getHandshake();
        if (handshake != null) return;

        new ConfigReq().get(new TioCallback<ConfigResp>() {
            @Override
            public void onTioSuccess(ConfigResp configResp) {
                // 获取配置信息是为了，获取 token 的 key
                setHandshake2(ACCOUNT, PWD);
            }

            @Override
            public void onTioError(String s) {

            }
        });
    }

    private void setHandshake2(String account, String pwd) {
        LoginReq.getPwdInstance(pwd, account)
                .post(new TioCallback<LoginResp>() {
                    @Override
                    public void onTioSuccess(LoginResp loginResp) {
                        // 因为获取配置信息时，得到了 token 的 key；登录成功后 token 为变更为登录状态
                        // 所以这里获取的 token，必定是有值且为登录状态的
                        String token = CookieUtils.getCookie();

                        setHandshake3(token, HAND_SHAKE_KEY, "imclient sample", null);
                    }

                    @Override
                    public void onTioError(String s) {

                    }
                });
    }

    private void setHandshake3(String token, String handShakeKey, String channelId, String jpushRegId) {
        TioHandshake handshake = new TioHandshake.Builder(
                token,
                handShakeKey,
                TioCommand.WX_HANDSHAKE_REQ)
                .setCid(channelId)
                .setJpushinfo(jpushRegId)
                .build();
        TioIMClient.getInstance().setHandshake(handshake);
    }

    // ====================================================================================
    // click event
    // ====================================================================================

    public void onClick_connect(View view) {
        TioIMClient.getInstance().connect();
    }

    public void onClick_disconnect(View view) {
        TioIMClient.getInstance().disconnect();
    }

    public void onClick_login(View view) {
        LoginReq.getPwdInstance(PWD, ACCOUNT)
                .post(new TioCallbackImpl<>());
    }
}