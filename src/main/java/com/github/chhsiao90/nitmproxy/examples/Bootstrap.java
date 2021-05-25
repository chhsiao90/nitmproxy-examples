package com.github.chhsiao90.nitmproxy.examples;

import com.github.chhsiao90.nitmproxy.NitmProxy;
import com.github.chhsiao90.nitmproxy.NitmProxyConfig;

public class Bootstrap {

  public static void start(NitmProxyConfig config) throws Exception {
    NitmProxy proxy = new NitmProxy(config);
    proxy.start();
  }
}
