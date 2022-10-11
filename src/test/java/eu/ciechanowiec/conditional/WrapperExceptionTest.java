package eu.ciechanowiec.conditional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WrapperExceptionTest {

    @Test
    void getMessage() {
        Exception wrappedException = new RuntimeException(RandomStringUtils.random(10));
        Class<? extends Exception> wrappedExceptionClass = wrappedException.getClass();
        String expectedWrappedExceptionName = wrappedExceptionClass.getName();
        String expectedWrappedExceptionMessage = wrappedException.getMessage();
        WrapperException wrapperException = new WrapperException(wrappedException);
        final String message = wrapperException.getMessage();
        assertAll(
                () -> assertTrue(message.contains(expectedWrappedExceptionName)),
                () -> assertTrue(message.contains(expectedWrappedExceptionMessage))
        );
    }
}
