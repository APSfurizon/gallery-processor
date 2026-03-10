package it.apsfurizon.galleryprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GalleryProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(GalleryProcessorApplication.class, args);
    }
}
