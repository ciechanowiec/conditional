package eu.ciechanowiec.conditional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionThrowerTest {

    @Test
    void mustThrowCheckedOnlyIfActive() {
        Exception exception = new Exception("Generic exception for tests");
        ChildException childException = new ChildException("Generic child exception for tests");
        ExceptionThrower activeThrower = new ActiveCheckedThrower();
        ExceptionThrower voidThrower = new VoidCheckedThrower();
        assertAll(
                () -> assertThrows(Exception.class, () -> activeThrower.throwCheckedIfActive(exception)),
                () -> assertThrows(Exception.class, () -> activeThrower.throwCheckedIfActive(childException)),
                () -> assertDoesNotThrow(() -> voidThrower.throwCheckedIfActive(exception)),
                () -> assertDoesNotThrow(() -> voidThrower.throwCheckedIfActive(childException))
        );
    }

    @Test
    void mustThrowUncheckedOnlyIfActive() {
        RuntimeException exception = new RuntimeException("Generic exception for tests");
        ChildRuntimeException childException = new ChildRuntimeException("Generic child exception for tests");
        ExceptionThrower activeThrower = new ActiveUncheckedThrower();
        ExceptionThrower voidThrower = new VoidUncheckedThrower();
        assertAll(
                () -> assertThrows(RuntimeException.class, () -> activeThrower.throwUncheckedIfActive(exception)),
                () -> assertThrows(RuntimeException.class, () -> activeThrower.throwUncheckedIfActive(childException)),
                () -> assertDoesNotThrow(() -> voidThrower.throwUncheckedIfActive(exception)),
                () -> assertDoesNotThrow(() -> voidThrower.throwUncheckedIfActive(childException))
        );
    }

    @Test
    void mustThrowUnsupportedOperation() {
        RuntimeException uncheckedException = new RuntimeException("Generic exception for tests");
        Exception checkedException = new Exception("Generic exception for tests");
        ExceptionThrower activeUncheckedThrower = new ActiveUncheckedThrower();
        ExceptionThrower voidUncheckedThrower = new VoidUncheckedThrower();
        ExceptionThrower activeCheckedThrower = new ActiveCheckedThrower();
        ExceptionThrower voidCheckedThrower = new VoidCheckedThrower();
        assertAll(
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> activeUncheckedThrower.throwCheckedIfActive(checkedException)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> voidUncheckedThrower.throwCheckedIfActive(checkedException)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> activeCheckedThrower.throwUncheckedIfActive(uncheckedException)),
                () -> assertThrows(UnsupportedOperationException.class,
                        () -> voidCheckedThrower.throwUncheckedIfActive(uncheckedException))
        );
    }
}
