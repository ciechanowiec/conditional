package eu.ciechanowiec.conditional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionThrowerTest {

    @Test
    void mustThrowCheckedOnlyIfActive() {
        Exception exception = new Exception("Generic exception for tests");
        ChildException childException = new ChildException("Generic child exception for tests");
        ExceptionThrower activeThrower = new ExceptionThrowerActive();
        ExceptionThrower voidThrower = new ExceptionThrowerVoid();
        assertAll(
                () -> assertThrows(Exception.class, () -> activeThrower.throwIfActive(exception)),
                () -> assertThrows(Exception.class, () -> activeThrower.throwIfActive(childException)),
                () -> assertDoesNotThrow(() -> voidThrower.throwIfActive(exception)),
                () -> assertDoesNotThrow(() -> voidThrower.throwIfActive(childException))
        );
    }

    @Test
    void mustThrowUncheckedOnlyIfActive() {
        RuntimeException exception = new RuntimeException("Generic exception for tests");
        ChildRuntimeException childException = new ChildRuntimeException("Generic child exception for tests");
        ExceptionThrower activeThrower = new ExceptionThrowerActive();
        ExceptionThrower voidThrower = new ExceptionThrowerVoid();
        assertAll(
                () -> assertThrows(RuntimeException.class, () -> activeThrower.throwIfActive(exception)),
                () -> assertThrows(RuntimeException.class, () -> activeThrower.throwIfActive(childException)),
                () -> assertDoesNotThrow(() -> voidThrower.throwIfActive(exception)),
                () -> assertDoesNotThrow(() -> voidThrower.throwIfActive(childException))
        );
    }

    @Test
    void mustThrowUnsupportedOperation() {
        RuntimeException uncheckedException = new RuntimeException("Generic exception for tests");
        Exception checkedException = new Exception("Generic exception for tests");
        ExceptionThrower activeUncheckedThrower = new ExceptionThrower() {};
        ExceptionThrower voidUncheckedThrower = new ExceptionThrower() {};
        ExceptionThrower activeCheckedThrower = new ExceptionThrower() {};
        ExceptionThrower voidCheckedThrower = new ExceptionThrower() {};
        assertAll(
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> activeUncheckedThrower.throwIfActive(checkedException)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> voidUncheckedThrower.throwIfActive(checkedException)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> activeCheckedThrower.throwIfActive(uncheckedException)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> voidCheckedThrower.throwIfActive(uncheckedException))
        );
    }

    @Test
    void mustHandleNPEIfPassingNull() {
        ExceptionThrower activeUncheckedThrower = new ExceptionThrowerActive();
        ExceptionThrower voidUncheckedThrower = new ExceptionThrowerVoid();
        ExceptionThrower activeCheckedThrower = new ExceptionThrowerActive();
        ExceptionThrower voidCheckedThrower = new ExceptionThrowerVoid();
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> activeUncheckedThrower.throwIfActive(null)),
                () -> assertDoesNotThrow(
                        () -> voidUncheckedThrower.throwIfActive(null)),
                () -> assertThrows(NullPointerException.class,
                        () -> activeCheckedThrower.throwIfActive(null)),
                () -> assertDoesNotThrow(
                        () -> voidCheckedThrower.throwIfActive(null))
        );
    }
}
