package com.bank.card_ops_producer.api;

import com.bank.card_ops_producer.domain.service.EventService;
import com.bank.card_ops_producer.api.dto.CardReplacementRequestDto;
import io.reactivex.rxjava3.core.Single;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardReplacementControllerTest {

    @Mock
    private EventService service;

    @InjectMocks
    private CardReplacementController controller;

    @Test
    void create_ShouldReturnAccepted_WhenServiceSucceeds() {
        // GIVEN
        CardReplacementRequestDto dto = new CardReplacementRequestDto();
        // (Opcional) Si tu DTO tiene @NotNull, el mockito puro no valida, así que
        // funcionará igual. Si usas MockMvc sí validaría.

        String generatedId = "req-12345";

        // Simulamos que el servicio procesa el evento y devuelve un ID
        when(service.process(any())).thenReturn(Single.just(generatedId));

        // WHEN & THEN
        controller.create(dto)
                .test() // Nos suscribimos al Single
                .assertComplete() // Verificamos que termine bien
                .assertNoErrors()
                .assertValue(response ->
                        // Verificamos el código HTTP 202 ACCEPTED
                        response.getStatusCode() == HttpStatus.ACCEPTED &&
                                // Verificamos que el cuerpo contenga el ID
                                response.getBody() != null &&
                                response.getBody().contains(generatedId)
                );
    }
}