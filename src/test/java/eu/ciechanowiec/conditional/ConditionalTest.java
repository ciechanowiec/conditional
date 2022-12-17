package eu.ciechanowiec.conditional;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.xml.catalog.CatalogException;
import java.awt.*;
import java.io.IOException;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.reflect.Field;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.rmi.AlreadyBoundException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.prefs.BackingStoreException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static eu.ciechanowiec.conditional.Conditional.*;
import static eu.ciechanowiec.conditional.Variables.EXCEPTION_TEST_MESSAGE;
import static eu.ciechanowiec.conditional.Variables.HELLO;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@Slf4j
@SuppressWarnings({"ChainedMethodCall", "ClassWithTooManyMethods"})
class ConditionalTest {

//  <!-- ====================================================================== -->
//  <!--        CREATION                                                        -->
//  <!-- ====================================================================== -->

    @ParameterizedTest
    @MethodSource("generateBooleans")
    void mustCreateSpecifiedConditional(boolean expectedValue) {
        Conditional conditional = conditional(expectedValue);
        assertNotNull(conditional);
        boolean actualValue = conditional.describedValue();
        assertEquals(expectedValue, actualValue);
    }

//  <!-- ====================================================================== -->
//  <!--        RETRIEVING DATA                                                 -->
//  <!-- ====================================================================== -->

    @Test
    void testIsFalseOrTrueForTrue() {
        Conditional conditional = conditional(TRUE);
        boolean isActualValueTrue = conditional.isTrue();
        boolean isActualValueFalse = conditional.isFalse();
        assertAll(
                () -> assertTrue(isActualValueTrue),
                () -> assertFalse(isActualValueFalse)
        );
    }

    @Test
    void testIsFalseOrTrueForFalse() {
        Conditional conditional = conditional(FALSE);
        boolean isActualValueTrue = conditional.isTrue();
        boolean isActualValueFalse = conditional.isFalse();
        assertAll(
                () -> assertFalse(isActualValueTrue),
                () -> assertTrue(isActualValueFalse)
        );
    }

    @Test
    void mustReturnActionsOnTrueAndFalse() {
        Conditional conditional = conditional(TRUE);
        ActionsList expectedActionsOnTrue = extractActionsViaReflection(conditional, TRUE);
        ActionsList actualActionsOnTrue = conditional.actionsOnTrue();
        ActionsList expectedActionsOnFalse = extractActionsViaReflection(conditional, FALSE);
        ActionsList actualActionsOnFalse = conditional.actionsOnFalse();
        assertAll(
                () -> assertEquals(expectedActionsOnTrue, actualActionsOnTrue),
                () -> assertEquals(expectedActionsOnFalse, actualActionsOnFalse),
                () -> assertNotEquals(actualActionsOnTrue, actualActionsOnFalse)
        );
    }

//  <!-- ====================================================================== -->
//  <!--        ACTIONS SUBMISSION                                              -->
//  <!-- ====================================================================== -->

    @Test
    void mustSubmitActionOnTrueAndFalseFromCallable() {
        // Callable cannot be spied, so testing via side effects
        // given
        List<String> listForTrue = spy(new ArrayList<>());
        Callable<String> callableForTrue = () -> {
            listForTrue.clear();
            return HELLO;
        };

        List<String> listForFalse = spy(new ArrayList<>());
        Callable<String> callableForFalse = () -> {
            listForFalse.clear();
            return HELLO;
        };

        // when
        conditional(TRUE)
                .onTrue(callableForTrue)
                .onFalse(callableForFalse)
                .execute(NumberUtils.INTEGER_TWO);
        conditional(FALSE)
                .onTrue(callableForTrue)
                .onFalse(callableForFalse)
                .execute(NumberUtils.INTEGER_ONE);

        // then
        assertAll(
                () -> verify(listForTrue, times(NumberUtils.INTEGER_TWO)).clear(),
                () -> verify(listForFalse, times(NumberUtils.INTEGER_ONE)).clear()
        );
    }

    @Test
    void mustSubmitActionOnTrueAndFalseFromRunnable() {
        // Runnable cannot be spied, so testing via side effects
        // given
        List<String> listForTrue = spy(new ArrayList<>());
        Runnable runnableForTrue = listForTrue::clear;

        List<String> listForFalse = spy(new ArrayList<>());
        Runnable runnableForFalse = listForFalse::clear;

        // when
        conditional(TRUE)
                .onTrue(runnableForTrue)
                .onFalse(runnableForFalse)
                .execute(NumberUtils.INTEGER_TWO);
        conditional(FALSE)
                .onTrue(runnableForTrue)
                .onFalse(runnableForFalse)
                .execute(NumberUtils.INTEGER_ONE);

        // then
        assertAll(
                () -> verify(listForTrue, times(NumberUtils.INTEGER_TWO)).clear(),
                () -> verify(listForFalse, times(NumberUtils.INTEGER_ONE)).clear()
        );
    }

