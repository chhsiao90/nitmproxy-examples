package com.github.chhsiao90.nitmproxy.examples;

import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static io.netty.buffer.Unpooled.copiedBuffer;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.chhsiao90.nitmproxy.NitmProxyConfig;
import com.github.chhsiao90.nitmproxy.listener.HttpListener;
import com.google.common.collect.ImmutableList;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.Optional;

public class HttpInterceptor {

  public static void main(String[] args) throws Exception {
    NitmProxyConfig config = new NitmProxyConfig();
    config.setHttpListeners(ImmutableList.of(new Interceptor()));
    Bootstrap.start(config);
  }

  private static class Interceptor implements HttpListener {
    @Override
    public Optional<FullHttpResponse> onHttp1Request(FullHttpRequest request) {
      if (request.method() == HttpMethod.CONNECT) {
        return Optional.empty();
      }
      DefaultFullHttpResponse response = new DefaultFullHttpResponse(
          HttpVersion.HTTP_1_1, HttpResponseStatus.OK, copiedBuffer("intercepted", UTF_8));
      response.headers().add(CONTENT_TYPE, "text/plain")
          .add(CONTENT_LENGTH, response.content().readableBytes());
      return Optional.of(response);
    }
  }
}
