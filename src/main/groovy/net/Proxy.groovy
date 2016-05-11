package net

class Proxy {

    def client

    Proxy(vertx, host, port) {
        client = vertx.createHttpClient(defaultHost: host, defaultPort: port, ssl: false)
    }

    def proxyRequest(routingContext) {
        def request = routingContext.request();
        def upstream = client.request(request.method(), request.path()) { response ->

            request.response().chunked = true
            request.response().statusCode = response.statusCode()

            response.handler { data ->
                request.response().write(data)
            }
            response.endHandler {
                request.response().end()
            }
        }

        request.handler { data ->
            upstream.write(data)
        }
        request.endHandler {
            upstream.end()
        }

    }

}
