package eu.ciechanowiec.conditional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static eu.ciechanowiec.conditional.Conditional.conditional;
import static eu.ciechanowiec.conditional.Variables.EXCEPTION_TEST_MESSAGE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@SuppressWarnings({"ChainedMethodCall", "ClassWithTooManyMethods"})
@Slf4j
@ExtendWith(MockitoExtension.class)
class ConditionalTest {

//  <!-- ====================================================================== -->
//  <!--        CREATION                                                        -->
//  <!-- ====================================================================== -->

    @ParameterizedTest
    @MethodSource("generateBooleans")
    void mustCreateSpecifiedConditional(boolean expectedValue) {
        Conditional conditional = conditional(expectedValue);
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
        ActionsList expectedActionsOnTrue = extractActions(conditional, TRUE);
        ActionsList actualActionsOnTrue = conditional.actionsOnTrue();
        ActionsList expectedActionsOnFalse = extractActions(conditional, FALSE);
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
            return Variables.HELLO;
        };

        List<String> listForFalse = spy(new ArrayList<>());
        Callable<String> callableForFalse = () -> {
            listForFalse.clear();
            return Variables.HELLO;
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
                () -> actionsOnTrue.forEach(action -> {
                    verify(action, times(NumberUtils.INTEGER_TWO)).execute();
                }),
                () -> actionsOnFalse.forEach(action -> {
                    verify(action, times(NumberUtils.INTEGER_ONE)).execute();
                })
        );
    }

//  <!-- ====================================================================== -->
//  <!--        ACTIONS OPERATIONS                                              -->
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
                () -> actionsOnTrue.forEach(action -> {
                    verify(action, times(NumberUtils.INTEGER_ONE)).execute();
                }),
                () -> actionsOnFalse.forEach(action -> {
                    verify(action, never()).execute();
                })
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
                () -> actionsOnTrue.forEach(action -> {
                    verify(action, times(NumberUtils.INTEGER_TWO)).execute();
                }),
                () -> actionsOnFalse.forEach(action -> {
                    verify(action, never()).execute();
                })
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
                () -> actionsOnTrue.forEach(action -> {
                    verify(action, never()).execute();
                }),
                () -> actionsOnFalse.forEach(action -> {
                    verify(action, times(NumberUtils.INTEGER_ONE)).execute();
                })
        );
    }

    @Test
    void mustExecuteSubsequently() {
        List<String> expectedValues = List.of("A", "B", "C");
        List<String> actualValues = new ArrayList<>();
        conditional(TRUE)
                .onTrue(() -> actualValues.add("A"))
                .onTrue(() -> actualValues.add("B"))
                .onTrue(() -> actualValues.add("C"))
                .execute();
        assertEquals(actualValues, expectedValues);
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
                () -> actionsOnTrue.forEach(action -> {
                    verify(action, times(NumberUtils.INTEGER_TWO)).execute();
                }),
                () -> actionsOnFalse.forEach(action -> {
                    verify(action, never()).execute();
                })
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
                () -> actionsOnTrue.forEach(action -> {
                    verify(action, never()).execute();
                }),
                () -> actionsOnFalse.forEach(action -> {
                    verify(action, times(NumberUtils.INTEGER_TWO)).execute();
                })
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
                () -> actionsOnTrue.forEach(action -> {
                    verify(action, never()).execute();
                }),
                () -> actionsOnFalse.forEach(action -> {
                    verify(action, never()).execute();
                })
        );
    }

    @ParameterizedTest
    @MethodSource("generateTrueAndFalseConditionals")
    void mustExecuteAndThrow(Conditional conditionalTrue, Conditional conditionalFalse) {
        // given
        RuntimeException exceptionForTrue = new RuntimeException();
        Exception exceptionForFalse = new Exception();
        conditionalTrue.onTrue(() -> {
            throw exceptionForTrue;
        });
        conditionalFalse.onTrue(() -> {
            throw exceptionForFalse;
        });

        try {
            conditionalTrue.execute();
            fail();
        } catch (WrapperException exception) {
            Throwable causeException = exception.getCause();
            assertEquals(causeException, exceptionForTrue);
        }
        assertDoesNotThrow(() -> conditionalFalse.execute());
    }

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
                .onTrue(() -> System.out.println(Variables.HELLO))
                .onFalse(() -> FALSE.toString());
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(() -> TRUE.toString())
                .onFalse(() -> System.out.println(Variables.HELLO));

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
    void mustGetWithMismatchedReturnTypeException() {
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
    void mustGetWithWrapperException() {
        // given
        Exception exceptionToThrow = new Exception();
        Conditional conditionalTrue = conditional(TRUE)
                .onTrue(() -> {
                    throw exceptionToThrow;
                })
                .onFalse(() -> FALSE.toString());
        Conditional conditionalFalse = conditional(FALSE)
                .onTrue(() -> TRUE.toString())
                .onFalse(() -> {
                    throw exceptionToThrow;
                });

        // then
        try {
            conditionalTrue.get(String.class);
            fail();
        } catch (WrapperException caughtException) {
            Throwable exceptionCause = caughtException.getCause();
            assertEquals(exceptionCause, exceptionToThrow);
        }

        try {
            conditionalFalse.get(String.class);
            fail();
        } catch (WrapperException caughtException) {
            Throwable exceptionCause = caughtException.getCause();
            assertEquals(exceptionCause, exceptionToThrow);
        }
    }

    @Test
    void mustGetWithUndeterminedReturnValueException() {
        // given
        Conditional conditionalWithTooManyActions = conditional(TRUE)
                .onTrue(() -> TRUE.toString())
                .onTrue(() -> System.out.println(Variables.HELLO));
        Conditional conditionalWithTooLittleActions = conditional(FALSE)
                .onTrue(() -> TRUE.toString())
                .onTrue(() -> System.out.println(Variables.HELLO));
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
        } catch (WrapperException exception) {
            Throwable causeException = exception.getCause();
            assertEquals(causeException, exceptionForTrue);
        }
        assertDoesNotThrow(() -> conditionalFalse.execute());
    }

    @ParameterizedTest
    @MethodSource("generateTrueAndFalseConditionals")
    void mustThrowOnFalse(Conditional conditionalTrue, Conditional conditionalFalse) {
        ChildRuntimeException exceptionForTrue = new ChildRuntimeException(EXCEPTION_TEST_MESSAGE);
        ChildException exceptionForFalse = new ChildException(EXCEPTION_TEST_MESSAGE);
        conditionalTrue.onFalseThrow(exceptionForTrue);
        conditionalFalse.onFalseThrow(exceptionForFalse);

        try {
            conditionalFalse.execute();
            fail();
        } catch (WrapperException exception) {
            Throwable causeException = exception.getCause();
            assertEquals(causeException, exceptionForFalse);
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
                .map(num -> (Callable<String>) () -> Variables.HELLO)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    static List<Runnable> generateThreeRunnable() {
        return IntStream.rangeClosed(1, 3)
                .boxed()
                .map(num -> (Runnable) () -> System.out.println(Variables.HELLO))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    ActionsList extractActions(Conditional conditional, boolean actionsCategory) {
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
