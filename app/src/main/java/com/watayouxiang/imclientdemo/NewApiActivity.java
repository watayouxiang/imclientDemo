package com.watayouxiang.imclientdemo;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.watayouxiang.imclient.TioIMClient;
import com.watayouxiang.imclient.packet.TioPacket;
import com.watayouxiang.imclientdemo.model.PacketCommand;
import com.watayouxiang.imclientdemo.model.SysReq;
import com.watayouxiang.imclientdemo.model.SysResp;

import org.greenrobot.eventbus.Subscribe;

/**
 * <pre>
 *     author : TaoWang
 *     e-mail : watayouxiang@qq.com
 *     time   : 2021/05/11
 *     desc   : 自定义长连接请求以及响应
 * </pre>
 */
public class NewApiActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 注册事件总线
        TioIMClient.getInstance().getEventEngine().register(this);
        // 映射 "命令码" 和 "消息体"
        TioIMClient.getInstance().getCommandBodyMap().put(PacketCommand.SYS_RESP, SysResp.class);

        sendPacket();
    }

    // 发送消息
    private void sendPacket() {
        SysReq sysReq = new SysReq("给我开个vip");

        TioPacket sysReqPacket = new TioPacket();
        sysReqPacket.setCommand(PacketCommand.SYS_REQ);
        sysReqPacket.setBody(sysReq);

        TioIMClient.getInstance().sendPacket(sysReqPacket);
    }

    // 接收消息
    @Subscribe
    public void onWxChatItemInfoResp(SysResp resp) {
        boolean ok = resp.ok;
        // TODO do something
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销事件总线
        TioIMClient.getInstance().getEventEngine().unregister(this);
    }
}
