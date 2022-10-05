package eu.ciechanowiec.conditional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MismatchedReturnTypeExceptionTest {

    @Test
    void mustGetMessage() {
        String randomStringOne = RandomStringUtils.random(10);
        String randomStringTwo = RandomStringUtils.random(10);
        MismatchedReturnTypeException exception = new MismatchedReturnTypeException(randomStringOne, randomStringTwo);
        String message = exception.getMessage();
        assertTrue(message.contains(randomStringOne) && message.contains(randomStringTwo));
    }
}
