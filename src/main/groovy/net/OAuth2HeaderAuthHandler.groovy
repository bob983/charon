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
    Random random = new Random()

    OAuth2HeaderAuthHandler(Vertx vertx) {
        this.redis = RedisClient.create(vertx, reditConfig)
    }

    @Override
    void handle(RoutingContext context) {
        def request = context.request();
        String authorization = request.headers().get(HttpHeaders.AUTHORIZATION.toString());
        if (authorization == null) {
            handle401(context);
        } else {
            def authHeaderKey = "key_" + authorization
            def companyId = random.nextInt(10)
            //println "Hm $companyId"
            redis.get(authHeaderKey) { response ->
                if (response.succeeded() && response.result() != null) {
                    context.put("userId", response.result())
                    context.put("companyId", companyId);
                } else {
                    if (authorization.startsWith("allow")) {
                        redis.set(authHeaderKey, "2") {
                            println "Setting expire to 30s"
                            redis.expire(authHeaderKey, 30) {}
                        }
                        context.put("userId", "2")
                        context.put("companyId", companyId);
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