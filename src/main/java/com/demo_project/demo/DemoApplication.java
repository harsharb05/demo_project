package com.demo_project.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    private final WebClient webClient;

    public DemoApplication(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://bfhldevapigw.healthrx.co.in").build();
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // Step 1: Generate webhook
        GenerateWebhookRequest request = new GenerateWebhookRequest(
                "John Doe",      // 🔹 Replace with your name
                "REG12347",      // 🔹 Replace with your regNo
                "john@example.com" // 🔹 Replace with your email
        );

        WebhookResponse response = webClient.post()
                .uri("/hiring/generateWebhook/JAVA")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), GenerateWebhookRequest.class)
                .retrieve()
                .bodyToMono(WebhookResponse.class)
                .block();

        if (response == null) {
            System.out.println("❌ Failed to generate webhook");
            return;
        }

        String webhookUrl = response.getWebhookUrl();
        String jwtToken = response.getAccessToken();
        String regNo = request.getRegNo();

        System.out.println("✅ Webhook URL: " + webhookUrl);
        System.out.println("✅ AccessToken: " + jwtToken);

        // Step 2: Decide Question (odd/even last 2 digits of regNo)
        int lastTwoDigits = Integer.parseInt(regNo.substring(regNo.length() - 2));
        String finalQuery = (lastTwoDigits % 2 == 1) ? solveQuestion1() : solveQuestion2();

        // Step 3: Submit solution
        SolutionRequest solution = new SolutionRequest(finalQuery);
        String result = webClient.post()
                .uri("/hiring/testWebhook/JAVA")
                .header("Authorization", jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(solution), SolutionRequest.class)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("✅ Response from webhook: " + result);
    }

    // ✅ SQL for Question 1
    private String solveQuestion1() {
        return "SELECT p.AMOUNT AS SALARY, " +
                "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                "d.DEPARTMENT_NAME " +
                "FROM PAYMENTS p " +
                "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
                "ORDER BY p.AMOUNT DESC LIMIT 1;";
    }

    // ✅ SQL for Question 2
    private String solveQuestion2() {
        return "SELECT e1.EMP_ID, " +
                "e1.FIRST_NAME, " +
                "e1.LAST_NAME, " +
                "d.DEPARTMENT_NAME, " +
                "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                "FROM EMPLOYEE e1 " +
                "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                "LEFT JOIN EMPLOYEE e2 " +
                "ON e1.DEPARTMENT = e2.DEPARTMENT " +
                "AND e2.DOB > e1.DOB " +
                "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                "ORDER BY e1.EMP_ID DESC;";
    }
}
