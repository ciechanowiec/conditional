package eu.ciechanowiec.conditional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Entity that stores {@link Action}s and provides basic API pertaining to the stored elements.
 * <p>
 * Actions are stored in an internal {@link List}.
 */
public class ActionsList {

    /**
     * Internal list where {@link Action}s are actually stored.
     */
    private final List<Action<?>> internalList;

    /**
     * Constructs an instance of an {@link ActionsList} that stores {@link Action}s
     * and provides basic API pertaining to the stored elements.
     * <p>
     * Actions are stored in an internal {@link List}.
     */
    public ActionsList() {
        internalList = new ArrayList<>();
    }

    /**
     * Adds the passed {@link Action} to this actions list.
     * <p>
     * The {@link Action} is added at the front of this actions list.
     * @param actionToAdd {@link Action} to add to this actions list
     * @param <T> type of value returned in the result of submitted action execution
     */
    public <T> void add(Action<T> actionToAdd) {
        internalList.add(actionToAdd);
    }

    /**
     * Retrieves, but does not remove, the first {@link Action} from this actions list.
     * @return the first element of this actions list
     * @throws NoSuchElementException if this actions list is empty
     */
    @SuppressWarnings({"squid:S1452", "squid:S1166"})
    public Action<?> getFirst() {
        try {
            return internalList.get(0);
        } catch (IndexOutOfBoundsException exception) {
            throw new NoSuchElementException("Actions list is empty. Nothing to return");
        }
    }

    /**
     * Informs, whether this actions list stores exactly one action.
     * @return {@code true} if this actions list stores exactly one action; {@code false} otherwise
     */
    boolean isExactlyOneActionInList() {
        return internalList.size() == 1;
    }

    /**
     * Removes all {@link Action}s from this actions list.
     * <p>
     * The list will be empty after this call returns.
     */
    public void clear() {
        internalList.clear();
    }

    /**
     * Retrieves by reference all instances of {@link Action}s stored in this actions list.
     * @return {@link List} of all instances of {@link Action}s stored in this actions list;
     * the returned list isn't the same instance of {@link List} used by this actions list internally
     * to store {@link Action}s
     */
    @SuppressWarnings("squid:S1452")
    public List<Action<?>> getAll() {
        return new ArrayList<>(internalList);
    }

    /**
     * Executes, subsequently and starting from the first one, all {@link Action}s stored in this actions list.
     * <p>
     * Execution is performed by calling an {@link Action#execute()} method of an executed {@link Action}.
     */
    public void executeAll() {
        internalList.forEach(Action::execute);
    }
}
