package eu.ciechanowiec.conditional;

import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
 * <li>Every submitted action is bound either to a {@code true} or
 * {@code false} boolean value, depending on the used submission method.</li>
 * <li>Submitted actions are supposed to be executed in a void manner <i>or</i> to be executed and return
 * a value in the result of that execution. To achieve that, usage of {@link Conditional} is finalized via
 * an {@code execute(...)} or {@code get(...)} methods respectively, that trigger actions
 * bound to a value described by a given instance of {@link Conditional}.</li>
 * <li>{@link Conditional} is lazy, which means that respective submitted actions will be triggered when
 * and only when an {@code execute(...)} or {@code get(...)} methods are called,
 * hence mere submission of an action doesn't suffice to trigger that action.</li>
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
*      // Output:
 *     // Checked number is even
 * }}</pre>
 * <i>Usage example with action returning a value:</i>
 * <pre>{@code
 * import static eu.ciechanowiec.conditional.Conditional.conditional;
 *
 * public static void main(String[] args) {
 *     String evenOrOdd = evenOrOdd(10);
 *     System.out.println("Result: " + evenOrOdd);
 *     // Output:
 *     // Returning a value on true...
 *     // Result: Even!
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
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "ClassWithTooManyMethods"})
public final class Conditional {

    private final boolean describedValue;
    private final Map<Boolean, ActionsList> actionsMap;

//  <!-- ====================================================================== -->
//  <!--        CREATION                                                        -->
//  <!-- ====================================================================== -->

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
    @Nonnull
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
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public boolean describedValue() {
        return describedValue;
    }

    /**
     * Answers whether this conditional describes a {@code true} value.
     * @return {@code true} if this conditional describes a {@code true} value;
     *         {@code false} otherwise
     */
    public boolean isTrue() {
        return describedValue == TRUE;
    }

    /**
     * Answers whether this conditional describes a {@code false} value.
     * @return {@code true} if this conditional describes a {@code false} value;
     *         {@code false} otherwise
     */
    public boolean isFalse() {
        return describedValue == FALSE;
    }

    /**
     * Retrieves by reference an {@link ActionsList} that stores all actions
     * submitted to this conditional and bound to a {@code true} value.
     * @return {@link ActionsList} (by reference) that stores all actions
     *          submitted to this conditional and bound to a {@code true} value
     */
    @Nonnull
    public ActionsList actionsOnTrue() {
        return actionsMap.get(TRUE);
    }