    @ParameterizedTest
    @MethodSource("generateActionsPair")
    void mustSubmitActionOnTrueAndFalseFromAction(Iterable<Action<?>> actionsOnTrue,
                                                  Iterable<Action<?>> actionsOnFalse) {
        // given
        Conditional conditionalTrue = conditional(TRUE);
        actionsOnTrue.forEach(conditionalTrue::onTrue);
        actionsOnFalse.forEach(conditionalTrue::onFalse);

        Conditional conditionalFalse = conditional(FALSE);
        actionsOnTrue.forEach(conditionalFalse::onTrue);
        actionsOnFalse.forEach(conditionalFalse::onFalse);

        // when
        conditionalTrue.execute(NumberUtils.INTEGER_TWO);
        conditionalFalse.execute(NumberUtils.INTEGER_ONE);

        // then
        assertAll(
                () -> actionsOnTrue.forEach(action ->
                        verify(action, times(NumberUtils.INTEGER_TWO)).execute()),
                () -> actionsOnFalse.forEach(action ->
                        verify(action, times(NumberUtils.INTEGER_ONE)).execute())
        );
    }

    @SuppressWarnings({"ConstantValue", "DataFlowIssue"})
    @Test
    void mustThrowNPEWhenSubmittingNulls() {
        Callable<?> callable = null;
        Runnable runnable = null;
        Action<?> action = null;
        Conditional trueWithCallable = conditional(TRUE)
                .onTrue(callable);
        Conditional trueWithRunnable = conditional(TRUE)
                .onTrue(runnable);
        Conditional trueWithAction = conditional(TRUE)
                .onTrue(action);
        Conditional falseWithCallable = conditional(FALSE)
                .onFalse(callable);
        Conditional falseWithRunnable = conditional(FALSE)
                .onFalse(runnable);
        Conditional falseWithAction = conditional(FALSE)
                .onFalse(action);
        assertAll(
                () -> assertThrows(NullPointerException.class, trueWithCallable::execute),
                () -> assertThrows(NullPointerException.class, trueWithRunnable::execute),
                () -> assertThrows(NullPointerException.class, trueWithAction::execute),
                () -> assertThrows(NullPointerException.class, falseWithCallable::execute),
                () -> assertThrows(NullPointerException.class, falseWithRunnable::execute),
                () -> assertThrows(NullPointerException.class, falseWithAction::execute)
        );
    }

//  <!-- ====================================================================== -->
//  <!--        EXECUTION OPERATIONS - USUAL                                    -->
//  <!-- ====================================================================== -->

    @ParameterizedTest
    @MethodSource("generateActionsPair")
    void mustExecuteOnceOnTrue(Iterable<Action<?>> actionsOnTrue,
                               Iterable<Action<?>> actionsOnFalse) {
        // given
        Conditional conditional = conditional(TRUE);
        actionsOnTrue.forEach(conditional::onTrue);
        actionsOnFalse.forEach(conditional::onFalse);

        // when
        conditional.execute();

        // then
        assertAll(
                () -> actionsOnTrue.forEach(action ->
                        verify(action, times(NumberUtils.INTEGER_ONE)).execute()),
                () -> actionsOnFalse.forEach(action ->
                        verify(action, never()).execute())
        );
    }

    @ParameterizedTest
    @MethodSource("generateActionsPair")
    void mustExecuteChainedOnTrue(Iterable<Action<?>> actionsOnTrue,
                                  Iterable<Action<?>> actionsOnFalse) {
        // given
        Conditional conditional = conditional(TRUE);
        actionsOnTrue.forEach(conditional::onTrue);
        actionsOnFalse.forEach(conditional::onFalse);

        // when
        conditional.execute().execute();

        // then
        assertAll(
                () -> actionsOnTrue.forEach(action ->
                        verify(action, times(NumberUtils.INTEGER_TWO)).execute()),
                () -> actionsOnFalse.forEach(action ->
                        verify(action, never()).execute())
        );
    }

    @ParameterizedTest
    @MethodSource("generateActionsPair")
    void mustExecuteOnceOnFalse(Iterable<Action<?>> actionsOnTrue,
                                Iterable<Action<?>> actionsOnFalse) {
        // given
        Conditional conditional = conditional(FALSE);
        actionsOnTrue.forEach(conditional::onTrue);
        actionsOnFalse.forEach(conditional::onFalse);

        // when
        conditional.execute();

        // then
        assertAll(
                () -> actionsOnTrue.forEach(action ->
                        verify(action, never()).execute()),
                () -> actionsOnFalse.forEach(action ->
                        verify(action, times(NumberUtils.INTEGER_ONE)).execute())
        );
    }

