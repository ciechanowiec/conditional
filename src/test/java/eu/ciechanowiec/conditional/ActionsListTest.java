package eu.ciechanowiec.conditional;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class ActionsListTest {

    @Spy
    private ActionsList actionsList;

    private Action<String> testAction;
    private List<Action<?>> internalList;

    @BeforeEach
    void setup() {
        testAction = new Action<>(() -> "Hello, Universe!");
        internalList = extractUnaryInternalList(actionsList);
    }

    @SuppressWarnings("unchecked")
    @Test
    void mustAdd() {
        assertTrue(internalList.isEmpty());
        actionsList.add(testAction);
        Action<String> actualAction = (Action<String>) internalList.get(0);
        assertAll(
                () -> assertEquals(testAction, actualAction),
                () -> assertEquals(NumberUtils.INTEGER_ONE, internalList.size())
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void mustGetFirst() {
        assertTrue(internalList.isEmpty());
        actionsList.add(testAction);
        Action<String> actualAction = (Action<String>) actionsList.getFirst();
        assertEquals(testAction, actualAction);
    }

    @Test
    void mustThrowWhenGetFirst() {
        assertAll(
                () -> assertTrue(internalList.isEmpty()),
                () -> assertThrows(NoSuchElementException.class, () -> actionsList.getFirst())
        );
    }

    @Test
    void testIsExactlyOneActionInCollection() {
        assertTrue(internalList.isEmpty());
        actionsList.add(testAction);
        assertTrue(actionsList.isExactlyOneActionInList());
        actionsList.add(testAction);
        assertFalse(actionsList.isExactlyOneActionInList());
    }

    @Test
    void mustClear() {
        assertTrue(internalList.isEmpty());
        actionsList.add(testAction);
        assertTrue(actionsList.isExactlyOneActionInList());
        actionsList.clear();
        assertTrue(internalList.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("generateActionsSpies")
    void mustGetAll(Iterable<Action<?>> actionSpiesToAdd) {
        assertTrue(internalList.isEmpty());
        actionSpiesToAdd.forEach(actionsList::add);
        List<Action<?>> actualActions = actionsList.getAll();
        boolean allActionsAreInBothLists = internalList.size() == actualActions.size()
                && internalList.containsAll(actualActions)
                && actualActions.containsAll(internalList);
        boolean differentInstancesOfLists = internalList != actualActions;
        assertAll(
                () -> assertTrue(allActionsAreInBothLists),
                () -> assertTrue(differentInstancesOfLists)
        );
    }

    @ParameterizedTest
    @MethodSource("generateActionsSpies")
    void mustExecuteAll(Iterable<Action<?>> actionSpiesToExecute) {
        actionSpiesToExecute.forEach(actionsList::add);
        actionsList.executeAll();
        actionSpiesToExecute.forEach(action -> {
            verify(action, times(NumberUtils.INTEGER_ONE)).execute();
        });
        actionsList.executeAll();
        actionSpiesToExecute.forEach(action -> {
            verify(action, times(NumberUtils.INTEGER_TWO)).execute();
        });
    }

    static Stream<Arguments> generateActionsSpies() {
        Action<?> actionOne = new Action<>(() -> System.out.println(Variables.HELLO));
        Action<?> actionOneSpy = spy(actionOne);
        Action<?> actionTwo = new Action<>(() -> Variables.HELLO);
        Action<?> actionTwoSpy = spy(actionTwo);
        return Stream.of(
                arguments(List.of(actionOneSpy, actionTwoSpy))
        );
    }

    List<Action<?>> extractUnaryInternalList(ActionsList actionsList) {
        List<List<Action<?>>> allInternalActionsLists = extractAllInternalLists(actionsList);
        Validate.isTrue(allInternalActionsLists.size() == NumberUtils.INTEGER_ONE);
        return allInternalActionsLists.get(NumberUtils.INTEGER_ZERO);
    }

    List<List<Action<?>>> extractAllInternalLists(ActionsList actionsList) {
        Class<? extends ActionsList> conditionalClass = actionsList.getClass();
        Field[] actionsListFields = conditionalClass.getDeclaredFields();
        Stream.of(actionsListFields).forEach(field -> field.setAccessible(TRUE));
        //noinspection unchecked
        return Stream.of(actionsListFields)
                .map(field -> {
                    try {
                        return field.get(actionsList);
                    } catch (IllegalAccessException exception) {
                        log.error("Illegal access", exception);
                    }
                    throw new IllegalStateException("Unforeseen application flow");
                })
                .filter(fieldObj -> {
                    Class<?> fieldObjAsClass = fieldObj.getClass();
                    String fieldObjClassName = fieldObjAsClass.getName();
                    //noinspection rawtypes
                    Class<ArrayList> arrayListClass = ArrayList.class;
                    String arrayListClassName = arrayListClass.getName();
                    return fieldObjClassName.equals(arrayListClassName);
                })
                .map(fieldObj -> (List<Action<?>>) fieldObj)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
