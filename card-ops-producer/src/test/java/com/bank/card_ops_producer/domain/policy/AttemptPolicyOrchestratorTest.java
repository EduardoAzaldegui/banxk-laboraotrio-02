package com.bank.card_ops_producer.domain.policy;

import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import com.bank.card_ops_producer.domain.port.AttemptStateRepository;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttemptPolicyOrchestratorTest {

    // 1. Mockeamos las dependencias (simulacros)
    @Mock
    private FirstTimePolicy first;

    @Mock
    private SecondTimePolicy second;

    @Mock
    private AttemptStateRepository repo;

    // 2. Inyectamos los mocks en la clase real
    @InjectMocks
    private AttemptPolicyOrchestrator orchestrator;

    @Test
    void resolveAttempt_WhenRequestExists_ShouldCallSecondPolicy() {
        // ARRANGE (Preparar datos)
        String requestId = "req-123";
        CardReplacementRequestDto dto = new CardReplacementRequestDto();
        dto.setRequestId(requestId);

        // Simulamos: El repositorio dice "SÍ existe" (true)
        when(repo.existsByRequestId(requestId)).thenReturn(Single.just(true));
        // Simulamos: La segunda política retorna intento 2
        when(second.resolveAttempt(dto)).thenReturn(Single.just(2));

        // ACT (Ejecutar)
        // .test() es clave en RxJava para suscribirse y ver resultados
        TestObserver<Integer> observer = orchestrator.resolveAttempt(dto).test();

        // ASSERT (Verificar)
        observer.assertComplete();      // Terminó bien
        observer.assertNoErrors();      // No lanzó excepción
        observer.assertValue(2);        // Retornó el valor esperado

        // Verificamos que NO llamó a la primera política
        verify(first, never()).resolveAttempt(any());
        // Verificamos que SÍ llamó a la segunda
        verify(second).resolveAttempt(dto);
    }

    @Test
    void resolveAttempt_WhenRequestDoesNotExist_ShouldSaveAndCallFirstPolicy() {
        // ARRANGE
        String requestId = "req-456";
        CardReplacementRequestDto dto = new CardReplacementRequestDto();
        dto.setRequestId(requestId);

        // Simulamos: El repositorio dice "NO existe" (false)
        when(repo.existsByRequestId(requestId)).thenReturn(Single.just(false));
        // Simulamos: El guardado es exitoso (true)
        when(repo.saveFirstAttempt(requestId)).thenReturn(Single.just(true));
        // Simulamos: La primera política retorna intento 1
        when(first.resolveAttempt(dto)).thenReturn(Single.just(1));

        // ACT
        TestObserver<Integer> observer = orchestrator.resolveAttempt(dto).test();

        // ASSERT
        observer.assertComplete();
        observer.assertValue(1);

        // Verificamos que primero intentó guardar
        verify(repo).saveFirstAttempt(requestId);
        // Verificamos que llamó a la primera política
        verify(first).resolveAttempt(dto);
        // Verificamos que NO llamó a la segunda
        verify(second, never()).resolveAttempt(any());
    }

    @Test
    void name_ShouldReturnOrchestrator() {
        // Test súper simple para cubrir la línea 30
        assertEquals("Orchestrator", orchestrator.name());
    }
}