    @Test
    void mustExecuteSubsequently() {
        List<String> expectedValuesWithoutCycles = List.of("A", "B", "C");
        List<String> actualValuesWithoutCycles = new ArrayList<>();
        conditional(TRUE)
                .onTrue(() -> actualValuesWithoutCycles.add("A"))
                .onTrue(() -> actualValuesWithoutCycles.add("B"))
                .onTrue(() -> actualValuesWithoutCycles.add("C"))
                .execute();
        assertEquals(actualValuesWithoutCycles, expectedValuesWithoutCycles);

        List<String> expectedValuesWithCycles = List.of("A", "B", "C", "A", "B", "C");
        List<String> actualValuesWithCycles = new ArrayList<>();
        conditional(FALSE)
                .onFalse(() -> actualValuesWithCycles.add("A"))
                .onFalse(() -> actualValuesWithCycles.add("B"))
                .onFalse(() -> actualValuesWithCycles.add("C"))
                .execute(2);
        assertEquals(actualValuesWithCycles, expectedValuesWithCycles);
    }

    @ParameterizedTest
    @MethodSource("generateActionsPair")
    void mustExecuteTwoCyclesOnTrue(Iterable<Action<?>> actionsOnTrue,
                                    Iterable<Action<?>> actionsOnFalse) {
        // given
        Conditional conditional = conditional(TRUE);
        actionsOnTrue.forEach(conditional::onTrue);
        actionsOnFalse.forEach(conditional::onFalse);

        // when
        conditional.execute(NumberUtils.INTEGER_TWO);

        // then
        assertAll(
                () -> actionsOnTrue.forEach(action ->
                        verify(action, times(NumberUtils.INTEGER_TWO)).execute()),
                () -> actionsOnFalse.forEach(action ->
                        verify(action, never()).execute())
        );
    }

    @ParameterizedTest
    @MethodSource("generateActionsPair")
    void mustExecuteTwoCyclesOnFalse(Iterable<Action<?>> actionsOnTrue,
                                     Iterable<Action<?>> actionsOnFalse) {
        // given
        Conditional conditional = conditional(FALSE);
        actionsOnTrue.forEach(conditional::onTrue);
        actionsOnFalse.forEach(conditional::onFalse);

        // when
        conditional.execute(NumberUtils.INTEGER_TWO);

        // then
        assertAll(
                () -> actionsOnTrue.forEach(action ->
                        verify(action, never()).execute()),
                () -> actionsOnFalse.forEach(action ->
                        verify(action, times(NumberUtils.INTEGER_TWO)).execute())
        );
    }

    @ParameterizedTest
    @MethodSource("generateActionsPair")
    void mustNotExecuteOnZeroOrLessCycles(Iterable<Action<?>> actionsOnTrue,
                                          Iterable<Action<?>> actionsOnFalse) {
        // given
        Conditional conditionalTrue = conditional(TRUE);
        Conditional conditionalFalse = conditional(FALSE);
        actionsOnTrue.forEach(conditionalTrue::onTrue);
        actionsOnFalse.forEach(conditionalFalse::onFalse);

        // when
        conditionalTrue.execute(NumberUtils.INTEGER_ZERO);
        conditionalFalse.execute(-NumberUtils.INTEGER_ONE);

        // then
        assertAll(
                () -> actionsOnTrue.forEach(action ->
                        verify(action, never()).execute()),
                () -> actionsOnFalse.forEach(action ->
                        verify(action, never()).execute())
        );
    }

    @ParameterizedTest
    @MethodSource("generateTrueAndFalseConditionals")
    void mustThrowFromActionOnExecution(Conditional conditionalTrue, Conditional conditionalFalse) {
        RuntimeException uncheckedException = new RuntimeException();
        Exception checkedException = new Exception();
        conditionalTrue.onTrue(() -> {
            throw uncheckedException;
        });
        conditionalFalse.onFalse(() -> {
            throw checkedException;
        });

        try {
            conditionalTrue.execute();
            fail();
        } catch (RuntimeException actualException) {
            assertEquals(actualException, uncheckedException);
        }

        try {
            conditionalFalse.execute();
            fail();
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") Exception actualException) {
            assertEquals(actualException, checkedException);
        }
    }

    @ParameterizedTest
    @MethodSource("generateTrueAndFalseConditionals")
    void mustNotThrowFromActionOnExecution(Conditional conditionalTrue, Conditional conditionalFalse) {
        RuntimeException uncheckedException = new RuntimeException();
        Exception checkedException = new Exception();

        conditionalTrue.onFalse(() -> {
            throw uncheckedException;
        });
        conditionalFalse.onTrue(() -> {
            throw checkedException;
        });

        assertAll(
                () -> assertDoesNotThrow(() -> conditionalTrue.execute()),
                () -> assertDoesNotThrow(() -> conditionalFalse.execute())
        );
    }

