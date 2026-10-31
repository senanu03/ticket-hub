package com.tickethub.payment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@org.springframework.kafka.test.context.EmbeddedKafka(partitions = 1, count = 1)
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
