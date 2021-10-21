package com.github.chhsiao90.nitmproxy.examples;

import com.github.chhsiao90.nitmproxy.NitmProxyConfig;
import com.github.chhsiao90.nitmproxy.tls.SimpleUnsafeAccessSupport;
import com.google.common.collect.ImmutableList;

public class UnsafeAccess {

  public static void main(String[] args) throws Exception {
    SimpleUnsafeAccessSupport unsafeAccessSupport = new SimpleUnsafeAccessSupport();

    NitmProxyConfig config = new NitmProxyConfig();
    config.setUnsafeAccessSupport(unsafeAccessSupport);
    config.setListeners(ImmutableList.of(unsafeAccessSupport::getInterceptor));
    Bootstrap.start(config);
  }
}
