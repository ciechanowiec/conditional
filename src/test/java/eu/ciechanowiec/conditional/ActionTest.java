package eu.ciechanowiec.conditional;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.xml.catalog.CatalogException;
import java.awt.*;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.math.BigDecimal;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.rmi.AlreadyBoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.prefs.BackingStoreException;
import java.util.stream.IntStream;

import static eu.ciechanowiec.conditional.Variables.EXCEPTION_TEST_MESSAGE;
import static eu.ciechanowiec.conditional.Variables.HELLO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ActionTest {

    @Mock
    private Runnable runnableMock;

    @Mock
    private Callable<?> callableMock;

    @Spy
    private ExceptionThrowerActive thrower;

//  <!-- ====================================================================== -->
//  <!--        EXECUTE                                                         -->
//  <!-- ====================================================================== -->

    @ParameterizedTest
    @ValueSource(ints = {1, 3})
    @SuppressWarnings("ChainedMethodCall")
    void mustExecuteSpecifiedAmountOfTimes(int timesToExecute) {
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
    void mustExecuteAndHaveSideEffects() {
        List<String> listForRunnable = new ArrayList<>(List.of("R1", "R2", "R3"));
        List<String> listForCallable = new ArrayList<>(List.of("C1", "C2", "C3"));
        Action<?> actionWithRunnable = new Action<>(listForRunnable::clear);
        Action<?> actionWithCallable = new Action<>(listForCallable::clear);
        assertAll(
                () -> assertFalse(listForRunnable.isEmpty()),
                () -> assertFalse(listForCallable.isEmpty())
        );
        actionWithRunnable.execute();
        actionWithCallable.execute();
        assertAll(
                () -> assertTrue(listForRunnable.isEmpty()),
                () -> assertTrue(listForCallable.isEmpty())
        );
    }

    @Test
    @SuppressWarnings({"ConstantValue", "DataFlowIssue"})
    void mustThrowNPEWhenPassedRunnableOrCallableIsNullAndWhenExecute() {
        Runnable runnable = null;
        Callable<?> callable = null;
        Action<?> actionWithRunnable = new Action<>(runnable);
        Action<?> actionWithCallable = new Action<>(callable);
        assertAll(
                () -> assertThrows(NullPointerException.class, actionWithRunnable::execute),
                () -> assertThrows(NullPointerException.class, actionWithCallable::execute)
        );
    }

    @Test
    void mustThrowExceptionFromPassedRunnableAndCallableWhenExecute() {
        Runnable runnableOne = () -> {
            throw new Exception(EXCEPTION_TEST_MESSAGE);
        };
        Runnable runnableTwo = () -> thrower.throwIfActive(new Exception(EXCEPTION_TEST_MESSAGE));
        Callable<?> callableOne = () -> {
            throw new Exception(EXCEPTION_TEST_MESSAGE);
        };
        Action<?> actionWithRunnableOne = new Action<>(runnableOne);
        Action<?> actionWithRunnableTwo = new Action<>(runnableTwo);
        Action<?> actionWithCallable = new Action<>(callableOne);
        assertAll(
                () -> assertThrows(Exception.class, actionWithRunnableOne::execute),
                () -> assertThrows(Exception.class, actionWithRunnableTwo::execute),
                () -> assertThrows(Exception.class, actionWithCallable::execute)
        );
    }

    @Test
    @SuppressWarnings("ChainedMethodCall")
    void mustExecuteWithExceptionsViaExecuteWithoutExceptions() {
        Runnable runnable = () -> System.out.println(HELLO);
        Callable<?> callable = () -> {
            System.out.println(HELLO);
            return HELLO;
        };
        Action<?> actionWithRunnable = spy(new Action<>(runnable));
        Action<?> actionWithCallable = spy(new Action<>(callable));
        actionWithRunnable.execute(RuntimeException.class);
        actionWithCallable.execute(RuntimeException.class);
        actionWithRunnable.execute(RuntimeException.class, RuntimeException.class);
        actionWithCallable.execute(RuntimeException.class, RuntimeException.class);
        actionWithRunnable.execute(RuntimeException.class, RuntimeException.class, RuntimeException.class);
        actionWithCallable.execute(RuntimeException.class, RuntimeException.class, RuntimeException.class);
        actionWithRunnable.execute(RuntimeException.class, RuntimeException.class,
                                   RuntimeException.class, RuntimeException.class);
        actionWithCallable.execute(RuntimeException.class, RuntimeException.class,
                                   RuntimeException.class, RuntimeException.class);
        verify(actionWithRunnable, times(4)).execute();
        verify(actionWithCallable, times(4)).execute();
    }

    @Test
    void mustEnforceHandlingOfPassedCheckedExceptionsWhenExecute() {
        Runnable runnable = () -> System.out.println(HELLO);
        Action<?> action = new Action<>(runnable);
        try {
            action.execute(AbsentInformationException.class);
            action.execute(AgentInitializationException.class, AgentLoadException.class);
            action.execute(AlreadyBoundException.class, AttachNotSupportedException.class, AWTException.class);
            action.execute(BackingStoreException.class, BadAttributeValueExpException.class,
                           BadBinaryOpValueExpException.class, BadLocationException.class);
        } catch (AbsentInformationException
                 | AgentInitializationException | AgentLoadException
                 | AlreadyBoundException | AttachNotSupportedException | AWTException
                 | BackingStoreException | BadAttributeValueExpException | BadBinaryOpValueExpException |
                 BadLocationException exception) {
            log.error("Exception occurred", exception);
            fail();
        }
    }

    @Test
    // Only the possibility to compile the statements in the test without handling
    // the passed exceptions is tested, so the following suppression is valid:
    @SuppressWarnings("squid:S2699")
    void mustNotEnforceHandlingOfPassedUncheckedExceptionsAndNullsWhenExecute() {
        Runnable runnable = () -> System.out.println(HELLO);
        Action<?> action = new Action<>(runnable);
        action.execute(AnnotationTypeMismatchException.class);
        action.execute(null);
        action.execute(ArithmeticException.class, ArrayStoreException.class);
        action.execute(null, null, null);
        action.execute(BufferOverflowException.class, BufferUnderflowException.class, CannotRedoException.class);
        action.execute(null, null, null);
        action.execute(CannotUndoException.class, CatalogException.class,
                       ClassCastException.class, ClassNotPreparedException.class);
        action.execute(null, null, null, null);
    }

//  <!-- ====================================================================== -->
//  <!--        GET                                                             -->
//  <!-- ====================================================================== -->

    @Test
    void mustGetNullWithRunnable() {
        Runnable runnable = () -> System.out.println(HELLO);
        Action<?> action = new Action<>(runnable);
        assertAll(
                () -> assertNull(action.get()),
                () -> assertNull(action.get(String.class)),
                () -> assertNull(action.get(Integer.class, RuntimeException.class)),
                () -> assertNull(action.get(Integer.class, RuntimeException.class, RuntimeException.class)),
                () -> assertNull(action.get(Integer.class, RuntimeException.class, RuntimeException.class,
                                                           RuntimeException.class)),
                () -> assertNull(action.get(Integer.class, RuntimeException.class, RuntimeException.class,
                                                           RuntimeException.class, RuntimeException.class))
        );
    }

    @Test
    @SuppressWarnings("ReturnOfNull")
    void mustGetPassedObjectWithCallable() {
        List<String> listToReturn = new ArrayList<>(List.of(HELLO));
        Callable<?> callableWithString = () -> HELLO;
        Callable<?> callableWithNull = () -> null;
        Callable<?> callableWithList = () -> listToReturn;
        Action<?> actionWithString = new Action<>(callableWithString);
        Action<?> actionWithNull = new Action<>(callableWithNull);
        Action<?> actionWithList = new Action<>(callableWithList);
        assertAll(
                () -> assertEquals(HELLO, actionWithString.get()),
                () -> assertEquals(HELLO, actionWithString.get(String.class)),
                () -> assertEquals(HELLO, actionWithString.get(String.class, RuntimeException.class)),
                () -> assertEquals(HELLO, actionWithString.get(String.class, RuntimeException.class, RuntimeException.class)),
                () -> assertEquals(HELLO, actionWithString.get(String.class, RuntimeException.class, RuntimeException.class,
                                                               RuntimeException.class)),
                () -> assertEquals(HELLO, actionWithString.get(String.class, RuntimeException.class, RuntimeException.class,
                                                               RuntimeException.class, RuntimeException.class)),
                () -> assertNull(actionWithNull.get()),
                () -> assertNull(actionWithNull.get(String.class)),
                () -> assertNull(actionWithNull.get(String.class, RuntimeException.class)),
                () -> assertNull(actionWithNull.get(String.class, RuntimeException.class, RuntimeException.class)),
                () -> assertNull(actionWithNull.get(String.class, RuntimeException.class, RuntimeException.class,
                                                    RuntimeException.class)),
                () -> assertNull(actionWithNull.get(String.class, RuntimeException.class, RuntimeException.class,
                                                    RuntimeException.class, RuntimeException.class)),
                () -> assertEquals(listToReturn, actionWithList.get()),
                () -> assertEquals(listToReturn, actionWithList.get(List.class)),
                () -> assertEquals(listToReturn, actionWithList.get(List.class, RuntimeException.class)),
                () -> assertEquals(listToReturn, actionWithList.get(List.class, RuntimeException.class, RuntimeException.class)),
                () -> assertEquals(listToReturn, actionWithList.get(List.class, RuntimeException.class, RuntimeException.class,
                                                                    RuntimeException.class)),
                () -> assertEquals(listToReturn, actionWithList.get(List.class, RuntimeException.class, RuntimeException.class,
                                                                    RuntimeException.class, RuntimeException.class))
        );
    }

    @Test
    @SuppressWarnings({"ConstantValue", "DataFlowIssue"})
    void mustThrowNPEWhenPassedRunnableOrCallableIsNullAndWhenGet() {
        Runnable runnable = null;
        Callable<?> callable = null;
        Action<?> actionWithRunnable = new Action<>(runnable);
        Action<?> actionWithCallable = new Action<>(callable);
        assertAll(
                () -> assertThrows(NullPointerException.class, actionWithRunnable::get),
                () -> assertThrows(NullPointerException.class, actionWithCallable::get)
        );
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void mustThrowNPEWhenPassedTypeIsNullAndWhenGet() {
        Runnable runnable = () -> System.out.println(HELLO);
        Callable<String> callable = () -> HELLO;
        Action<?> actionWithRunnable = new Action<>(runnable);
        Action<?> actionWithCallable = new Action<>(callable);
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> actionWithRunnable.get(null)),
                () -> assertThrows(NullPointerException.class, () -> actionWithCallable.get(null))
        );
    }

    @Test
    void mustThrowMismatchedReturnTypeExceptionWhenGetWithCallable() {
        Callable<?> callable = () -> HELLO;
        Action<?> action = new Action<>(callable);
        assertThrows(MismatchedReturnTypeException.class, () -> action.get(Integer.class));
    }

    @Test
    void mustThrowExceptionFromPassedRunnableAndCallableWhenGet() {
        Runnable runnableOne = () -> {
            throw new Exception(EXCEPTION_TEST_MESSAGE);
        };
        Runnable runnableTwo = () -> thrower.throwIfActive(new Exception(EXCEPTION_TEST_MESSAGE));
        Callable<?> callableOne = () -> {
            throw new Exception(EXCEPTION_TEST_MESSAGE);
        };
        Action<?> actionWithRunnableOne = new Action<>(runnableOne);
        Action<?> actionWithRunnableTwo = new Action<>(runnableTwo);
        Action<?> actionWithCallable = new Action<>(callableOne);
        assertAll(
                () -> assertThrows(Exception.class, actionWithRunnableOne::get),
                () -> assertThrows(Exception.class, () -> actionWithRunnableOne.get(BigDecimal.class)),
                () -> assertThrows(Exception.class, actionWithRunnableTwo::get),
                () -> assertThrows(Exception.class, () -> actionWithRunnableTwo.get(BigDecimal.class)),
                () -> assertThrows(Exception.class, actionWithCallable::get),
                () -> assertThrows(Exception.class, () -> actionWithCallable.get(BigDecimal.class))
        );
    }

    @Test
    @SuppressWarnings("ChainedMethodCall")
    void mustGetWithExceptionsViaGetWithoutExceptions() {
        Runnable runnable = () -> System.out.println(HELLO);
        Callable<?> callable = () -> {
            System.out.println(HELLO);
            return HELLO;
        };
        Action<?> actionWithRunnable = spy(new Action<>(runnable));
        Action<?> actionWithCallable = spy(new Action<>(callable));
        actionWithRunnable.get(String.class, RuntimeException.class);
        actionWithCallable.get(String.class, RuntimeException.class);
        actionWithRunnable.get(String.class, RuntimeException.class, RuntimeException.class);
        actionWithCallable.get(String.class, RuntimeException.class, RuntimeException.class);
        actionWithRunnable.get(String.class, RuntimeException.class, RuntimeException.class, RuntimeException.class);
        actionWithCallable.get(String.class, RuntimeException.class, RuntimeException.class, RuntimeException.class);
        actionWithRunnable.get(String.class, RuntimeException.class, RuntimeException.class,
                               RuntimeException.class, RuntimeException.class);
        actionWithCallable.get(String.class, RuntimeException.class, RuntimeException.class,
                               RuntimeException.class, RuntimeException.class);
        verify(actionWithRunnable, times(4)).get(String.class);
        verify(actionWithCallable, times(4)).get(String.class);
    }

    @Test
    void mustEnforceHandlingOfPassedCheckedExceptionsWhenGet() {
        Callable<String> callable = () -> HELLO;
        Action<?> action = new Action<>(callable);
        try {
            action.get(String.class, AbsentInformationException.class);
            action.get(String.class, AgentInitializationException.class, AgentLoadException.class);
            action.get(String.class, AlreadyBoundException.class, AttachNotSupportedException.class, AWTException.class);
            action.get(String.class, BackingStoreException.class, BadAttributeValueExpException.class,
                       BadBinaryOpValueExpException.class, BadLocationException.class);
        } catch (AbsentInformationException
                 | AgentInitializationException | AgentLoadException
                 | AlreadyBoundException | AttachNotSupportedException | AWTException
                 | BackingStoreException | BadAttributeValueExpException | BadBinaryOpValueExpException |
                 BadLocationException exception) {
            log.error("Exception occurred", exception);
            fail();
        }
    }

    @Test
    // Only the possibility to compile the statements in the test without handling
    // the passed exceptions is tested, so the following suppression is valid:
    @SuppressWarnings("squid:S2699")
    void mustNotEnforceHandlingOfPassedUncheckedExceptionsAndNullsWhenGet() {
        Runnable runnable = () -> System.out.println(HELLO);
        Action<?> action = new Action<>(runnable);
        action.get(String.class, AnnotationTypeMismatchException.class);
        action.get(String.class, null);
        action.get(String.class, ArithmeticException.class, ArrayStoreException.class);
        action.get(String.class, null, null, null);
        action.get(String.class, BufferOverflowException.class, BufferUnderflowException.class, CannotRedoException.class);
        action.get(String.class, null, null, null);
        action.get(String.class, CannotUndoException.class, CatalogException.class,
                   ClassCastException.class, ClassNotPreparedException.class);
        action.get(String.class, null, null, null, null);
    }
}
