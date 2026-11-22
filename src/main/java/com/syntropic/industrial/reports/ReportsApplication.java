package com.syntropic.industrial.reports;

import com.syntropic.industrial.reports.launcher.DesktopApplication;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;


@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class
})
public class ReportsApplication {

	public static void main(String[] args) {

		// Prevents crashes on servers/VMs missing GPU display drivers
		System.setProperty("prism.order", "sw");
		System.setProperty("prism.verbose", "false");

		Application.launch(DesktopApplication.class, args);
//		SpringApplication.run(ReportsApplication.class, args);

	}

}
