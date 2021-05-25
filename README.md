# Examples for [nitmproxy](https://github.com/chhsiao90/nitmproxy)

This repository contains examples for  [nitmproxy](https://github.com/chhsiao90/nitmproxy).

## Run Examples

You must generate a self-signed certificate before running the examples.

```shell
openssl req -new -newkey rsa:2048 -days 365 -nodes -x509 -keyout key.pem -out server.pem
```