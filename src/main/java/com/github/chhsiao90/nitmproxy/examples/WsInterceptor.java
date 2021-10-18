package com.github.chhsiao90.nitmproxy.examples;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.chhsiao90.nitmproxy.ConnectionContext;
import com.github.chhsiao90.nitmproxy.NitmProxyConfig;
import com.github.chhsiao90.nitmproxy.listener.HttpListener;
import com.google.common.collect.ImmutableList;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WsInterceptor {

  public static void main(String[] args) throws Exception {
    NitmProxyConfig config = new NitmProxyConfig();
    config.setHttpListeners(ImmutableList.of(new Interceptor()));
    Bootstrap.start(config);
  }

  private static class Interceptor implements HttpListener {

    @Override
    public void onSendingWsFrame(ConnectionContext connectionContext, WebSocketFrame frame) {
      if (frame instanceof TextWebSocketFrame) {
        System.out.format("Send to %s%s %s %n",
            connectionContext.getServerAddr(),
            connectionContext.wsCtx().path(),
            frame.content().toString(UTF_8));
      }
    }

    @Override
    public void onReceivingWsFrame(ConnectionContext connectionContext, WebSocketFrame frame) {
      if (frame instanceof TextWebSocketFrame) {
        System.out.format("Receive from %s%s %s %n",
            connectionContext.getServerAddr(),
            connectionContext.wsCtx().path(),
            frame.content().toString(UTF_8));
        String content = frame.content().toString(UTF_8);
        if (content.contains("Chat Bot")) {
          frame.content().clear();
          frame.content().writeCharSequence(content.replace("Chat Bot", "nitmproxy"), UTF_8);
        }
      }
    }
  }
}
