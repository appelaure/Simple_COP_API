package dk.kb.simplecopapi;

/**
 * Created by laap on 05-05-2017.
 */
import io.swagger.jaxrs.config.BeanConfig;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Bootstrap extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setBasePath("http://localhost:8080/api");
        beanConfig.setResourcePackage("io.swagger.resources");
        beanConfig.setScan(true);
    }
}