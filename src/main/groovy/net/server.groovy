package net

import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.Router

def vertx = Vertx.vertx([
        workerPoolSize: 40
])


def server = vertx.createHttpServer()
def router = Router.router(vertx)
def proxy = new Proxy(vertx, 'localhost', 9090)

router.route().handler { routingContext ->
    def req = routingContext.request()
    routingContext.request().pause()
    routingContext.next()
}

router.route().handler(new OAuth2HeaderAuthHandler(vertx))
router.route().handler(new RateLimitHandler(vertx))

router.route().handler { routingContext ->
    proxy.proxyRequest(routingContext)
    routingContext.request().resume()
}

router.route().failureHandler({ routingContext ->
    routingContext.request().resume()
})

server.requestHandler(router.&accept).listen(8080)

