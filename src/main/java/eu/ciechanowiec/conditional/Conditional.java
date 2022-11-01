package eu.ciechanowiec.conditional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.IntStream;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Basic entity of <i>Conditional</i> library, that replaces {@code if-else} statements and ternary operators with a featured fluent interface.
 * <ol>
 * <li>{@link Conditional} can be instantiated via a {@link Conditional#conditional(boolean)}
 * method. That method accepts a boolean argument ({@code true}/{@code false}), which becomes 
 * an immutable value described by a constructed {@link Conditional}. Described value 
 * determines behavior of a given instance of {@link Conditional}.</li>
 * <li>To a given instance of {@link Conditional} multiple actions can be submitted in a fluent manner.</li>
 * <li>Every submitted action is bound either to a {@code true} or {@code false} boolean value.</li>
 * <li>Submitted actions are supposed to be executed in a void manner <i>or</i> to be executed and return
 * a value in the result that execution. To achieve that, usage of {@link Conditional} is finalized via
 * an {@link Conditional#execute()}, {@link Conditional#execute(int)} or {@link Conditional#get(Class)}
 * method respectively, that triggers actions bound to a value described by a given instance
 * of {@link Conditional}.</li>
 * <li>{@link Conditional} is lazy, which means that respective submitted actions will be triggered when
 * and only when an {@link Conditional#execute()}, {@link Conditional#execute(int)} or
 * {@link Conditional#get(Class)} method is called, hence mere submission of an action
 * doesn't suffice to trigger those actions.</li>
 * </ol>
 * <i>Usage example with void action:</i>
 * <pre>{@code
 * import static eu.ciechanowiec.conditional.Conditional.conditional;
 *
 * public static void main(String[] args) {
 *     conditional(10 % 2 == 0)
 *             .onTrue(() -> System.out.println("Checked number is even"))
 *             .onFalse(() -> System.out.println("Checked number is odd"))
 *             .execute();
 * }}</pre>
 * <i>Usage example with action returning a value:</i>
 * <pre>{@code
 * import static eu.ciechanowiec.conditional.Conditional.conditional;
 *
 * public static void main(String[] args) {
 *     String evenOrOdd = evenOrOdd(10);
 *     System.out.println("Result: " + evenOrOdd);
 * }
 *
 * private static String evenOrOdd(int numToCheck) {
 *     return conditional(numToCheck % 2 == 0)
 *                       .onTrue(() -> {
 *                           System.out.println("Returning a value on true...");
 *                           return "Even!";
 *                       })
 *                       .onFalse(() -> "Odd!")
 *                       .get(String.class);
 * }}</pre>
 */
public final class Conditional {

    private final boolean describedValue;
    private final Map<Boolean, ActionsList> actionsMap;

    /**
     * Constructs an instance of a {@link Conditional} that describes the passed
     * boolean value ({@code true} or {@code false}). That value is final
     * and cannot be changed in conventional way.
     * @param describedValue value described by the created conditional
     */
    private Conditional(boolean describedValue) {
        this.describedValue = describedValue;
        actionsMap = new HashMap<>();
        actionsMap.put(TRUE, new ActionsList());
        actionsMap.put(FALSE, new ActionsList());
    }

    /**
     * Returns a new instance of a {@link Conditional} that describes the passed
     * boolean value ({@code true} or {@code false}). That value is final
     * and cannot be changed in conventional way.
     * @param describedValue value that will be described by the created conditional
     * @return new instance of a conditional that describes the passed boolean value
     */
    public static Conditional conditional(boolean describedValue) {
        return new Conditional(describedValue);
    }

//  <!-- ====================================================================== -->
//  <!--        RETRIEVING DATA                                                 -->
//  <!-- ====================================================================== -->

    /**
     * Returns the value described by this conditional ({@code true} or {@code false}).
     * @return value described by this conditional ({@code true} or {@code false})
     */
    public boolean describedValue() {
        return describedValue;
    }

    /**
     * Answers whether this conditional describes a {@code true} value.
     * @return {@code true} if this conditional describes a {@code true} value;
     * {@code false} otherwise
     */
    public boolean isTrue() {
        return describedValue == TRUE;
    }

    /**
     * Answers whether this conditional describes a {@code false} value.
     * @return {@code true} if this conditional describes a {@code false} value;
     * {@code false} otherwise
     */
    public boolean isFalse() {
        return describedValue == FALSE;
    }

    /**
     * Retrieves by reference an {@link ActionsList} that stores all actions
     * submitted to this conditional and bound to a {@code true} value.
     * @return {@link ActionsList} (by reference) that stores all actions
     * submitted to this conditional and bound to a {@code true} value
     */
    public ActionsList actionsOnTrue() {
        return actionsMap.get(TRUE);
    }

    /**
     * Retrieves by reference an {@link ActionsList} that stores all actions
     * submitted to this conditional and bound to a {@code false} value.
     * @return {@link ActionsList} (by reference) that stores all actions
     * submitted to this conditional and bound to a {@code false} value
     */
    public ActionsList actionsOnFalse() {
        return actionsMap.get(FALSE);
    }

//  <!-- ====================================================================== -->
//  <!--        ACTIONS SUBMISSION                                              -->
//  <!-- ====================================================================== -->

    /**
     * Submits an action to this conditional and bounds it to a {@code true} value.
     * <p>
     * The submitted {@link Callable} is wrapped into an instance of an {@link Action}
     * via an {@link Action#Action(Callable)} constructor and is managed via API
     * of that {@link Action}. See documentation for {@link Action} for details.
     * @param actionOnTrue action that should be submitted to this conditional and bound to a {@code true} value
     * @param <T> type of value returned in the result of submitted action execution
     * @return this conditional after submitting an action
     */
    public <T> Conditional onTrue(Callable<T> actionOnTrue) {
        Action<T> actionOnTrueWrapped = new Action<>(actionOnTrue);
        addToActions(actionOnTrueWrapped, TRUE);
        return this;
    }

    /**
     * Submits an action to this conditional and bounds it to a {@code true} value.
     * <p>
     * The submitted {@link Runnable} is wrapped into an instance of an {@link Action}
     * via an {@link Action#Action(Runnable)} constructor and is managed via API
     * of that {@link Action}. See documentation for {@link Action} for details.
     * @param actionOnTrue action that should be submitted to this conditional and bound to a {@code true} value
     * @return this conditional after submitting an action
     */
    public Conditional onTrue(Runnable actionOnTrue) {
        Action<?> actionOnTrueWrapped = new Action<>(actionOnTrue);
        addToActions(actionOnTrueWrapped, TRUE);
        return this;
    }

    /**
     * Submits an action to this conditional and bounds it to a {@code true} value.
     * @param actionOnTrue action that should be submitted to this conditional and bound to a {@code true} value
     * @param <T> type of value returned in the result of submitted action execution
     * @return this conditional after submitting an action
     */
    public <T> Conditional onTrue(Action<T> actionOnTrue) {
        addToActions(actionOnTrue, TRUE);
        return this;
    }

    /**
     * Submits an action to this conditional and bounds it to a {@code false} value.
     * <p>
     * The submitted {@link Callable} is wrapped into an instance of an {@link Action}
     * via an {@link Action#Action(Callable)} constructor and is managed via API
     * of that {@link Action}. See documentation for {@link Action} for details.
     * @param actionOnFalse action that should be submitted to this conditional and bound to a {@code false} value
     * @param <T> type of value returned in the result of submitted action execution
     * @return this conditional after submitting an action
     */
    public <T> Conditional onFalse(Callable<T> actionOnFalse) {
        Action<T> actionOnFalseWrapped = new Action<>(actionOnFalse);
        addToActions(actionOnFalseWrapped, FALSE);
        return this;
    }

    /**
     * Submits an action to this conditional and bounds it to a {@code false} value.
     * <p>
     * The submitted {@link Runnable} is wrapped into an instance of an {@link Action}
     * via an {@link Action#Action(Runnable)} constructor and is managed via API
     * of that {@link Action}. See documentation for {@link Action} for details.
     * @param actionOnFalse action that should be submitted to this conditional and bound to a {@code false} value
     * @return this conditional after submitting an action
     */
    public Conditional onFalse(Runnable actionOnFalse) {
        Action<?> actionOnFalseWrapped = new Action<>(actionOnFalse);
        addToActions(actionOnFalseWrapped, FALSE);
        return this;
    }

    /**
     * Submits an action to this conditional and bounds it to a {@code false} value.
     * @param actionOnFalse action that should be submitted to this conditional and bound to a {@code false} value
     * @param <T> type of value returned in the result of submitted action execution
     * @return this conditional after submitting an action
     */
    public <T> Conditional onFalse(Action<T> actionOnFalse) {
        addToActions(actionOnFalse, FALSE);
        return this;
    }

    /**
     * Submits an action to this conditional and bounds it to a specified value.
     * @param actionToAdd action that should be submitted to this conditional and bound to a specified value
     * @param valueToWhichActionMustBeBoundTo value to which a submitted should be bound to
     * @param <T> type of value returned in the result of submitted action execution
     */
    private <T> void addToActions(Action<T> actionToAdd, boolean valueToWhichActionMustBeBoundTo) {
        ActionsList actionsOnTrue = actionsMap.get(valueToWhichActionMustBeBoundTo);
        actionsOnTrue.add(actionToAdd);
    }

//  <!-- ====================================================================== -->
//  <!--        ACTIONS OPERATIONS                                              -->
//  <!-- ====================================================================== -->

    /**
     * Executes all submitted actions, bound to the value described by this conditional.
     * <ol>
     *     <li>Execution is performed subsequently, starting from the first submitted
     *     action, bound to the value described by this conditional.</li>
     *     <li>Execution is performed via calling an {@link Action#execute()}
     *     method of every executed action. See documentation for that method
     *     for details about execution.</li>
     *     <li>If there are no submitted actions, bound to the value described
     *     by this conditional, then nothing happens: no action is executed,
     *     no exception is thrown.</li>
     *     <li>This method can be called multiple times. However, in case of calling it
     *     more than once be aware of possible side effects caused by a previous call. </li>
     * </ol>
     *
     * @return this conditional after this method call
     * @throws WrapperException if an {@link Exception} during execution of an action was thrown;
     *                          in such case the thrown {@link Exception} is set as a cause of an unchecked {@link WrapperException}
     */
    public Conditional execute() {
        ActionsList actionsForDescribedValue = actionsMap.get(describedValue);
        actionsForDescribedValue.executeAll();
        return this;
    }

    /**
     * Executes the specified amount of cycles all submitted actions,
     * bound to the value described by this conditional.
     * <p>
     * For example, if sequence of relevant actions is [{@code A -> B -> C}]
     * and the specified amount of cycles is 2, then those actions will be executed
     * in the following order: [{@code A -> B -> C -> A -> B -> C}].
     * <p>
     * For details see documentation for {@link Conditional#execute()}.
     *
     * @param cyclesToExecute the amount of cycles relevant actions should be executed;
     *                        if value of the passed argument is 0 or less, then nothing happens: no action
     *                        is executed, no exception is thrown
     * @return this conditional after this method call
     * @throws WrapperException if an {@link Exception} during execution of an action was thrown;
     *                          in such case the thrown {@link Exception} is set as a cause of an unchecked {@link WrapperException}
     */
    public Conditional execute(int cyclesToExecute) {
        ActionsList actionsForDescribedValue = actionsMap.get(describedValue);
        IntStream.range(0, cyclesToExecute).forEach(index -> actionsForDescribedValue.executeAll());
        return this;
    }

    /**
     * Executes the submitted action if the passed boolean value is {@code true}.
     * If the passed boolean value is {@code false}, does nothing.
     * @param conditionThatMustBeTrue condition that must be {@code true} in order for
     *                                the passed action to be executed
     * @param actionToExecute action to execute if the passed boolean value is {@code true}
     * @throws WrapperException if an {@link Exception} during execution of an action was thrown;
     *                          in such case the thrown {@link Exception} is set as a cause of an unchecked {@link WrapperException}
     */
    public static void onTrueExecute(boolean conditionThatMustBeTrue, Runnable actionToExecute) {
        conditional(conditionThatMustBeTrue)
                .onTrue(actionToExecute)
                .execute();
    }

    /**
     * Executes the submitted action if the passed boolean value is {@code false}.
     * If the passed boolean value is {@code true}, does nothing.
     * @param conditionThatMustBeFalse condition that must be {@code false} in order for
     *                                the passed action to be executed
     * @param actionToExecute action to execute if the passed boolean value is {@code false}
     * @throws WrapperException if an {@link Exception} during execution of an action was thrown;
     *                          in such case the thrown {@link Exception} is set as a cause of an unchecked {@link WrapperException}
     */
    public static void onFalseExecute(boolean conditionThatMustBeFalse, Runnable actionToExecute) {
        conditional(conditionThatMustBeFalse)
                .onFalse(actionToExecute)
                .execute();
    }

    /**
     * Executes a unary action submitted to this conditional and bound to the
     * value described by this conditional. After that, returns a return value
     * that is produced in the result of execution of the action, but cast into
     * a specified type.
     * <ol>
     *     <li>For successful execution of this method exactly one action should
     *     be submitted to this conditional and be bound to the value described by
     *     this conditional.</li>
     *     <li>For details on the action execution see documentation for
     *     {@link Conditional#execute()}, respectively adjusted to the unary
     *     character of this method.</li>
     *     <li>This method can be called multiple times. However, in case
     *     of calling it more than once be aware of possible side effects
     *     caused by a previous call.</li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <T>}) to which
     *                               the return value will be cast into
     * @param <T> type to which the return value will be cast into
     * @return return value that is produced in the result of execution of a unary
     * action submitted to this conditional and bound to the value described by this conditional;
     * the produced value is cast into a specified type ({@code typeToGet}); if {@code null} is produced,
     * then {@code null} is returned regardless of the passed {@code typeToGet}; if the executed action
     * was submitted with the use of {@link Runnable} ({@link Conditional#onTrue(Runnable)},
     * {@link Conditional#onFalse(Runnable)}), then {@code null} is always returned
     * @throws MismatchedReturnTypeException if a return value cannot be cast into a
     * specified type ({@code typeToGet}) due to {@link ClassCastException}
     * @throws WrapperException if an {@link Exception} during execution of the action was thrown;
     * in such case the thrown {@link Exception} is set as a cause of an unchecked {@link WrapperException}
     * @throws UndeterminedReturnValueException if not exactly one action was submitted
     * to this conditional and was bound to the value described by this conditional
     */
    public <T> T get(Class<T> typeToGet) {
        rejectIfNotExactlyOneActionInDescribedCollection();
        ActionsList action = actionsMap.get(describedValue);
        Action<?> unaryAction = action.getFirst();
        return unaryAction.get(typeToGet);
    }

    /**
     * Removes all actions submitted to this conditional.
     * This conditional will not contain any actions after this method returns.
     * @return this conditional after this method call
     */
    public Conditional discardAllActions() {
        this.discardActionsOnTrue();
        this.discardActionsOnFalse();
        return this;
    }

    /**
     * Removes all actions submitted to this conditional and bound to a {@code true} value.
     * This conditional will not contain any actions bound to a {@code true} value after this method returns.
     * @return this conditional after this method call
     */
    public Conditional discardActionsOnTrue() {
        ActionsList actionsOnTrue = actionsMap.get(TRUE);
        actionsOnTrue.clear();
        return this;
    }

    /**
     * Removes all actions submitted to this conditional and bound to a {@code false} value.
     * This conditional will not contain any actions bound to a {@code false} value after this method returns.
     * @return this conditional after this method call
     */
    public Conditional discardActionsOnFalse() {
        ActionsList actionsOnFalse = actionsMap.get(FALSE);
        actionsOnFalse.clear();
        return this;
    }

    /**
     * Assures that exactly one action was submitted to this conditional
     * and is bound to the value described by this conditional
     * @throws UndeterminedReturnValueException if not exactly one action was submitted
     * to this conditional and was bound to the value described by this conditional
     */
    private void rejectIfNotExactlyOneActionInDescribedCollection() {
        String exceptionMessage = "To use a get(...) method for a given Conditional, exactly one " +
                                  "action must be submitted. This condition hasn't been met";
        ActionsList actionsForDescribedValue = actionsMap.get(describedValue);
        boolean isExactlyOneActionInDescribedCollection =
                actionsForDescribedValue.isExactlyOneActionInList();
        isTrueOrThrow(isExactlyOneActionInDescribedCollection,
                new UndeterminedReturnValueException(exceptionMessage));
    }

//  <!-- ====================================================================== -->
//  <!--        EXCEPTIONS                                                      -->
//  <!-- ====================================================================== -->

    /**
     * Submits an action to this conditional that throws the passed {@link Exception} wrapped
     * into an unchecked {@link WrapperException}. The action is bound to a {@code true} value.
     * <p>
     * The submitted action isn't prioritized in relation to other actions, particularly in relation
     * to previously submitted actions. It means that regardless of an action submitted via this
     * method, actions execution order remains usual: execution is performed subsequently,
     * starting from the first submitted action, bound to the value described by this conditional.
     * @param exceptionToThrow exception that will be wrapped into an unchecked {@link WrapperException},
     * which, in turn, if a submitted action is executed
     * @param <T> type of the passed exception                         
     * @return this conditional after submitting an action
     */
    public <T extends Exception> Conditional onTrueThrow(T exceptionToThrow) {
        Callable<?> callableWithException = () -> {
            throw exceptionToThrow;
        };
        return onTrue(callableWithException);
    }

    /**
     * Submits an action to this conditional that throws the passed {@link Exception} wrapped
     * into an unchecked {@link WrapperException}. The action is bound to a {@code false} value.
     * <p>
     * The submitted action isn't prioritized in relation to other actions, particularly in relation
     * to previously submitted actions. It means that regardless of an action submitted via this
     * method, actions execution order remains usual: execution is performed subsequently,
     * starting from the first submitted action, bound to the value described by this conditional.
     * @param exceptionToThrow exception that will be wrapped into an unchecked {@link WrapperException},
     * which, in turn, if a submitted action is executed
     * @param <T> type of the passed exception
     * @return this conditional after submitting an action
     */
    public <T extends Exception> Conditional onFalseThrow(T exceptionToThrow) {
        Callable<?> callableWithException = () -> {
            throw exceptionToThrow;
        };
        return onFalse(callableWithException);
    }

    /**
     * Assures that the passed boolean value is {@code true}.
     * <p>
     * Throws the passed {@link RuntimeException} if the passed boolean value is {@code false}.
     * Does nothing if the passed boolean value is {@code true}.
     * @param conditionThatMustBeTrue boolean value that is supposed to be {@code true} 
     * @param exceptionToThrow if the passed boolean value is {@code false}
     * @param <T> type of the passed exception
     * @throws T if the passed boolean value is {@code false}
     */
    public static <T extends RuntimeException>
    void isTrueOrThrow(boolean conditionThatMustBeTrue, T exceptionToThrow) throws T {
        Conditional conditional = conditional(!conditionThatMustBeTrue);
        ExceptionThrower exceptionThrower = conditional.uncheckedThrower();
        exceptionThrower.throwUncheckedIfActive(exceptionToThrow);
    }

    /**
     * Assures that the passed boolean value is {@code true}.
     * <p>
     * Throws the passed {@link Exception} if the passed boolean value is {@code false}.
     * Does nothing if the passed boolean value is {@code true}.
     * @param conditionThatMustBeTrue boolean value that is supposed to be {@code true}
     * @param exceptionToThrow if the passed boolean value is {@code false}
     * @param <T> type of the passed exception
     * @throws T if the passed boolean value is {@code false}
     */
    public static <T extends Exception> 
    void isTrueOrThrow(boolean conditionThatMustBeTrue, T exceptionToThrow) throws T {
        Conditional conditional = conditional(!conditionThatMustBeTrue);
        ExceptionThrower exceptionThrower = conditional.checkedThrower();
        exceptionThrower.throwCheckedIfActive(exceptionToThrow);
    }

    /**
     * Assures that the passed boolean value is {@code false}.
     * <p>
     * Throws the passed {@link RuntimeException} if the passed boolean value is {@code true}.
     * Does nothing if the passed boolean value is {@code false}.
     * @param conditionThatMustBeFalse boolean value that is supposed to be {@code false}
     * @param exceptionToThrow if the passed boolean value is {@code true}
     * @param <T> type of the passed exception                        
     * @throws T if the passed boolean value is {@code true}
     */
    public static <T extends RuntimeException> 
    void isFalseOrThrow(boolean conditionThatMustBeFalse, T exceptionToThrow) throws T {
        Conditional conditional = conditional(conditionThatMustBeFalse);
        ExceptionThrower exceptionThrower = conditional.uncheckedThrower();
        exceptionThrower.throwUncheckedIfActive(exceptionToThrow);
    }

    /**
     * Assures that the passed boolean value is {@code false}.
     * <p>
     * Throws the passed {@link Exception} if the passed boolean value is {@code true}.
     * Does nothing if the passed boolean value is {@code false}.
     * @param conditionThatMustBeFalse boolean value that is supposed to be {@code false}
     * @param exceptionToThrow if the passed boolean value is {@code true}
     * @param <T> type of the passed exception                        
     * @throws T if the passed boolean value is {@code true}
     */
    public static <T extends Exception>
    void isFalseOrThrow(boolean conditionThatMustBeFalse, T exceptionToThrow) throws T {
        Conditional conditional = conditional(conditionThatMustBeFalse);
        ExceptionThrower exceptionThrower = conditional.checkedThrower();
        exceptionThrower.throwCheckedIfActive(exceptionToThrow);
    }

    private ExceptionThrower checkedThrower() {
        Map<Boolean, ExceptionThrower> checkedThrowers = new HashMap<>();
        checkedThrowers.put(TRUE, new ActiveCheckedThrower());
        checkedThrowers.put(FALSE, new VoidCheckedThrower());
        return checkedThrowers.get(describedValue);
    }

    private ExceptionThrower uncheckedThrower() {
        Map<Boolean, ExceptionThrower> checkedThrowers = new HashMap<>();
        checkedThrowers.put(TRUE, new ActiveUncheckedThrower());
        checkedThrowers.put(FALSE, new VoidUncheckedThrower());
        return checkedThrowers.get(describedValue);
    }
}
