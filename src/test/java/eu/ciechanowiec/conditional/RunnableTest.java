package eu.ciechanowiec.conditional;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RunnableTest {

    @Spy
    private List<String> list;

    @SuppressWarnings("ChainedMethodCall")
    @Test
    void mustRunWithoutThrowing() {
        Runnable runnable = list::clear;
        assertAll(
                () -> verify(list, never()).clear(),
                () -> assertDoesNotThrow(runnable::run),
                () -> verify(list, times(NumberUtils.INTEGER_ONE)).clear()
        );
    }

    @Test
    void mustRunWithThrowing() {
        ActiveCheckedThrower thrower = new ActiveCheckedThrower();
        Runnable runnable = () -> {
            list.clear();
            thrower.throwCheckedIfActive(new Exception());
        };
        assertAll(
                () -> verify(list, never()).clear(),
                () -> assertThrows(Exception.class, runnable::run),
                () -> verify(list, times(NumberUtils.INTEGER_ONE)).clear()
        );
    }
}
