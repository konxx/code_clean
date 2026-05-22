package com.source;

import com.source.ui.Ui;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SourceDocxApplication implements CommandLineRunner {
   public static void main(String[] args) {
      (new SpringApplicationBuilder(new Class[]{SourceDocxApplication.class})).headless(false).run(args);
   }

   public void run(String... args) throws Exception {
      Ui.getInstance().initUI();
   }
}
