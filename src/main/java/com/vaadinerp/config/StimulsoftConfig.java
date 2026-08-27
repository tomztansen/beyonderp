package com.vaadinerp.config;

import com.stimulsoft.base.licenses.StiLicense;
import com.stimulsoft.web.servlet.StiWebResourceServletJk;
import com.stimulsoft.webdesigner.servlet.StiWebDesignerActionServletJk;
import com.stimulsoft.webviewer.servlet.StiWebViewerActionServletJk;
import jakarta.servlet.http.HttpServlet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import jakarta.annotation.PostConstruct;

@Configuration
@PropertySource("classpath:stimulsoft.properties")
public class StimulsoftConfig {

    @Value("${stimulsoft.license.key}")
    private String licenseKey;

    @PostConstruct
    public void init() {
        try {
            StiLicense.setKey(licenseKey);
            int keyLen = (licenseKey != null) ? licenseKey.length() : 0;
            System.out.println("Stimulsoft Java Engine initialized. License Key Length: " + keyLen);
            if (keyLen < 10) {
                System.err.println("WARNING: License key is empty or too short. It might not be loaded correctly!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> stiWebDesignerServlet() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>();
        bean.setServlet(new StiWebDesignerActionServletJk());
        bean.addUrlMappings("/stimulsoft_webdesigner_action", "/stimulsoft_webdesigner_action/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> stiWebViewerServlet() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>();
        bean.setServlet(new StiWebViewerActionServletJk());
        bean.addUrlMappings(
                "/stimulsoft_webviewer_action", "/stimulsoft_webviewer_action/*",
                // Preview di dalam Web Designer mewarisi controller designer sebagai base
                // lalu menambahkan "/stimulsoft_webviewer_action", menghasilkan path ganda
                // "/stimulsoft_webdesigner_action/stimulsoft_webviewer_action". Tanpa mapping ini,
                // path tersebut jatuh ke servlet designer (balas kosong) sehingga StiJsViewer
                // tidak terdefinisi dan Preview memutar tanpa henti. Prefix yang lebih panjang
                // ini menang atas "/stimulsoft_webdesigner_action/*" milik servlet designer.
                "/stimulsoft_webdesigner_action/stimulsoft_webviewer_action",
                "/stimulsoft_webdesigner_action/stimulsoft_webviewer_action/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean<HttpServlet> stiWebResourceServlet() {
        ServletRegistrationBean<HttpServlet> bean = new ServletRegistrationBean<>();
        bean.setServlet(new StiWebResourceServletJk());
        bean.addUrlMappings("/stimulsoft_web_resource", "/stimulsoft_web_resource/*");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
