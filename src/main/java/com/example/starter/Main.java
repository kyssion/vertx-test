package com.example.starter;


import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.*;

import java.util.concurrent.locks.LockSupport;

public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DeploymentOptions options = new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD);

        // 异步部署Verticle
        vertx.deployVerticle(new MainVerticle(),options, deploymentResult -> {
            if (deploymentResult.succeeded()) {
                System.out.println("Deployment id is: " + deploymentResult.result());
            } else {
                System.out.println("Deployment failed: " + deploymentResult.cause());
            }
        });

        while (true) {
            LockSupport.park();
        }
        // 主线程继续执行，不会挂起
    }
}

class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        PgConnectOptions options = new PgConnectOptions()
                .setHost("39.100.86.193")
                .setPort(5432)
                .setDatabase("k_media_platform").setUser("postgres").setPassword("Javarustc++11.");
        PoolOptions poolOptions = new PoolOptions().setMaxSize(20);
        Pool pool = PgBuilder.pool().connectingTo(options).with(poolOptions).using(vertx).build();
        router.route("/").handler(ctx -> {
            HttpServerResponse response = ctx.response();
            response.setChunked(true);
            SqlConnection connection = Future.await(pool.getConnection());
            RowSet<Row> rowSetFuture =Future.await(connection.query("select * from media_resources limit 1000").execute());
            for (Row row : rowSetFuture) {
                response.write(row.getLong(0).toString()+'\n');
            }
            connection.close();
            response.end();
        });
        server.requestHandler(router).listen(8888).onComplete(res->{
            if (res.succeeded()) {
                startPromise.complete();
            } else {
                startPromise.fail(res.cause());
            }
        });
    }
}

