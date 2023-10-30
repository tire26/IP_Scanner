package org;

import io.javalin.Javalin;
import io.javalin.plugin.rendering.JavalinRenderer;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;

public class WebInterface {
    public static void main(String[] args) {
        deleteDomainNamesFile();

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
            String ipWithMask = ctx.queryParam("ip");
            int numThreads = Integer.parseInt(ctx.queryParam("numThreads"));

            IPRangeScannerDistributor.scanIPRange(ipWithMask, numThreads);

            ctx.attribute("message", "Сканирование начато. Результаты будут сохранены в файл.");
            ctx.render("/templates/index.html");
        });

        return app;
    }

    public static void deleteDomainNamesFile() {
        File file = new File("domain_names.txt");
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Файл domain_names.txt удален.");
            } else {
                System.err.println("Ошибка при удалении файла domain_names.txt.");
            }
        } else {
            System.out.println("Файл domain_names.txt не существует.");
        }
    }
}
