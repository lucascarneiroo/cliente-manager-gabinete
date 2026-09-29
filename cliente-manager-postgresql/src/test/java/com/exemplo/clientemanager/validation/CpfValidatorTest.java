package com.exemplo.clientemanager.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidatorTest {

    private final CpfValidator validator = new CpfValidator();

    @Test
    void aceitaCpfValidoSemPontuacao() {
        assertTrue(validator.isValid("11144477735", null));
    }

    @Test
    void aceitaCpfValidoComPontuacao() {
        assertTrue(validator.isValid("111.444.777-35", null));
    }

    @Test
    void rejeitaCpfComDigitosVerificadoresIncorretos() {
        assertFalse(validator.isValid("11144477736", null));
    }

    @Test
    void rejeitaCpfComTodosDigitosIguais() {
        assertFalse(validator.isValid("11111111111", null));
    }

    @Test
    void rejeitaCpfComTamanhoInvalido() {
        assertFalse(validator.isValid("123", null));
    }

    @Test
    void rejeitaCpfNulo() {
        assertFalse(validator.isValid(null, null));
    }
}
