package org.example;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.*;


public class Main {
    public  static int limitNum = 1;
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setPort(5432)
                .setHost("39.100.86.193")
                .setDatabase("k_media_platform")
                .setUser("postgres")
                .setPassword("Javarustc++11.");
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(20);
        Pool pool = PgBuilder
                .pool()
                .connectingTo(connectOptions)
                .with(poolOptions)
                .using(vertx)
                .build();
        router.route().handler(ctx -> {
            HttpServerResponse response = ctx.response();
            int limit = (int) ((Math.random()+1)*10000);
            Future<RowSet<Row>> rowSetFuture = pool.query("select * from media_resources limit "+limit).execute();
            rowSetFuture.onComplete(ar -> {
                if (ar.succeeded()) {
                    RowSet<Row> rows = ar.result();
                    for (Row row : rows) {
                        response.end(row.getLong(0).toString());
                    }
                } else {
                    response.end("hello world");
                }
            });
        });
        server.requestHandler(router).listen(8888);
    }
}