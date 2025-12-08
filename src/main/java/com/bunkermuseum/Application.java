package com.bunkermuseum;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.bunkermuseum"})
@EnableCaching
@Theme("default")
@PWA(
    name = "Bunkermuseum Mitgliederverwaltung",
    shortName = "Bunkermuseum",
    description = "Mitgliederverwaltung für das Bunkermuseum - Verwalten Sie Mitglieder, Buchungen und E-Mails",
    themeColor = "#000000",
    backgroundColor = "#ffffff",
    iconPath = "icons/icon-512x512.png",
    manifestPath = "manifest.json",
    offlinePath = "offline.html",
    startPath = "/",
    display = "standalone"
)
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
