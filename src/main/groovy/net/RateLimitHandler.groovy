package net

import io.vertx.core.Handler
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.web.RoutingContext
import io.vertx.groovy.redis.RedisClient

class RateLimitHandler implements Handler<RoutingContext> {

    def reditConfig = [
            host: "192.168.99.100",
            port: 32768
    ]
    RedisClient redis

    RateLimitHandler(Vertx vertx) {
        this.redis = RedisClient.create(vertx, reditConfig)
    }

    @Override
    void handle(RoutingContext context) {
        def companyId = context.get("companyId")
        def rateKey = "rate_company_$companyId"
        println "Checking rate limit for $rateKey"
        redis.incr(rateKey) { response ->
            if(response.failed()) {
                context.fail(500)
                println "Error ${response.cause()}"
            }
            println "Incremented rate for $rateKey to ${response.result()}"
            redis.expire(rateKey, 60) {}
            if (response.result() > 100) {
                println "Rate limit reached"
                handle429(context)
            }
            context.next()
        }
    }

    private static void handle429(RoutingContext context) {
        context.fail(429);
    }

}