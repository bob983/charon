package net

import io.vertx.core.Handler
import io.vertx.core.http.HttpHeaders
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.groovy.redis.RedisClient

class OAuth2HeaderAuthHandler implements Handler<RoutingContext> {

    def reditConfig = [
            host: "192.168.99.100",
            port: 32768
    ]
    RedisClient redis

    OAuth2HeaderAuthHandler(Vertx vertx) {
        this.redis = RedisClient.create(vertx, reditConfig)
    }

    @Override
    void handle(RoutingContext context) {
        def request = context.request();
        String authorization = request.headers().get(HttpHeaders.AUTHORIZATION.toString());
        println "Authorizataion is $authorization"

        if (authorization == null) {
            handle401(context);
        } else {
            def authHeaderKey = "key_" + authorization
            redis.get(authHeaderKey) { response ->
                if (response.succeeded() && response.result() != null) {
                    println "Key is in cache"
                    context.put("userId", response.result())
                    context.put("companyId", 10);
                } else {
                    println "Pretending to call external service, because key is not in cache"
                    if (authorization.startsWith("allow")) {
                        println "Adding key to cache"
                        redis.set(authHeaderKey, "2") {
                            println "Setting expire to 30s"
                            redis.expire(authHeaderKey, 30) {}
                        }
                        context.put("userId", "2")
                        context.put("companyId", 10);
                    } else {
                        handle401(context)
                    }
                }
                context.next();
            }
        }
    }

    private static void handle401(RoutingContext context) {
        context.fail(401);
    }

}