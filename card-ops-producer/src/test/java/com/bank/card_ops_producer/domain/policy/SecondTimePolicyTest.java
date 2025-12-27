package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecondTimePolicyTest {

    private final SecondTimePolicy policy = new SecondTimePolicy();

    @Test
    void resolveAttempt_ShouldReturnTwo() {
        // ACT
        TestObserver<Integer> observer = policy.resolveAttempt(new CardReplacementRequestDto()).test();

        // ASSERT
        observer.assertComplete();
        observer.assertValue(2); // Debe devolver 2
    }

    @Test
    void name_ShouldReturnCorrectName() {
        assertEquals("SecondTimePolicy", policy.name());
    }
}