    @ParameterizedTest
    @MethodSource("generateActionsPair")
    void mustExecuteWithExceptionsViaExecuteWithoutExceptions(
            Iterable<Action<?>> actionsOnTrue,
            Iterable<Action<?>> actionsOnFalse
    ) {
        // given
        Conditional conditional = spy(conditional(TRUE));
        actionsOnTrue.forEach(conditional::onTrue);
        actionsOnFalse.forEach(conditional::onFalse);

        // when
        conditional.execute(RuntimeException.class);
        conditional.execute(RuntimeException.class, RuntimeException.class);
        conditional.execute(RuntimeException.class, RuntimeException.class, RuntimeException.class);
        conditional.execute(RuntimeException.class, RuntimeException.class,
                            RuntimeException.class, RuntimeException.class);

        // then
        assertAll(
                () -> verify(conditional, times(4)).execute(),
                () -> actionsOnTrue.forEach(action ->
                        verify(action, times(4)).execute()),
                () -> actionsOnFalse.forEach(action ->
                        verify(action, never()).execute())
        );
    }

    @Test
    void mustEnforceHandlingOfPassedCheckedExceptionsWhenExecute() {
        Conditional conditional = conditional(TRUE)
                                    .onTrue(() -> System.out.println(HELLO));
        try {
            conditional.execute(AbsentInformationException.class);
            conditional.execute(AgentInitializationException.class, AgentLoadException.class);
            conditional.execute(AlreadyBoundException.class, AttachNotSupportedException.class, AWTException.class);
            conditional.execute(BackingStoreException.class, BadAttributeValueExpException.class,
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
        Conditional conditional = conditional(TRUE)
                                    .onTrue(() -> System.out.println(HELLO));
        conditional.execute(AnnotationTypeMismatchException.class);
        conditional.execute(null);
        conditional.execute(ArithmeticException.class, ArrayStoreException.class);
        conditional.execute(null, null, null);
        conditional.execute(BufferOverflowException.class, BufferUnderflowException.class, CannotRedoException.class);
        conditional.execute(null, null, null);
        conditional.execute(CannotUndoException.class, CatalogException.class,
                            ClassCastException.class, ClassNotPreparedException.class);
        conditional.execute(null, null, null, null);
    }

//  <!-- ====================================================================== -->
//  <!--        EXECUTION OPERATIONS - STATIC                                   -->
//  <!-- ====================================================================== -->

    @Test
    @SuppressWarnings("OverlyBroadThrowsClause")
    void mustExecuteOnStatic() throws Exception {
        Runnable actionForTrue = spy(Runnable.class);
        Runnable actionForFalse = spy(Runnable.class);

        onTrueExecute(FALSE, actionForTrue);
        verify(actionForTrue, never()).run();
        onTrueExecute(TRUE, actionForTrue);
        verify(actionForTrue, times(NumberUtils.INTEGER_ONE)).run();
        onTrueExecute(FALSE, actionForTrue, RuntimeException.class);
        verify(actionForTrue, times(NumberUtils.INTEGER_ONE)).run();
        onTrueExecute(TRUE, actionForTrue, RuntimeException.class);
        verify(actionForTrue, times(NumberUtils.INTEGER_TWO)).run();

        onFalseExecute(TRUE, actionForFalse);
        verify(actionForFalse, never()).run();
        onFalseExecute(FALSE, actionForFalse);
        verify(actionForFalse, times(NumberUtils.INTEGER_ONE)).run();
        onFalseExecute(TRUE, actionForFalse, RuntimeException.class);
        verify(actionForFalse, times(NumberUtils.INTEGER_ONE)).run();
        onFalseExecute(FALSE, actionForFalse, RuntimeException.class);
        verify(actionForFalse, times(NumberUtils.INTEGER_TWO)).run();
    }

    @Test
    void mustThrowFromActionOnStaticExecution() {
        RuntimeException uncheckedException = new RuntimeException();
        Exception checkedException = new Exception();

        try {
            onTrueExecute(TRUE, () -> {
                throw uncheckedException;
            });
            fail();
        } catch (RuntimeException actualException) {
            assertEquals(actualException, uncheckedException);
        }

        try {
            onTrueExecute(TRUE, () -> {
                throw uncheckedException;
            }, ArithmeticException.class);
            fail();
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") RuntimeException actualException) {
            assertEquals(actualException, uncheckedException);
        }

        try {
            onFalseExecute(FALSE, () -> {
                throw checkedException;
            });
            fail();
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") Exception actualException) {
            assertEquals(actualException, checkedException);
        }

        try {
            onFalseExecute(FALSE, () -> {
                throw checkedException;
            }, IOException.class);
            fail();
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") Exception actualException) {
            assertEquals(actualException, checkedException);
        }
    }

    @Test
    void mustNotThrowFromActionOnStaticExecution() {
        RuntimeException uncheckedException = new RuntimeException();
        Exception checkedException = new Exception();

        Runnable runnableWithUnchecked = () -> {
            throw uncheckedException;
        };
        Runnable runnableWithChecked = () -> {
            throw checkedException;
        };

        assertAll(
                () -> assertDoesNotThrow(() -> onTrueExecute(FALSE, runnableWithUnchecked)),
                () -> assertDoesNotThrow(() -> onTrueExecute(FALSE, runnableWithUnchecked, RuntimeException.class)),
                () -> assertDoesNotThrow(() -> onFalseExecute(TRUE, runnableWithChecked)),
                () -> assertDoesNotThrow(() -> onFalseExecute(TRUE, runnableWithChecked, RuntimeException.class))
        );
    }

    @Test
    void mustEnforceHandlingOfPassedCheckedExceptionOnStaticExecution() {
        Runnable runnable = () -> System.out.println(HELLO);
        try {
            onTrueExecute(TRUE, runnable, AbsentInformationException.class);
            onFalseExecute(FALSE, runnable, AgentLoadException.class);
        } catch (AbsentInformationException | AgentLoadException exception) {
            log.error("Exception occurred", exception);
            fail();
        }
    }

    @Test
    // Only the possibility to compile the statements in the test without handling
    // the passed exceptions is tested, so the following suppression is valid:
    @SuppressWarnings("squid:S2699")
    void mustNotEnforceHandlingOfPassedUncheckedExceptionAndNullsOnStaticExecution() {
        Runnable runnable = () -> System.out.println(HELLO);
        onTrueExecute(TRUE, runnable, AnnotationTypeMismatchException.class);
        onTrueExecute(TRUE, runnable, null);
        onFalseExecute(FALSE, runnable, ArrayStoreException.class);
        onFalseExecute(FALSE, runnable, null);
    }

    @Test
    void mustHandleNPEWhenSubmittingNullsOnStaticExecution() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> onTrueExecute(TRUE, null)),
                () -> assertThrows(NullPointerException.class, () ->
                        onTrueExecute(TRUE, null, RuntimeException.class)),
                () -> assertDoesNotThrow(() -> onTrueExecute(FALSE, null, RuntimeException.class)),
                () -> assertThrows(NullPointerException.class, () -> onFalseExecute(FALSE, null)),
                () -> assertThrows(NullPointerException.class, () ->
                        onFalseExecute(FALSE, null, RuntimeException.class)),
                () -> assertDoesNotThrow(() -> onFalseExecute(TRUE, null, RuntimeException.class))
        );
    }

