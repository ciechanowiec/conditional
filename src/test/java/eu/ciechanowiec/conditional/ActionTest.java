package eu.ciechanowiec.conditional;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import static eu.ciechanowiec.conditional.Variables.EXCEPTION_TEST_MESSAGE;
import static eu.ciechanowiec.conditional.Variables.HELLO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ActionTest {

    @Mock
    private Runnable runnableMock;

    @Mock
    private Callable<?> callableMock;

    @Spy
    private ActiveCheckedThrower thrower;

    @ParameterizedTest
    @ValueSource(ints = {1, 3})
    void mustExecuteWithRunnableAndCallable(int timesToExecute) {
        Action<?> actionWithRunnable = new Action<>(runnableMock);
        Action<?> actionWithCallable = new Action<>(callableMock);
        IntStream.range(NumberUtils.INTEGER_ZERO, timesToExecute)
                 .forEach(index -> {
                     actionWithRunnable.execute();
                     actionWithCallable.execute();
                 });
        assertAll(
                () -> verify(runnableMock, times(timesToExecute)).run(),
                () -> verify(callableMock, times(timesToExecute)).call()
        );
    }

    @Test
    void mustThrowWrapperExceptionWhenExecuteWithRunnableAndCallable() {
        Runnable runnableOne = () -> {
            throw new Exception(EXCEPTION_TEST_MESSAGE);
        };
        Runnable runnableTwo = () -> thrower.throwCheckedIfActive(new Exception(EXCEPTION_TEST_MESSAGE));
        Callable<?> callableOne = () -> {
            throw new Exception(EXCEPTION_TEST_MESSAGE);
        };
        Action<?> actionWithRunnableOne = new Action<>(runnableOne);
        Action<?> actionWithRunnableTwo = new Action<>(runnableTwo);
        Action<?> actionWithCallable = new Action<>(callableOne);
        assertAll(
            () -> assertThrows(WrapperException.class, actionWithRunnableOne::execute),
            () -> assertThrows(WrapperException.class, actionWithRunnableTwo::execute),
            () -> assertThrows(WrapperException.class, actionWithCallable::execute)
        );
    }

    @Test
    void mustNestExceptionWhenExecuteWithRunnableAndCallable() {
        Exception nestedExceptionForRunnable = new Exception(EXCEPTION_TEST_MESSAGE);
        Exception nestedExceptionForCallable = new Exception(EXCEPTION_TEST_MESSAGE);
        Runnable runnable = () -> {
            throw nestedExceptionForRunnable;
        };
        Callable<?> callable = () -> {
            throw nestedExceptionForCallable;
        };
        Action<?> actionWithRunnable = new Action<>(runnable);
        Action<?> actionWithCallable = new Action<>(callable);

        try {
            actionWithRunnable.execute();
        } catch (WrapperException exception) {
            Throwable causeException = exception.getCause();
            assertEquals(nestedExceptionForRunnable, causeException);
        }

        try {
            actionWithCallable.execute();
        } catch (WrapperException exception) {
            Throwable causeException = exception.getCause();
            assertEquals(nestedExceptionForCallable, causeException);
        }
    }

    @Test
    void mustGetNullWithRunnable() {
        Runnable runnable = () -> System.out.println(HELLO);
        Action<?> action = new Action<>(runnable);
        assertAll(
                () -> assertNull(action.get(String.class)),
                () -> assertNull(action.get(Integer.class)),
                () -> assertNull(action.get())
        );
    }

    @SuppressWarnings("ReturnOfNull")
    @Test
    void mustGetPassedObjectWithCallable() {
        List<String> listToReturn = new ArrayList<>(List.of("Ola"));
        Callable<?> callableWithString = () -> HELLO;
        Callable<?> callableWithNull = () -> null;
        Callable<?> callableWithList = () -> listToReturn;
        Action<?> actionWithString = new Action<>(callableWithString);
        Action<?> actionWithNull = new Action<>(callableWithNull);
        Action<?> actionWithList = new Action<>(callableWithList);
        assertAll(
                () -> assertEquals(HELLO, actionWithString.get(String.class)),
                () -> assertEquals(HELLO, actionWithString.get()),
                () -> assertNull(actionWithNull.get(String.class)),
                () -> assertNull(actionWithNull.get()),
                () -> assertEquals(listToReturn, actionWithList.get(List.class)),
                () -> assertEquals(listToReturn, actionWithList.get())
        );
    }

    @Test
    void mustThrowMismatchedReturnTypeExceptionWhenGetWithCallable() {
        Callable<?> callable = () -> HELLO;
        Action<?> action = new Action<>(callable);
        assertThrows(MismatchedReturnTypeException.class, () -> action.get(Integer.class));
    }

    @Test
    void mustThrowWrapperExceptionWhenGetWithRunnableAndCallable() {
        Runnable runnableOne = () -> {
            throw new Exception(EXCEPTION_TEST_MESSAGE);
        };
        Runnable runnableTwo = () -> thrower.throwCheckedIfActive(new Exception(EXCEPTION_TEST_MESSAGE));
        Callable<?> callable = () -> {
            throw new Exception(EXCEPTION_TEST_MESSAGE);
        };
        Action<?> actionWithRunnableOne = new Action<>(runnableOne);
        Action<?> actionWithRunnableTwo = new Action<>(runnableTwo);
        Action<?> actionWithCallable = new Action<>(callable);
        assertAll(
                () -> assertThrows(WrapperException.class, () -> actionWithRunnableOne.get(String.class)),
                () -> assertThrows(WrapperException.class, () -> actionWithRunnableTwo.get(String.class)),
                () -> assertThrows(WrapperException.class, () -> actionWithCallable.get(String.class)),
                () -> assertThrows(WrapperException.class, actionWithCallable::get),
                () -> assertThrows(WrapperException.class, actionWithCallable::get)
        );
    }

    @Test
    void mustNestExceptionWhenGetWithRunnableAndCallable() {
        Exception nestedExceptionForRunnable = new Exception(EXCEPTION_TEST_MESSAGE);
        Exception nestedExceptionForCallable = new Exception(EXCEPTION_TEST_MESSAGE);
        Runnable runnable = () -> {
            throw nestedExceptionForRunnable;
        };
        Callable<?> callable = () -> {
            throw nestedExceptionForCallable;
        };
        Action<?> actionWithRunnable = new Action<>(runnable);
        Action<?> actionWithCallable = new Action<>(callable);

        try {
            actionWithRunnable.get(String.class);
        } catch (WrapperException exception) {
            Throwable causeException = exception.getCause();
            assertEquals(nestedExceptionForRunnable, causeException);
        }

        try {
            actionWithCallable.get(String.class);
        } catch (WrapperException exception) {
            Throwable causeException = exception.getCause();
            assertEquals(nestedExceptionForCallable, causeException);
        }
    }
}
