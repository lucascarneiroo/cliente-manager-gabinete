package com.exemplo.clientemanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Valida CPF verificando os digitos verificadores (algoritmo oficial da Receita Federal),
 * nao apenas o formato. Aceita CPF com ou sem pontuacao (000.000.000-00 ou 00000000000).
 */
public class CpfValidator implements ConstraintValidator<CpfValido, String> {

    @Override
    public boolean isValid(String cpfOriginal, ConstraintValidatorContext context) {
        if (cpfOriginal == null) {
            return false;
        }

        String cpf = cpfOriginal.replaceAll("[^0-9]", "");

        if (cpf.length() != 11) {
            return false;
        }

        // Rejeita CPFs com todos os digitos iguais (ex: 111.111.111-11), que passariam
        // no calculo matematico mas nao sao validos na pratica.
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int primeiroDigito = 11 - (soma % 11);
            if (primeiroDigito >= 10) primeiroDigito = 0;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int segundoDigito = 11 - (soma % 11);
            if (segundoDigito >= 10) segundoDigito = 0;

            return primeiroDigito == Character.getNumericValue(cpf.charAt(9))
                    && segundoDigito == Character.getNumericValue(cpf.charAt(10));
        } catch (Exception e) {
            return false;
        }
    }
}
