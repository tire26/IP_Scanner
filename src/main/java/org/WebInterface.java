package org;

import io.javalin.Javalin;

import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class WebInterface {
    public static void main(String[] args) {
        Javalin app = getApp();
        app.start();
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(javalinConfig -> {
            JavalinThymeleaf.configure(getTemplateEngine());
            JavalinRenderer.register(JavalinThymeleaf.INSTANCE, ".html");
        });

        app.get("/", ctx -> ctx.render("/templates/index.html"));

        app.get("/scan", ctx -> {
            String[] enter = ctx.queryParam("startIP").split("/");
            String startIP = enter[0];
            String endIP = enter[1];
            int numThreads = Integer.parseInt(ctx.queryParam("numThreads"));

            IPRangeScannerDistributor.scanIPRange(startIP, endIP, numThreads);

            ctx.attribute("message", "Сканирование начато. Результаты будут сохранены в файл.");
            ctx.render("/templates/index.html");
        });

        return app;
    }
}
