package com.swiftpay.gatewayservice;

import com.swiftpay.gatewayservice.entity.PaymentEntity;
import com.swiftpay.gatewayservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class PaymentRepositoryIntegrationTest {

    @BeforeAll
    static void setupTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18")
                    .withDatabaseName("swiftpay")
                    .withUsername("swiftpay")
                    .withPassword("swiftpay");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void shouldSavePaymentInPostgresContainer() {

        PaymentEntity payment = PaymentEntity.builder()
                .transactionId("txn-test-1")
                .senderId("u1")
                .receiverId("u2")
                .amount(BigDecimal.valueOf(100))
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        assertThat(saved.getTransactionId()).isNotNull();
        assertThat(saved.getTransactionId()).isEqualTo("txn-test-1");
        assertThat(saved.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(saved.getCurrency()).isEqualTo("INR");
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }
}
