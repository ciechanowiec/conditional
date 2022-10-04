package eu.ciechanowiec.conditional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Entity that can:
 * <ol>
 *     <li>execute a predefined command in a void manner
 *         (see {@link Action#execute()});</li>
 *     <li>execute a predefined command and return the result of that execution
 *         (see {@link Action#get()}, {@link Action#get(Class)}).</li>
 * </ol>
 * @param <T> type of value returned in the result of action execution
 */
public class Action<T> {

    /**
     * An underlying {@link Callable} instance
     * used as an engine of this action.
     */
    private final Callable<T> engine;

    /**
     * Constructs an action based on the passed {@link Callable}.
     * <p>
     * Contrary to an action based on a passed {@link Runnable}, this constructor constructs
     * an action which in the result of its execution returns a value computed by the call of a
     * {@link Callable#call()} method belonging to the passed {@link Callable}. Therefore, type
     * parameter of an action constructed with this constructor is strongly recommended to describe
     * the type of value that is supposed to be returned, e.g. {@code Action<String>} (see examples below).
     * <p>
     * Example:<pre>{@code
     * Action<String> actionOne = new Action<>(() -> "Hello, Universe!");
     *
     * Action<String> actionTwo = new Action<>(() -> {
     *   System.out.println("Hello, Universe!");
     *   return "How can I help you?";
     * });
     *
     * int num = 10;
     * Action<String> actionThree = new Action<>(() -> {
     *   if (num % 2 != 0) {
     *     throw new Exception("The passed number must be even");
     *   }
     *   return "The passed number is even";
     * }); }</pre>
     * @param engine used for method calls of a constructed action
     */
    public Action(Callable<T> engine) {
        this.engine = engine;
    }

    /**
     * Constructs an action based on the passed {@link Runnable}.
     * <p>
     * Contrary to an action based on a passed {@link Callable}, this constructor constructs
     * an action which in the result of its execution always returns {@code null}. Therefore, type
     * parameter of an action constructed with this constructor can be defined with a wildcard:
     * {@code Action<?>} (see examples below).
     * <p>
     * The passed {@link Runnable} is wrapped into a {@link Callable}. As a result,
     * the execution of this action is performed via nested method calls.
     * Firstly the wrapper {@link Callable#call()} method is called that subsequently:
     * <ol>
     *     <li>calls the wrapped {@link Runnable#run()} method;</li>
     *     <li>returns {@code null}.</li>
     * </ol>
     * <p>
     * Example:<pre>{@code
     * Action<?> actionOne = new Action<>(() -> System.out.println("Hello, Universe!"));
     *
     * Action<?> actionTwo = new Action<>(() -> {
     *   System.out.println("Hello, Universe!");
     *   System.out.println("How can I help you?");
     * });}</pre>
     * @param engine used for method calls of a constructed action
     */
    @SuppressWarnings("ReturnOfNull")
    public Action(Runnable engine) {
        this.engine = () -> {
            engine.run();
            return null;
        };
    }

    /**
     * Executes this action.
     * <ol>
     *     <li>Execution is performed via calling a {@link Callable#call()}
     *     method of the underlying {@link Callable}.</li>
     *     <li>If this action was constructed with the use of {@link Runnable},
     *     then calling a {@link Callable#call()} method of the underlying {@link Callable}
     *     causes the call of a {@link Runnable#run()} method belonging to the {@link Runnable}
     *     wrapped into the underlying {@link Callable}:
     *     <pre>{@code
     *     Action.execute()
     *        └──Callable.call()
     *              └──Runnable.run()
     *     }</pre>
     *     </li>
     *     <li>A return value that is produced in the result
     *     of execution of this action is discarded.</li>
     *     <li>This method can be called multiple times. However, in case of calling it
     *     more than once be aware of possible side effects caused by a previous call. </li>
     * </ol>
     * @throws WrapperException if an {@link Exception} during execution of this action was thrown;
     * in such case the thrown {@link Exception} is set as a cause of an unchecked {@link WrapperException}
     */
    public void execute() {
        try {
            engine.call();
        } catch (Exception exception) { // Repack into unchecked
            throw new WrapperException(exception);
        }
    }

    /**
     * Executes this action and returns a return value that is
     * produced in the result of execution of this action.
     * <p>
     * This method can be called multiple times. However, in case of calling it
     * more than once be aware of possible side effects caused by a previous call.
     * <p>
     * For details on the execution see documentation for {@link Action#execute()}.
     * @return return value that is produced in the result of execution of this action;
     * if {@code null} is produced, then {@code null} is returned;
     * if this action was constructed with the use of {@link Runnable}, then {@code null} is always returned
     * @throws WrapperException if an {@link Exception} during execution of this action was thrown;
     * in such case the thrown {@link Exception} is set as a cause of an unchecked {@link WrapperException}
     */
    public T get() {
        try {
            return engine.call();
        } catch (Exception exception) { // Repack into unchecked
            throw new WrapperException(exception);
        }
    }

    /**
     * Executes this action and returns a return value that is produced in
     * the result of execution of this action, but cast into a specified type.
     * <p>
     * This method can be called multiple times. However, in case of calling it
     * more than once be aware of possible side effects caused by a previous call.
     * <p>
     * For details on the execution see documentation for {@link Action#execute()}.
     * <ol>
     *     <li> Example with {@link String}:
     *     <pre>{@code
     * Callable<String> callableWithString = () -> "Hello, Universe!";
     * Action<String> actionWithString = new Action<>(callableWithString);
     * String returnedValue = actionWithString.get(String.class);
     * // result: "Hello, Universe!"
     *     }</pre>
     *     </li>
     *     <li> Example with {@code null}:
     *     <pre> {@code
     * Callable<?> callableWithNull = () -> null;
     * Action<?> actionWithNull = new Action<>(callableWithNull);
     * String returnedValue = actionWithNull.get(String.class);
     * // result: null
     *     }</pre>
     *     </li>
     *     <li> Example with {@link List}:
     *     <pre>{@code
     * List<String> listToReturn = new ArrayList<>(List.of("Hello, Universe!"));
     * Callable<List<String>> callableWithList = () -> listToReturn;
     * Action<List<String>> actionWithList = new Action<>(callableWithList);
     * List<String> returnedValue = actionWithList.get(List.class);
     * // result: listToReturn == returnedValue
     *     }</pre>
     *      </li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <S>}) to which the return value will be cast into
     * @param <S> type to which the return value will be cast into
     * @return return value that is produced in the result of execution of this action and
     * cast into a specified type ({@code typeToGet}); if {@code null} is produced, then
     * {@code null} is returned regardless of the passed {@code typeToGet}; if this action
     * was constructed with the use of {@link Runnable}, then {@code null} is always returned
     * @throws MismatchedReturnTypeException if a return value cannot be cast into a specified type ({@code typeToGet})
     * due to {@link ClassCastException}
     * @throws WrapperException if an {@link Exception} during execution of this action was thrown;
     * in such case the thrown {@link Exception} is set as a cause of an unchecked {@link WrapperException}
     */
    @SuppressWarnings("squid:S1166")
    public <S> S get(Class<S> typeToGet) {
        T resultOfActionCall = null;
        try {
            resultOfActionCall = engine.call();
            return typeToGet.cast(resultOfActionCall);
        } catch (ClassCastException exception) {
            T resultOfActionCallNotNull = Objects.requireNonNull(resultOfActionCall);
            Class<?> actualReturnType = resultOfActionCallNotNull.getClass();
            String actualReturnTypeAsString = actualReturnType.getName();
            String demandedReturnTypeAsString = typeToGet.getName();
            throw new MismatchedReturnTypeException(actualReturnTypeAsString, demandedReturnTypeAsString);
        } catch (Exception exception) { // Repack into unchecked
            throw new WrapperException(exception);
        }
    }
}