//  <!-- ====================================================================== -->
//  <!--        GET OPERATIONS                                                  -->
//  <!-- ====================================================================== -->

    @Test
    void mustGet() {
        // given
        Conditional conditionalTrue = conditional(TRUE)
                .onTrue(() -> TRUE.toString())
                .onFalse(() -> FALSE.toString());
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(() -> TRUE.toString())
                .onFalse(() -> FALSE.toString());

        // when
        String actualResultOnTrue = conditionalTrue.get(String.class);
        String actualResultOnFalse = conditionalFalse.get(String.class);

        // then
        assertAll(
                () -> assertEquals(TRUE.toString(), actualResultOnTrue),
                () -> assertEquals(FALSE.toString(), actualResultOnFalse)
        );
    }

    @Test
    void mustGetNull() {
        // given
        Conditional conditionalTrue = conditional(TRUE)
                .onTrue(() -> System.out.println(HELLO))
                .onFalse(() -> FALSE.toString());
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(() -> TRUE.toString())
                .onFalse(() -> System.out.println(HELLO));

        // when
        String actualResultOnTrue = conditionalTrue.get(String.class);
        String actualResultOnFalse = conditionalFalse.get(String.class);

        // then
        assertAll(
                () -> assertNull(actualResultOnTrue),
                () -> assertNull(actualResultOnFalse)
        );
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void mustThrowNPEWhenPassedTypeIsNullAndWhenGet() {
        // given
        Conditional conditionalTrue = conditional(TRUE)
                .onTrue(() -> System.out.println(HELLO))
                .onFalse(() -> FALSE.toString());
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(() -> TRUE.toString())
                .onFalse(() -> System.out.println(HELLO));

        // then
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> conditionalTrue.get(null)),
                () -> assertThrows(NullPointerException.class, () -> conditionalFalse.get(null))
        );
    }

    @Test
    void mustThrowMismatchedReturnTypeExceptionWhenGet() {
        // given
        Conditional conditionalTrue = conditional(TRUE)
                .onTrue(() -> TRUE.toString())
                .onFalse(() -> FALSE.toString());
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(() -> TRUE.toString())
                .onFalse(() -> FALSE.toString());

        // then
        assertAll(
                () -> assertThrows(MismatchedReturnTypeException.class, () -> conditionalTrue.get(List.class)),
                () -> assertThrows(MismatchedReturnTypeException.class, () -> conditionalFalse.get(List.class))
        );
    }

    @Test
    void mustThrowUndeterminedReturnValueExceptionWhenGet() {
        // given
        Conditional conditionalWithTooManyActions = conditional(TRUE)
                .onTrue(() -> TRUE.toString())
                .onTrue(() -> System.out.println(HELLO));
        Conditional conditionalWithTooLittleActions = conditional(FALSE)
                .onTrue(() -> TRUE.toString())
                .onTrue(() -> System.out.println(HELLO));
        Conditional conditionalWithOneAction = conditional(TRUE)
                .onTrue(() -> TRUE.toString());

        // then
        assertAll(
                () -> assertThrows(UndeterminedReturnValueException.class,
                        () -> conditionalWithTooManyActions.get(String.class)),
                () -> assertThrows(UndeterminedReturnValueException.class,
                        () -> conditionalWithTooLittleActions.get(String.class)),
                () -> assertDoesNotThrow(() -> conditionalWithOneAction.get(String.class))
        );
    }

    @Test
    void mustThrowExceptionFromPassedActionWhenGet() {
        // given
        RuntimeException uncheckedException = new RuntimeException();
        Exception checkedException = new Exception();
        Conditional conditionalTrue = conditional(TRUE)
                .onTrue(() -> {
                    throw uncheckedException;
                })
                .onFalse(() -> FALSE.toString());
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(() -> TRUE.toString())
                .onFalse(() -> {
                    throw checkedException;
                });

        // then
        try {
            conditionalTrue.get(String.class);
            fail();
        } catch (RuntimeException caughtException) {
            assertEquals(uncheckedException, caughtException);
        }

        try {
            conditionalFalse.get(String.class);
            fail();
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") Exception caughtException) {
            assertEquals(checkedException, caughtException);
        }
    }

    @Test
    void mustGetWithExceptionsViaGetWithoutExceptions() {
        // given
        Conditional conditional = spy(conditional(TRUE));
        conditional.onTrue(() -> HELLO);

        // when
        conditional.get(String.class, RuntimeException.class);
        conditional.get(String.class, RuntimeException.class, RuntimeException.class);
        conditional.get(String.class, RuntimeException.class, RuntimeException.class, RuntimeException.class);
        conditional.get(String.class, RuntimeException.class, RuntimeException.class,
                        RuntimeException.class, RuntimeException.class);

        // then
        verify(conditional, times(4)).get(String.class);
    }

    @Test
    void mustEnforceHandlingOfPassedCheckedExceptionsWhenGet() {
        Conditional conditional = spy(conditional(TRUE));
        conditional.onTrue(() -> HELLO);
        try {
            conditional.get(String.class, AbsentInformationException.class);
            conditional.get(String.class, AgentInitializationException.class, AgentLoadException.class);
            conditional.get(String.class, AlreadyBoundException.class, AttachNotSupportedException.class, AWTException.class);
            conditional.get(String.class, BackingStoreException.class, BadAttributeValueExpException.class,
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
        Conditional conditional = spy(conditional(TRUE));
        conditional.onTrue(() -> HELLO);
        conditional.get(String.class, AnnotationTypeMismatchException.class);
        conditional.get(String.class, null);
        conditional.get(String.class, ArithmeticException.class, ArrayStoreException.class);
        conditional.get(String.class, null, null, null);
        conditional.get(String.class, BufferOverflowException.class, BufferUnderflowException.class, CannotRedoException.class);
        conditional.get(String.class, null, null, null);
        conditional.get(String.class, CannotUndoException.class, CatalogException.class,
                        ClassCastException.class, ClassNotPreparedException.class);
        conditional.get(String.class, null, null, null, null);
    }

//  <!-- ====================================================================== -->
//  <!--        DISCARD OPERATIONS                                              -->
//  <!-- ====================================================================== -->

    @Test
    void mustDiscardAllActions() {
        // given
        List<String> listForTrue = spy(new ArrayList<>());
        List<String> listForFalse = spy(new ArrayList<>());
        Conditional conditionalTrue = conditional(TRUE)
                .onTrue(listForTrue::clear)
                .onFalse(listForFalse::clear);
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(listForTrue::clear)
                .onFalse(listForFalse::clear);

        // when
        conditionalTrue.discardAllActions();
        conditionalFalse.discardAllActions();
        conditionalTrue.execute();
        conditionalFalse.execute();

        // then
        assertAll(
                () -> verify(listForTrue, never()).clear(),
                () -> verify(listForFalse, never()).clear()
        );
    }

    @Test
    void mustDiscardActionsOnTrue() {
        // given
        List<String> listForTrue = spy(new ArrayList<>());
        List<String> listForFalse = spy(new ArrayList<>());
        Conditional conditionalTrue = conditional(TRUE)
                .onTrue(listForTrue::clear)
                .onFalse(listForFalse::clear);
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(listForTrue::clear)
                .onFalse(listForFalse::clear);

        // when
        conditionalTrue.discardActionsOnTrue();
        conditionalFalse.discardActionsOnTrue();
        conditionalTrue.execute();
        conditionalFalse.execute();

        // then
        assertAll(
                () -> verify(listForTrue, never()).clear(),
                () -> verify(listForFalse, times(NumberUtils.INTEGER_ONE)).clear()
        );
    }

    @Test
    void mustDiscardActionsOnFalse() {
        // given
        List<String> listForTrue = spy(new ArrayList<>());
        List<String> listForFalse = spy(new ArrayList<>());
        Conditional conditionalTrue = conditional(TRUE)
                .onTrue(listForTrue::clear)
                .onFalse(listForFalse::clear);
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(listForTrue::clear)
                .onFalse(listForFalse::clear);

        // when
        conditionalTrue.discardActionsOnFalse();
        conditionalFalse.discardActionsOnFalse();
        conditionalTrue.execute();
        conditionalFalse.execute();

        // then
        assertAll(
                () -> verify(listForTrue, times(NumberUtils.INTEGER_ONE)).clear(),
                () -> verify(listForFalse, never()).clear()
        );
    }

//  <!-- ====================================================================== -->
//  <!--        EXCEPTIONS                                                      -->
//  <!-- ====================================================================== -->

    @ParameterizedTest
    @MethodSource("generateTrueAndFalseConditionals")
    void mustThrowOnTrue(Conditional conditionalTrue, Conditional conditionalFalse) {
        ChildRuntimeException exceptionForTrue = new ChildRuntimeException(EXCEPTION_TEST_MESSAGE);
        ChildException exceptionForFalse = new ChildException(EXCEPTION_TEST_MESSAGE);
        conditionalTrue.onTrueThrow(exceptionForTrue);
        conditionalFalse.onTrueThrow(exceptionForFalse);

        try {
            conditionalTrue.execute();
            fail();
        } catch (ChildRuntimeException caughtException) {
            assertEquals(exceptionForTrue, caughtException);
        }
        assertDoesNotThrow(() -> conditionalFalse.execute());
    }

    @ParameterizedTest
    @MethodSource("generateTrueAndFalseConditionals")
    void mustThrowOnFalse(Conditional conditionalTrue, Conditional conditionalFalse) {
        ChildRuntimeException exceptionForTrue = new ChildRuntimeException(EXCEPTION_TEST_MESSAGE);
        ArithmeticException exceptionForFalse = new ArithmeticException(EXCEPTION_TEST_MESSAGE);
        conditionalTrue.onFalseThrow(exceptionForTrue);
        conditionalFalse.onFalseThrow(exceptionForFalse);

        try {
            conditionalFalse.execute();
            fail();
        } catch (ArithmeticException caughtException) {
            assertEquals(exceptionForFalse, caughtException);
        }
        assertDoesNotThrow(() -> conditionalTrue.execute());
    }

    @ParameterizedTest
    @MethodSource("generateRuntimeException")
    void testIsTrueOrThrowForRuntimeException(RuntimeException genericException) {
        assertDoesNotThrow(() -> Conditional.isTrueOrThrow(TRUE, genericException));
        try {
            Conditional.isTrueOrThrow(FALSE, genericException);
            fail();
        } catch (RuntimeException caughtException) {
            assertEquals(caughtException, genericException);
        }
    }

    @ParameterizedTest
    @MethodSource("generateException")
    void testIsTrueOrThrowForException(Exception genericException) {
        assertDoesNotThrow(() -> Conditional.isTrueOrThrow(TRUE, genericException));
        try {
            Conditional.isTrueOrThrow(FALSE, genericException);
            fail();
        } catch (Exception caughtException) {
            assertEquals(caughtException, genericException);
        }
    }

    @ParameterizedTest
    @MethodSource("generateRuntimeException")
    void testIsFalseOrThrowForRuntimeException(RuntimeException genericException) {
        assertDoesNotThrow(() -> Conditional.isFalseOrThrow(FALSE, genericException));
        try {
            Conditional.isFalseOrThrow(TRUE, genericException);
            fail();
        } catch (RuntimeException caughtException) {
            assertEquals(caughtException, genericException);
        }
    }

    @ParameterizedTest
    @MethodSource("generateException")
    void testIsFalseOrThrowForException(Exception genericException) {
        assertDoesNotThrow(() -> Conditional.isFalseOrThrow(FALSE, genericException));
        try {
            Conditional.isFalseOrThrow(TRUE, genericException);
            fail();
        } catch (Exception caughtException) {
            assertEquals(caughtException, genericException);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void mustThrowNPEWhenNullAsExceptionPassed() {
        Conditional conditionalTrueWithException = conditional(TRUE)
                .onTrueThrow(null);
        Conditional conditionalTrueWithoutException = conditional(TRUE)
                .onFalseThrow(null);
        Conditional conditionalFalseWithException = conditional(FALSE)
                .onFalseThrow(null);
        Conditional conditionalFalseWithoutException = conditional(FALSE)
                .onTrueThrow(null);

        assertAll(
                () -> assertThrows(NullPointerException.class, conditionalTrueWithException::execute),
                () -> assertDoesNotThrow(() -> conditionalTrueWithoutException.execute()),
                () -> assertThrows(NullPointerException.class, conditionalFalseWithException::execute),
                () -> assertDoesNotThrow(() -> conditionalFalseWithoutException.execute()),
                () -> assertThrows(NullPointerException.class, () -> isTrueOrThrow(FALSE, null)),
                () -> assertDoesNotThrow(() -> isTrueOrThrow(TRUE, null)),
                () -> assertThrows(NullPointerException.class, () -> isFalseOrThrow(TRUE, null)),
                () -> assertDoesNotThrow(() -> isFalseOrThrow(FALSE, null))
        );
    }

//  <!-- ====================================================================== -->
//  <!--        UTILS                                                           -->
//  <!-- ====================================================================== -->

    static Stream<Arguments> generateActionsPair() {
        return Stream.of(
                arguments(generateSixActionsFromCallableAndRunnable(),
                          generateSixActionsFromCallableAndRunnable())
        );
    }

    static List<Action<?>> generateSixActionsFromCallableAndRunnable() {
        List<Action<?>> actionsFromCallable = wrapCallableIntoActions(generateThreeCallable());
        List<Action<?>> actionsFromRunnable = wrapRunnableIntoActions(generateThreeRunnable());
        return Stream.concat(actionsFromCallable.stream(), actionsFromRunnable.stream())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static List<Action<?>> wrapCallableIntoActions(Collection<Callable<?>> callableToWrap) {
        return callableToWrap.stream()
                .map(callable -> (Action<?>) new Action<>(callable))
                .map(Mockito::spy)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static List<Action<?>> wrapRunnableIntoActions(Collection<Runnable> runnableToWrap) {
        return runnableToWrap.stream()
                .map(runnable -> (Action<?>) new Action<>(runnable))
                .map(Mockito::spy)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static List<Callable<?>> generateThreeCallable() {
        return IntStream.rangeClosed(1, 3)
                .boxed()
                .map(num -> (Callable<String>) () -> HELLO)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static List<Runnable> generateThreeRunnable() {
        return IntStream.rangeClosed(1, 3)
                .boxed()
                .map(num -> (Runnable) () -> System.out.println(HELLO))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    ActionsList extractActionsViaReflection(Conditional conditional, boolean actionsCategory) {
        Map<Boolean, ActionsList> actionsMap = extractUnaryInternalActionsMap(conditional);
        return actionsMap.get(actionsCategory);
    }

    Map<Boolean, ActionsList> extractUnaryInternalActionsMap(Conditional conditional) {
        List<Map<Boolean, ActionsList>> internalActionsMaps = extractAllInternalActionsMaps(conditional);
        Validate.isTrue(internalActionsMaps.size() == NumberUtils.INTEGER_ONE);
        return internalActionsMaps.get(NumberUtils.INTEGER_ZERO);
    }

    List<Map<Boolean, ActionsList>> extractAllInternalActionsMaps(Conditional conditional) {
        Class<? extends Conditional> conditionalClass = conditional.getClass();
        Field[] conditionalFields = conditionalClass.getDeclaredFields();
        Stream.of(conditionalFields).forEach(field -> field.setAccessible(TRUE));
        //noinspection unchecked
        return Stream.of(conditionalFields)
                .map(field -> {
                    try {
                        return field.get(conditional);
                    } catch (IllegalAccessException exception) {
                        log.error("Illegal access", exception);
                    }
                    throw new IllegalStateException("Unforeseen application flow");
                })
                .filter(fieldObj -> {
                    Class<?> fieldObjAsClass = fieldObj.getClass();
                    String fieldObjClassName = fieldObjAsClass.getName();
                    //noinspection rawtypes
                    Class<HashMap> hashMapClass = HashMap.class;
                    String hashMapClassName = hashMapClass.getName();
                    return fieldObjClassName.equals(hashMapClassName);
                })
                .map(fieldObj -> (Map<Boolean, ActionsList>) fieldObj)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    static Stream<Arguments> generateBooleans() {
        return Stream.of(
                arguments(TRUE),
                arguments(FALSE)
        );
    }

    static Stream<Arguments> generateRuntimeException() {
        return Stream.of(
                arguments(new RuntimeException(EXCEPTION_TEST_MESSAGE)),
                arguments(new ChildRuntimeException(EXCEPTION_TEST_MESSAGE))
        );
    }

    static Stream<Arguments> generateException() {
        return Stream.of(
                arguments(new Exception(EXCEPTION_TEST_MESSAGE)),
                arguments(new ChildException(EXCEPTION_TEST_MESSAGE))
        );
    }

    static Stream<Arguments> generateTrueAndFalseConditionals() {
        return Stream.of(
                arguments(conditional(TRUE), conditional(FALSE))
        );
    }
}
