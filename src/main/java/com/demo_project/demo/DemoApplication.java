package com.demo_project.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Scanner;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private WebClient webClient;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if ("test".equals(activeProfile)) {
            System.out.println("Skipping startup logic in test profile...");
            return;
        }

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter RegNo: ");
        String regNo = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        GenerateWebhookRequest request = new GenerateWebhookRequest(name, regNo, email);

        WebhookResponse response = webClient.post()
                .uri("/hiring/generateWebhook/JAVA")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), GenerateWebhookRequest.class)
                .retrieve()
                .bodyToMono(WebhookResponse.class)
                .block();

        assert response != null;
        String webhookUrl = response.getWebhookUrl();
        String jwtToken = response.getAccessToken();

        System.out.println("\n✅ Webhook URL: " + webhookUrl);
        System.out.println("✅ AccessToken: " + jwtToken);

        int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));
        if (lastTwoDigits % 2 == 1) {
            System.out.println("👉 Your question (ref Question 1): https://drive.google.com/file/d/1IeSI6l6KoS_QAFfRihIT9tEDICtoz-G_/view?usp=sharing");
        } else {
            System.out.println("👉 Your question (ref Question 2): https://drive.google.com/file/d/143MR5cLFrlNEuHzzWJ5RHnEW_uijuM9X/view?usp=sharing");
        }


        System.out.println("\nEnter your final SQL query (paste in one line):");
        String finalQuery = sc.nextLine();

        SolutionRequest solution = new SolutionRequest(finalQuery);
        String result = webClient.post()
                .uri("/hiring/testWebhook/JAVA")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(solution), SolutionRequest.class)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.println("\n📩 Response from webhook: " + result);
    }
}
