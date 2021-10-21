package com.github.chhsiao90.nitmproxy.examples;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.chhsiao90.nitmproxy.ConnectionContext;
import com.github.chhsiao90.nitmproxy.NitmProxyConfig;
import com.github.chhsiao90.nitmproxy.handler.protocol.http2.Http2DataFrameWrapper;
import com.github.chhsiao90.nitmproxy.handler.protocol.http2.Http2FrameWrapper;
import com.github.chhsiao90.nitmproxy.listener.NitmProxyListener;
import com.google.common.collect.ImmutableList;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import java.util.List;

public class HttpResponseInterceptor {

  public static final byte[] CONTENT = "<script>alert('intercepted');</script>".getBytes(UTF_8);;

  public static void main(String[] args) throws Exception {
    NitmProxyConfig config = new NitmProxyConfig();
    config.setListeners(ImmutableList.of(Interceptor::new));
    Bootstrap.start(config);
  }

  private static class Interceptor implements NitmProxyListener {
    private boolean intercepting;

    @Override
    public List<HttpObject> onHttp1Response(ConnectionContext connectionContext, HttpObject data) {
      if (data instanceof FullHttpResponse) {
        FullHttpResponse response = (FullHttpResponse) data;
        if (response.headers().get(CONTENT_TYPE, "").startsWith("text/html")) {
          if (response.headers().contains(CONTENT_LENGTH)) {
            response.headers().set(CONTENT_LENGTH, response.headers().getInt(CONTENT_LENGTH) + CONTENT.length);
          }
          return ImmutableList.of(
              new DefaultHttpResponse(response.protocolVersion(), response.status(), response.headers()),
              new DefaultHttpContent(response.content()),
              new DefaultLastHttpContent(connectionContext.alloc().buffer().writeBytes(CONTENT)));
        }
      }
      if (data instanceof HttpResponse) {
        HttpResponse response = (HttpResponse) data;
        intercepting = response.headers().get(CONTENT_TYPE, "").startsWith("text/html");
        if (intercepting && response.headers().contains(CONTENT_LENGTH)) {
          response.headers().set(CONTENT_LENGTH, response.headers().getInt(CONTENT_LENGTH) + CONTENT.length);
        }
        return ImmutableList.of(response);
      }
      if (intercepting && data instanceof LastHttpContent) {
        intercepting = false;
        LastHttpContent last = (LastHttpContent) data;
        return ImmutableList.of(
            new DefaultHttpContent(last.content()),
            new DefaultLastHttpContent(connectionContext.alloc().buffer().writeBytes(CONTENT)));
      }
      return ImmutableList.of(data);
    }

    @Override
    public List<Http2FrameWrapper<?>> onHttp2Response(ConnectionContext connectionContext,
        Http2FrameWrapper<?> frame) {
      if (Http2FrameWrapper.isFrame(frame, Http2HeadersFrame.class)) {
        Http2HeadersFrame headersFrame = (Http2HeadersFrame) Http2FrameWrapper.frame(frame);
        intercepting = headersFrame.headers().get(CONTENT_TYPE, "").toString().startsWith("text/html");
        if (intercepting && headersFrame.headers().contains(CONTENT_LENGTH)) {
          headersFrame.headers().setInt(CONTENT_LENGTH, headersFrame.headers().getInt(CONTENT_LENGTH) + CONTENT.length);
        }
      }
      if (intercepting && Http2FrameWrapper.isFrame(frame, Http2DataFrame.class)) {
        Http2DataFrameWrapper data = (Http2DataFrameWrapper) frame;
        if (data.frame().isEndStream() && data.frame().content().readableBytes() == 0) {
          intercepting = false;
          data.frame().content().writeBytes(CONTENT);
        } else if (data.frame().isEndStream()) {
          intercepting = false;
          return ImmutableList.of(
              new Http2DataFrameWrapper(data.streamId(), new DefaultHttp2DataFrame(data.content(), false)),
              new Http2DataFrameWrapper(data.streamId(), new DefaultHttp2DataFrame(
                  connectionContext.alloc().buffer().writeBytes(CONTENT),
                  true)));
        }
      }
      return ImmutableList.of(frame);
    }
  }
}
