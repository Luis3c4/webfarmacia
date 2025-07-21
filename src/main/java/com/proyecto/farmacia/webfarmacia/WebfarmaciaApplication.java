package com.proyecto.farmacia.webfarmacia;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class WebfarmaciaApplication {

	public static void main(String[] args) {
                            Dotenv dotenv = Dotenv.configure().load();
        System.setProperty("SUPABASE_URL", dotenv.get("SUPABASE_URL"));
        System.setProperty("SUPABASE_SERVICE_ROLE_KEY", dotenv.get("SUPABASE_SERVICE_ROLE_KEY"));
        System.setProperty("SUPABASE_ANON_KEY", dotenv.get("SUPABASE_ANON_KEY"));

        System.setProperty("SUPABASE_DB_URL", dotenv.get("SUPABASE_DB_URL"));
        System.setProperty("SUPABASE_DB_USER", dotenv.get("SUPABASE_DB_USER"));
        System.setProperty("SUPABASE_DB_PASSWORD", dotenv.get("SUPABASE_DB_PASSWORD"));
                System.out.println("SUPABASE_DB_URL: " + System.getProperty("SUPABASE_DB_URL"));
		SpringApplication.run(WebfarmaciaApplication.class, args);


	}

}
