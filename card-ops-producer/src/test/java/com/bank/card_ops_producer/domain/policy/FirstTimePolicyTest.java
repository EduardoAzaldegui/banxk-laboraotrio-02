package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirstTimePolicyTest {

    private final FirstTimePolicy policy = new FirstTimePolicy();

    @Test
    void resolveAttempt_ShouldReturnOne() {
        // ACT
        TestObserver<Integer> observer = policy.resolveAttempt(new CardReplacementRequestDto()).test();

        // ASSERT
        observer.assertComplete();
        observer.assertValue(1); // Debe devolver 1
    }

    @Test
    void name_ShouldReturnCorrectName() {
        assertEquals("FirstTimePolicy", policy.name());
    }
}