    /**
     * Retrieves by reference an {@link ActionsList} that stores all actions
     * submitted to this conditional and bound to a {@code false} value.
     * @return {@link ActionsList} (by reference) that stores all actions
     *         submitted to this conditional and bound to a {@code false} value
     */
    @Nonnull
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
    @Nonnull
    public <T> Conditional onTrue(@Nonnull Callable<T> actionOnTrue) {
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
    @Nonnull
    public Conditional onTrue(@Nonnull Runnable actionOnTrue) {
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
    @Nonnull
    public <T> Conditional onTrue(@Nonnull Action<T> actionOnTrue) {
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
    @Nonnull
    public <T> Conditional onFalse(@Nonnull Callable<T> actionOnFalse) {
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
    @Nonnull
    public Conditional onFalse(@Nonnull Runnable actionOnFalse) {
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
    @Nonnull
    public <T> Conditional onFalse(@Nonnull Action<T> actionOnFalse) {
        addToActions(actionOnFalse, FALSE);
        return this;
    }

    /**
     * Submits an action to this conditional and bounds it to a specified value.
     * @param actionToAdd action that should be submitted to this conditional and bound to a specified value
     * @param valueToWhichActionMustBeBoundTo value to which a submitted action should be bound to
     * @param <T> type of value returned in the result of submitted action execution
     */
    private <T> void addToActions(Action<T> actionToAdd, boolean valueToWhichActionMustBeBoundTo) {
        ActionsList actionsOnTrue = actionsMap.get(valueToWhichActionMustBeBoundTo);
        actionsOnTrue.add(actionToAdd);
    }

//  <!-- ====================================================================== -->
//  <!--        EXECUTION OPERATIONS - USUAL                                    -->
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
     *     more than once side effects caused by a previous call are possible.</li>
     * </ol>
     * @return this conditional after this method call
     * @throws Exception if an {@link Exception} during execution of an action was thrown;
     *         note that the {@link Exception} isn't specified in a method declaration in
     *         a {@code throws...} clause in order to avoid enforcing that {@link Exception}
     *         handling (omitting of specifying the {@link Exception} in the method
     *         declaration is achieved via {@link SneakyThrows} on the underlying action)
     */
    @Nonnull
    @SuppressWarnings("JavadocDeclaration")
    public Conditional execute() {
        ActionsList actionsForDescribedValue = actionsMap.get(describedValue);
        actionsForDescribedValue.executeAll();
        return this;
    }

    /**
     * Executes the specified amount of cycles all submitted actions,
     * bound to the value described by this conditional. For example, if sequence of relevant
     * actions is [{@code A -> B -> C}] and the specified amount of cycles is 2, then those
     * actions will be executed in the following order: [{@code A -> B -> C -> A -> B -> C}].
     * <p>
     * For details on execution see documentation for {@link Conditional#execute()}.
     * @param cyclesToExecute the amount of cycles that relevant actions should be executed;
     *                        if value of the passed argument is {@code 0} or less, then nothing
     *                        happens: no action is executed, no exception is thrown
     * @return this conditional after this method call
     * @throws Exception if an {@link Exception} during execution of an action was thrown;
     *         note that the {@link Exception} isn't specified in a method declaration in
     *         a {@code throws...} clause in order to avoid enforcing that {@link Exception}
     *         handling (omitting of specifying the {@link Exception} in the method
     *         declaration is achieved via {@link SneakyThrows} on the underlying action)
     */
    @Nonnull
    @SuppressWarnings("JavadocDeclaration")
    public Conditional execute(int cyclesToExecute) {
        ActionsList actionsForDescribedValue = actionsMap.get(describedValue);
        IntStream.range(0, cyclesToExecute).forEach(index -> actionsForDescribedValue.executeAll());
        return this;
    }

    /**
     * Executes all submitted actions, bound to the value described by this conditional,
     * and sets the passed exception class as a unary element of an exception list
     * belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling an
     *     {@link Conditional#execute()} method. Therefore, for details on
     *     the execution see documentation for {@link Conditional#execute()}.</li>
     *     <li>The only relevant difference between this method and an {@link Conditional#execute()}
     *     method is that this method declaration has an exception list ({@code throws...} clause)
     *     with one element inside, which is the passed exception class. The reason behind such
     *     solution is that an {@link Conditional#execute()} method doesn't have a {@code throws...}
     *     clause, although it is capable of throwing an {@link Exception} (the clause is omitted
     *     via {@link SneakyThrows} on the underlying action), which allows to avoid enforcing of
     *     {@link Exception} handling. In cases, where enforcing of such handling is, however,
     *     required, this method can be used.</li>
     *     <li>Consider the following conditional. Upon execution, it always throws an
     *     {@link java.io.IOException}, which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Conditional trueConditional = conditional(true)
     *             .onTrue(() -> {
     *                 throw new IOException();
     *             });
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     trueConditional.execute(); // Doesn't enforce exception handling
     *
     *     try {
     *         trueConditional.execute(IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param expectedException class representing an exception class that will be set as a unary
     *                          element of an exception list belonging to this method declaration,
     *                          i.e. as a unary element of the {@code throws...} clause
     * @return this conditional after this method call
     * @param <X> an exception class that will be set as a unary
     *            element of an exception list belonging to this method declaration,
     *            i.e. as a unary element of the {@code throws...} clause
     * @throws X if an {@link Exception} of {@code X} type during execution of an action was thrown
     */
    @Nonnull
    @SuppressWarnings({"unused", "RedundantThrows", "squid:S1130"})
    public <X extends Exception>
    Conditional execute(@Nullable Class<X> expectedException)
    throws X {
        execute();
        return this;
    }

    /**
     * Executes all submitted actions, bound to the value described by this conditional,
     * and respectively sets the passed exception classes as elements of an exception
     * list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling an
     *     {@link Conditional#execute()} method. Therefore, for details on
     *     the execution see documentation for {@link Conditional#execute()}.</li>
     *     <li>The only relevant difference between this method and an {@link Conditional#execute()}
     *     method is that this method declaration has an exception list ({@code throws...} clause),
     *     which is respectively defined by the passed exception classes. The reason behind such
     *     solution is that an {@link Conditional#execute()} method doesn't have a {@code throws...}
     *     clause, although it is capable of throwing an {@link Exception} (the clause is omitted
     *     via {@link SneakyThrows} on the underlying action), which allows to avoid enforcing of
     *     {@link Exception} handling. In cases, where enforcing of such handling is, however,
     *     required, this method can be used.</li>
     *     <li>Consider the following conditional. Upon execution, it always throws an
     *     {@link java.io.IOException}, which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Conditional trueConditional = conditional(true)
     *             .onTrue(() -> {
     *                 throw new IOException();
     *             });
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     trueConditional.execute(); // Doesn't enforce exception handling
     *
     *     try {
     *         trueConditional.execute(IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param expectedExceptionOne class representing an exception class that will be set as a first
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a first element of the {@code throws...} clause
     * @param expectedExceptionTwo class representing an exception class that will be set as a second
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a second element of the {@code throws...} clause
     * @return this conditional after this method call
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of an action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of an action was thrown
     */
    @Nonnull
    @SuppressWarnings({"unused", "RedundantThrows", "squid:S1130"})
    public <X1 extends Exception, X2 extends Exception>
    Conditional execute(@Nullable Class<X1> expectedExceptionOne, @Nullable Class<X2> expectedExceptionTwo)
    throws X1, X2 {
        execute();
        return this;
    }

    /**
     * Executes all submitted actions, bound to the value described by this conditional,
     * and respectively sets the passed exception classes as elements of an exception
     * list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling an
     *     {@link Conditional#execute()} method. Therefore, for details on
     *     the execution see documentation for {@link Conditional#execute()}.</li>
     *     <li>The only relevant difference between this method and an {@link Conditional#execute()}
     *     method is that this method declaration has an exception list ({@code throws...} clause),
     *     which is respectively defined by the passed exception classes. The reason behind such
     *     solution is that an {@link Conditional#execute()} method doesn't have a {@code throws...}
     *     clause, although it is capable of throwing an {@link Exception} (the clause is omitted
     *     via {@link SneakyThrows} on the underlying action), which allows to avoid enforcing of
     *     {@link Exception} handling. In cases, where enforcing of such handling is, however,
     *     required, this method can be used.</li>
     *     <li>Consider the following conditional. Upon execution, it always throws an
     *     {@link java.io.IOException}, which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Conditional trueConditional = conditional(true)
     *             .onTrue(() -> {
     *                 throw new IOException();
     *             });
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     trueConditional.execute(); // Doesn't enforce exception handling
     *
     *     try {
     *         trueConditional.execute(IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param expectedExceptionOne class representing an exception class that will be set as a first
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a first element of the {@code throws...} clause
     * @param expectedExceptionTwo class representing an exception class that will be set as a second
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a second element of the {@code throws...} clause
     * @param expectedExceptionThree class representing an exception class that will be set as a third
     *                               element of an exception list belonging to this method declaration,
     *                               i.e. as a third element of the {@code throws...} clause
     * @return this conditional after this method call
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @param <X3> an exception class that will be set as a third
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a third element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of an action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of an action was thrown
     * @throws X3 if an {@link Exception} of {@code X3} type during execution of an action was thrown
     */
    @Nonnull
    @SuppressWarnings({"unused", "RedundantThrows", "squid:S1130"})
    public <X1 extends Exception, X2 extends Exception, X3 extends Exception>
    Conditional execute(@Nullable Class<X1> expectedExceptionOne, @Nullable Class<X2> expectedExceptionTwo,
                        @Nullable Class<X3> expectedExceptionThree)
    throws X1, X2, X3 {
        execute();
        return this;
    }

    /**
     * Executes all submitted actions, bound to the value described by this conditional,
     * and respectively sets the passed exception classes as elements of an exception
     * list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling an
     *     {@link Conditional#execute()} method. Therefore, for details on
     *     the execution see documentation for {@link Conditional#execute()}.</li>
     *     <li>The only relevant difference between this method and an {@link Conditional#execute()}
     *     method is that this method declaration has an exception list ({@code throws...} clause),
     *     which is respectively defined by the passed exception classes. The reason behind such
     *     solution is that an {@link Conditional#execute()} method doesn't have a {@code throws...}
     *     clause, although it is capable of throwing an {@link Exception} (the clause is omitted
     *     via {@link SneakyThrows} on the underlying action), which allows to avoid enforcing of
     *     {@link Exception} handling. In cases, where enforcing of such handling is, however,
     *     required, this method can be used.</li>
     *     <li>Consider the following conditional. Upon execution, it always throws an
     *     {@link java.io.IOException}, which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Conditional trueConditional = conditional(true)
     *             .onTrue(() -> {
     *                 throw new IOException();
     *             });
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     trueConditional.execute(); // Doesn't enforce exception handling
     *
     *     try {
     *         trueConditional.execute(IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param expectedExceptionOne class representing an exception class that will be set as a first
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a first element of the {@code throws...} clause
     * @param expectedExceptionTwo class representing an exception class that will be set as a second
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a second element of the {@code throws...} clause
     * @param expectedExceptionThree class representing an exception class that will be set as a third
     *                               element of an exception list belonging to this method declaration,
     *                               i.e. as a third element of the {@code throws...} clause
     * @param expectedExceptionFour class representing an exception class that will be set as a fourth
     *                              element of an exception list belonging to this method declaration,
     *                              i.e. as a fourth element of the {@code throws...} clause
     * @return this conditional after this method call
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @param <X3> an exception class that will be set as a third
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a third element of the {@code throws...} clause
     * @param <X4> an exception class that will be set as a fourth
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a fourth element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of an action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of an action was thrown
     * @throws X3 if an {@link Exception} of {@code X3} type during execution of an action was thrown
     * @throws X4 if an {@link Exception} of {@code X4} type during execution of an action was thrown
     */
    @Nonnull
    @SuppressWarnings({"unused", "RedundantThrows", "squid:S1130"})
    public <X1 extends Exception, X2 extends Exception, X3 extends Exception, X4 extends Exception>
    Conditional execute(@Nullable Class<X1> expectedExceptionOne, @Nullable Class<X2> expectedExceptionTwo,
                        @Nullable Class<X3> expectedExceptionThree, @Nullable Class<X4> expectedExceptionFour)
    throws X1, X2, X3, X4 {
        execute();
        return this;
    }

//  <!-- ====================================================================== -->
//  <!--        EXECUTION OPERATIONS - STATIC                                   -->
//  <!-- ====================================================================== -->

    /**
     * Executes the submitted action if the passed boolean value is {@code true}.
     * If the passed boolean value is {@code false}, does nothing.
     * @param conditionThatMustBeTrue condition that must be {@code true} in order for
     *                                the passed action to be executed
     * @param actionToExecute action to execute if the passed boolean value is {@code true}
     * @throws Exception if an {@link Exception} during execution of an action was thrown;
     *         note that the {@link Exception} isn't specified in a method declaration in
     *         a {@code throws...} clause in order to avoid enforcing that {@link Exception}
     *         handling (omitting of specifying the {@link Exception} in the method
     *         declaration is achieved via {@link SneakyThrows} on the underlying action)
     */
    @SuppressWarnings("JavadocDeclaration")
    public static void onTrueExecute(boolean conditionThatMustBeTrue, @Nonnull Runnable actionToExecute) {
        conditional(conditionThatMustBeTrue)
                .onTrue(actionToExecute)
                .execute();
    }

    /**
     * Executes the submitted action if the passed boolean value is {@code true}.
     * If the passed boolean value is {@code false}, does nothing.
     * <ol>
     *     <li>The internal logic of this method is exactly the same as for an
     *     {@link Conditional#onTrueExecute(boolean, Runnable)} method.</li>
     *     <li>The only relevant difference between this method and an
     *     {@link Conditional#onTrueExecute(boolean, Runnable)} method is that this method declaration
     *     has an exception list ({@code throws...} clause) with one element inside, which is the passed
     *     exception class. The reason behind such solution is that an {@link Conditional#onTrueExecute(boolean, Runnable)}
     *     method doesn't have a {@code throws...} clause, although it is capable of throwing an
     *     {@link Exception} (the clause is omitted via {@link SneakyThrows} on the underlying action),
     *     which allows to avoid enforcing of {@link Exception} handling. In cases, where enforcing
     *     of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException},
     *     which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Runnable action = () -> {
     *         throw new IOException();
     *     };
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     onTrueExecute(true, action); // Doesn't enforce exception handling
     *
     *     try {
     *         onTrueExecute(true, action, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param conditionThatMustBeTrue condition that must be {@code true} in order for
     *                                the passed action to be executed
     * @param actionToExecute action to execute if the passed boolean value is {@code true}
     * @param expectedException class representing an exception class that will be set as a unary
     *                          element of an exception list belonging to this method declaration,
     *                          i.e. as a unary element of the {@code throws...} clause                        
     * @param <X> an exception class that will be set as a unary
     *            element of an exception list belonging to this method declaration,
     *            i.e. as a unary element of the {@code throws...} clause
     * @throws X if an {@link Exception} of {@code X} type during execution of an action was thrown
     */
    @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "unused", "RedundantThrows", "squid:S1130"})
    public static <X extends Exception> void onTrueExecute
    (boolean conditionThatMustBeTrue, @Nonnull Runnable actionToExecute, @Nullable Class<X> expectedException)
    throws X {
        conditional(conditionThatMustBeTrue)
                .onTrue(actionToExecute)
                .execute();
    }

    /**
     * Executes the submitted action if the passed boolean value is {@code false}.
     * If the passed boolean value is {@code true}, does nothing.
     * @param conditionThatMustBeFalse condition that must be {@code false} in order for
     *                                 the passed action to be executed
     * @param actionToExecute action to execute if the passed boolean value is {@code false}
     * @throws Exception if an {@link Exception} during execution of an action was thrown;
     *         note that the {@link Exception} isn't specified in a method declaration in
     *         a {@code throws...} clause in order to avoid enforcing that {@link Exception}
     *         handling (omitting of specifying the {@link Exception} in the method
     *         declaration is achieved via {@link SneakyThrows} on the underlying action)
     */
    @SuppressWarnings("JavadocDeclaration")
    public static void onFalseExecute(boolean conditionThatMustBeFalse, @Nonnull Runnable actionToExecute) {
        conditional(conditionThatMustBeFalse)
                .onFalse(actionToExecute)
                .execute();
    }

    /**
     * Executes the submitted action if the passed boolean value is {@code false}.
     * If the passed boolean value is {@code true}, does nothing.
     * <ol>
     *     <li>The internal logic of this method is exactly the same as for an
     *     {@link Conditional#onFalseExecute(boolean, Runnable)} method.</li>
     *     <li>The only relevant difference between this method and an
     *     {@link Conditional#onFalseExecute(boolean, Runnable)} method is that this method declaration
     *     has an exception list ({@code throws...} clause) with one element inside, which is the passed
     *     exception class. The reason behind such solution is that an {@link Conditional#onFalseExecute(boolean, Runnable)}
     *     method doesn't have a {@code throws...} clause, although it is capable of throwing an
     *     {@link Exception} (the clause is omitted via {@link SneakyThrows} on the underlying action),
     *     which allows to avoid enforcing of {@link Exception} handling. In cases, where enforcing
     *     of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException},
     *     which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Runnable action = () -> {
     *         throw new IOException();
     *     };
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     onFalseExecute(false, action); // Doesn't enforce exception handling
     *
     *     try {
     *         onFalseExecute(false, action, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param conditionThatMustBeFalse condition that must be {@code false} in order for
     *                                 the passed action to be executed
     * @param actionToExecute action to execute if the passed boolean value is {@code false}
     * @param expectedException class representing an exception class that will be set as a unary
     *                          element of an exception list belonging to this method declaration,
     *                          i.e. as a unary element of the {@code throws...} clause                        
     * @param <X> an exception class that will be set as a unary
     *            element of an exception list belonging to this method declaration,
     *            i.e. as a unary element of the {@code throws...} clause
     * @throws X if an {@link Exception} of {@code X} type during execution of an action was thrown
     */
    @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "unused", "RedundantThrows", "squid:S1130"})
    public static <X extends Exception> void onFalseExecute
    (boolean conditionThatMustBeFalse, @Nonnull Runnable actionToExecute, @Nullable Class<X> expectedException)
    throws X {
        conditional(conditionThatMustBeFalse)
                .onFalse(actionToExecute)
                .execute();
    }

//  <!-- ====================================================================== -->
//  <!--        GET OPERATIONS                                                  -->
//  <!-- ====================================================================== -->

    /**
     * Executes a unary action submitted to this conditional and bound to the
     * value described by this conditional; after that, returns a return value
     * that is produced in the result of execution of the action, but cast into
     * a specified type.
     * <ol>
     *     <li>For successful execution of this method exactly one action should
     *     be submitted to this conditional and be bound to the value described by
     *     this conditional.</li>
     *     <li>For details on the action execution see documentation for
     *     {@link Conditional#execute()}, respectively adjusted to the unary
     *     character of this method.</li>
     *     <li>This method can be called multiple times. However, in case of calling it
     *     more than once side effects caused by a previous call are possible.</li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <T>}) to which
     *                                the return value will be cast into
     * @param <T> type to which the return value will be cast into
     * @return return value that is produced in the result of execution of a unary action submitted
     *         to this conditional and bound to the value described by this conditional;
     *         the produced value is cast into a specified type ({@code typeToGet}); if {@code null}
     *         is produced, then {@code null} is returned regardless of the passed {@code typeToGet};
     *         if the executed action was submitted with the use of {@link Runnable} ({@link Conditional#onTrue(Runnable)},
     *         {@link Conditional#onFalse(Runnable)}), then {@code null} is always returned
     * @throws MismatchedReturnTypeException if a return value cannot be cast into a specified type
     *                                       ({@code typeToGet}) due to {@link ClassCastException}
     * @throws UndeterminedReturnValueException if not exactly one action was submitted to this conditional
     *                                          and was bound to the value described by this conditional
     * @throws Exception if an {@link Exception} different from the described above during execution of
     *         an action was thrown; note that the {@link Exception} isn't specified in a method
     *         declaration in a {@code throws...} clause in order to avoid enforcing that
     *         {@link Exception} handling (omitting of specifying the {@link Exception} in the
     *         method declaration is achieved via {@link SneakyThrows} on the underlying action)
     */
    @Nullable
    @SuppressWarnings("JavadocDeclaration")
    public <T> T get(@Nonnull Class<T> typeToGet) {
        rejectIfNotExactlyOneActionInDescribedCollection();
        ActionsList action = actionsMap.get(describedValue);
        Action<?> unaryAction = action.getFirst();
        return unaryAction.get(typeToGet);
    }

    /**
     * Executes a unary action submitted to this conditional and bound to the
     * value described by this conditional; after that, returns a return value
     * that is produced in the result of execution of the action, but cast into
     * a specified type.
     * <p>
     * The passed exception class is set as a unary element of an exception list
     * belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling a
     *     {@link Conditional#get(Class)} method. Therefore, for details on
     *     the execution see documentation for {@link Conditional#get(Class)}.</li>
     *     <li>The only relevant difference between this method and a {@link Conditional#get(Class)}
     *     method is that this method declaration has an exception list ({@code throws...} clause)
     *     with one element inside, which is the passed exception class. The reason behind such
     *     solution is that {@link Conditional#get(Class)} method doesn't have a {@code throws...}
     *     clause, although it is capable of throwing an {@link Exception} (the clause is omitted
     *     via {@link SneakyThrows} on the underlying action), which allows to avoid enforcing of
     *     {@link Exception} handling. In cases, where enforcing of such handling is, however,
     *     required, this method can be used.</li>
     *     <li>Consider the following conditional. Upon execution, it always throws an
     *     {@link java.io.IOException}, which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Conditional trueConditional = conditional(true)
     *             .onTrue(() -> {
     *                 throw new IOException();
     *             });
     *     }</pre>
     *     However, the first get method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     String resultOne = trueConditional.get(String.class); // Doesn't enforce exception handling
     *
     *     try {
     *         String resultTwo = trueConditional.get(String.class, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <T>}) to which
     *                                the return value will be cast into
     * @param expectedException class representing an exception class that will be set as a unary
     *                          element of an exception list belonging to this method declaration,
     *                          i.e. as a unary element of the {@code throws...} clause
     * @return return value that is produced in the result of execution of a unary action submitted
     *         to this conditional and bound to the value described by this conditional;
     *         the produced value is cast into a specified type ({@code typeToGet}); if {@code null}
     *         is produced, then {@code null} is returned regardless of the passed {@code typeToGet};
     *         if the executed action was submitted with the use of {@link Runnable} ({@link Conditional#onTrue(Runnable)},
     *         {@link Conditional#onFalse(Runnable)}), then {@code null} is always returned
     * @param <T> type to which the return value will be cast into
     * @param <X> an exception class that will be set as a unary
     *            element of an exception list belonging to this method declaration,
     *            i.e. as a unary element of the {@code throws...} clause
     * @throws X if an {@link Exception} of {@code X1} type during execution of an action was thrown
     */
    @Nullable
    @SuppressWarnings({"unused", "RedundantThrows", "squid:S1130"})
    public <T, X extends Exception>
    T get(@Nonnull Class<T> typeToGet,
          @Nullable Class<X> expectedException)
    throws X {
        return get(typeToGet);
    }

    /**
     * Executes a unary action submitted to this conditional and bound to the
     * value described by this conditional; after that, returns a return value
     * that is produced in the result of execution of the action, but cast into
     * a specified type.
     * <p>
     * The passed exception classes are set as elements of an exception list belonging
     * to this method declaration ({@code throws...} clause). If the passed exception
     * class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling a
     *     {@link Conditional#get(Class)} method. Therefore, for details on
     *     the execution see documentation for {@link Conditional#get(Class)}.</li>
     *     <li>The only relevant difference between this method and a {@link Conditional#get(Class)}
     *     method is that this method declaration has an exception list ({@code throws...} clause),
     *     which is respectively defined by the passed exception classes. The reason behind such solution
     *     is that {@link Conditional#get(Class)} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows} on the underlying action), which allows to avoid enforcing of
     *     {@link Exception} handling. In cases, where enforcing of such handling is, however,
     *     required, this method can be used.</li>
     *     <li>Consider the following conditional. Upon execution, it always throws an
     *     {@link java.io.IOException}, which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Conditional trueConditional = conditional(true)
     *             .onTrue(() -> {
     *                 throw new IOException();
     *             });
     *     }</pre>
     *     However, the first get method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     String resultOne = trueConditional.get(String.class); // Doesn't enforce exception handling
     *
     *     try {
     *         String resultTwo = trueConditional.get(String.class, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <T>}) to which
     *                                the return value will be cast into
     * @param expectedExceptionOne class representing an exception class that will be set as a first
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a first element of the {@code throws...} clause
     * @param expectedExceptionTwo class representing an exception class that will be set as a second
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a second element of the {@code throws...} clause
     * @return return value that is produced in the result of execution of a unary action submitted
     *         to this conditional and bound to the value described by this conditional;
     *         the produced value is cast into a specified type ({@code typeToGet}); if {@code null}
     *         is produced, then {@code null} is returned regardless of the passed {@code typeToGet};
     *         if the executed action was submitted with the use of {@link Runnable} ({@link Conditional#onTrue(Runnable)},
     *         {@link Conditional#onFalse(Runnable)}), then {@code null} is always returned
     * @param <T> type to which the return value will be cast into
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of an action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of an action was thrown
     */
    @Nullable
    @SuppressWarnings({"unused", "RedundantThrows", "squid:S1130"})
    public <T, X1 extends Exception, X2 extends Exception>
    T get(@Nonnull Class<T> typeToGet,
          @Nullable Class<X1> expectedExceptionOne,
          @Nullable Class<X2> expectedExceptionTwo)
    throws X1, X2 {
        return get(typeToGet);
    }

    /**
     * Executes a unary action submitted to this conditional and bound to the
     * value described by this conditional; after that, returns a return value
     * that is produced in the result of execution of the action, but cast into
     * a specified type.
     * <p>
     * The passed exception classes are set as elements of an exception list belonging
     * to this method declaration ({@code throws...} clause). If the passed exception
     * class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling a
     *     {@link Conditional#get(Class)} method. Therefore, for details on
     *     the execution see documentation for {@link Conditional#get(Class)}.</li>
     *     <li>The only relevant difference between this method and a {@link Conditional#get(Class)}
     *     method is that this method declaration has an exception list ({@code throws...} clause),
     *     which is respectively defined by the passed exception classes. The reason behind such solution
     *     is that {@link Conditional#get(Class)} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows} on the underlying action), which allows to avoid enforcing of
     *     {@link Exception} handling. In cases, where enforcing of such handling is, however,
     *     required, this method can be used.</li>
     *     <li>Consider the following conditional. Upon execution, it always throws an
     *     {@link java.io.IOException}, which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Conditional trueConditional = conditional(true)
     *             .onTrue(() -> {
     *                 throw new IOException();
     *             });
     *     }</pre>
     *     However, the first get method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     String resultOne = trueConditional.get(String.class); // Doesn't enforce exception handling
     *
     *     try {
     *         String resultTwo = trueConditional.get(String.class, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <T>}) to which
     *                                the return value will be cast into
     * @param expectedExceptionOne class representing an exception class that will be set as a first
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a first element of the {@code throws...} clause
     * @param expectedExceptionTwo class representing an exception class that will be set as a second
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a second element of the {@code throws...} clause
     * @param expectedExceptionThree class representing an exception class that will be set as a third
     *                               element of an exception list belonging to this method declaration,
     *                               i.e. as a third element of the {@code throws...} clause
     * @return return value that is produced in the result of execution of a unary action submitted
     *         to this conditional and bound to the value described by this conditional;
     *         the produced value is cast into a specified type ({@code typeToGet}); if {@code null}
     *         is produced, then {@code null} is returned regardless of the passed {@code typeToGet};
     *         if the executed action was submitted with the use of {@link Runnable} ({@link Conditional#onTrue(Runnable)},
     *         {@link Conditional#onFalse(Runnable)}), then {@code null} is always returned
     * @param <T> type to which the return value will be cast into
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @param <X3> an exception class that will be set as a third
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a third element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of an action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of an action was thrown
     * @throws X3 if an {@link Exception} of {@code X3} type during execution of an action was thrown
     */
    @Nullable
    @SuppressWarnings({"unused", "RedundantThrows", "squid:S1130"})
    public <T, X1 extends Exception, X2 extends Exception, X3 extends Exception>
    T get(@Nonnull Class<T> typeToGet,
          @Nullable Class<X1> expectedExceptionOne,
          @Nullable Class<X2> expectedExceptionTwo,
          @Nullable Class<X3> expectedExceptionThree)
    throws X1, X2, X3 {
        return get(typeToGet);
    }

    /**
     * Executes a unary action submitted to this conditional and bound to the
     * value described by this conditional; after that, returns a return value
     * that is produced in the result of execution of the action, but cast into
     * a specified type.
     * <p>
     * The passed exception classes are set as elements of an exception list belonging
     * to this method declaration ({@code throws...} clause). If the passed exception
     * class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling a
     *     {@link Conditional#get(Class)} method. Therefore, for details on
     *     the execution see documentation for {@link Conditional#get(Class)}.</li>
     *     <li>The only relevant difference between this method and a {@link Conditional#get(Class)}
     *     method is that this method declaration has an exception list ({@code throws...} clause),
     *     which is respectively defined by the passed exception classes. The reason behind such solution
     *     is that {@link Conditional#get(Class)} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows} on the underlying action), which allows to avoid enforcing of
     *     {@link Exception} handling. In cases, where enforcing of such handling is, however,
     *     required, this method can be used.</li>
     *     <li>Consider the following conditional. Upon execution, it always throws an
     *     {@link java.io.IOException}, which is a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Conditional trueConditional = conditional(true)
     *             .onTrue(() -> {
     *                 throw new IOException();
     *             });
     *     }</pre>
     *     However, the first get method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     String resultOne = trueConditional.get(String.class); // Doesn't enforce exception handling
     *
     *     try {
     *         String resultTwo = trueConditional.get(String.class, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }}</pre>
     *     </li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <T>}) to which
     *                                the return value will be cast into
     * @param expectedExceptionOne class representing an exception class that will be set as a first
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a first element of the {@code throws...} clause
     * @param expectedExceptionTwo class representing an exception class that will be set as a second
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a second element of the {@code throws...} clause
     * @param expectedExceptionThree class representing an exception class that will be set as a third
     *                               element of an exception list belonging to this method declaration,
     *                               i.e. as a third element of the {@code throws...} clause
     * @param expectedExceptionFour class representing an exception class that will be set as a fourth
     *                              element of an exception list belonging to this method declaration,
     *                              i.e. as a fourth element of the {@code throws...} clause
     * @return return value that is produced in the result of execution of a unary action submitted
     *         to this conditional and bound to the value described by this conditional;
     *         the produced value is cast into a specified type ({@code typeToGet}); if {@code null}
     *         is produced, then {@code null} is returned regardless of the passed {@code typeToGet};
     *         if the executed action was submitted with the use of {@link Runnable} ({@link Conditional#onTrue(Runnable)},
     *         {@link Conditional#onFalse(Runnable)}), then {@code null} is always returned
     * @param <T> type to which the return value will be cast into
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @param <X3> an exception class that will be set as a third
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a third element of the {@code throws...} clause
     * @param <X4> an exception class that will be set as a fourth
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a fourth element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of an action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of an action was thrown
     * @throws X3 if an {@link Exception} of {@code X3} type during execution of an action was thrown
     * @throws X4 if an {@link Exception} of {@code X4} type during execution of an action was thrown
     */
    @Nullable
    @SuppressWarnings({"unused", "RedundantThrows", "squid:S1130"})
    public <T, X1 extends Exception, X2 extends Exception, X3 extends Exception, X4 extends Exception>
    T get(@Nonnull Class<T> typeToGet,
          @Nullable Class<X1> expectedExceptionOne,
          @Nullable Class<X2> expectedExceptionTwo,
          @Nullable Class<X3> expectedExceptionThree,
          @Nullable Class<X4> expectedExceptionFour)
    throws X1, X2, X3, X4 {
        return get(typeToGet);
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
//  <!--        DISCARD OPERATIONS                                              -->
//  <!-- ====================================================================== -->

    /**
     * Removes all actions submitted to this conditional.
     * This conditional will not contain any actions after this method returns.
     * @return this conditional after this method call
     */
    @Nonnull
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
    @Nonnull
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
    @Nonnull
    public Conditional discardActionsOnFalse() {
        ActionsList actionsOnFalse = actionsMap.get(FALSE);
        actionsOnFalse.clear();
        return this;
    }

//  <!-- ====================================================================== -->
//  <!--        EXCEPTIONS                                                      -->
//  <!-- ====================================================================== -->

    /**
     * Submits an action to this conditional that throws the passed {@link Exception}.
     * The action is bound to a {@code true} value.
     * <p>
     * The submitted action isn't prioritized in relation to other actions, particularly in relation
     * to previously submitted actions. It means that regardless of an action submitted via this
     * method, actions execution order remains usual as described in documentation for
     * {@link Conditional#execute()}: execution is performed subsequently, starting from
     * the first submitted action, bound to the value described by this conditional.
     * @param exceptionToThrow exception that is thrown if a submitted action is executed
     * @param <T> type of the passed exception                         
     * @return this conditional after submitting an action
     */
    @Nonnull
    public <T extends Exception> Conditional onTrueThrow(@Nonnull T exceptionToThrow) {
        Callable<?> callableWithException = () -> {
            throw exceptionToThrow;
        };
        return onTrue(callableWithException);
    }

    /**
     * Submits an action to this conditional that throws the passed {@link Exception}.
     * The action is bound to a {@code false} value.
     * <p>
     * The submitted action isn't prioritized in relation to other actions, particularly in relation
     * to previously submitted actions. It means that regardless of an action submitted via this
     * method, actions execution order remains usual as described in documentation for
     * {@link Conditional#execute()}: execution is performed subsequently, starting from
     * the first submitted action, bound to the value described by this conditional.
     * @param exceptionToThrow exception that is thrown if a submitted action is executed
     * @param <T> type of the passed exception
     * @return this conditional after submitting an action
     */
    @Nonnull
    public <T extends Exception> Conditional onFalseThrow(@Nonnull T exceptionToThrow) {
        Callable<?> callableWithException = () -> {
            throw exceptionToThrow;
        };
        return onFalse(callableWithException);
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
    void isTrueOrThrow(boolean conditionThatMustBeTrue, @Nonnull T exceptionToThrow) throws T {
        Conditional conditional = conditional(!conditionThatMustBeTrue);
        ExceptionThrower exceptionThrower = conditional.getThrower();
        exceptionThrower.throwIfActive(exceptionToThrow);
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
    void isFalseOrThrow(boolean conditionThatMustBeFalse, @Nonnull T exceptionToThrow) throws T {
        Conditional conditional = conditional(conditionThatMustBeFalse);
        ExceptionThrower exceptionThrower = conditional.getThrower();
        exceptionThrower.throwIfActive(exceptionToThrow);
    }

    private ExceptionThrower getThrower() {
        Map<Boolean, ExceptionThrower> checkedThrowers = new HashMap<>();
        checkedThrowers.put(TRUE, new ExceptionThrowerActive());
        checkedThrowers.put(FALSE, new ExceptionThrowerVoid());
        return checkedThrowers.get(describedValue);
    }
}
