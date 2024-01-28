package eu.ciechanowiec.conditional;

import lombok.SneakyThrows;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Entity that can:
 * <ol>
 *     <li>execute a predefined command in a void manner
 *         (see {@code get(...)} methods of this class);</li>
 *     <li>execute a predefined command and return the result of that execution
 *         (see {@code execute(...)} methods of this class).</li>
 * </ol>
 * @param <T> type of value returned in the result of action execution
 */
@SuppressWarnings({"WeakerAccess", "JavadocDeclaration"})
public class Action<T> {

    /**
     * An underlying {@link Callable} instance
     * used as an engine of this action.
     */
    @Nonnull
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
     * @param engine entity used for method calls of a constructed action
     */
    public Action(@Nonnull Callable<T> engine) {
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
     * @param engine entity used for method calls of a constructed action
     */
    public Action(@Nonnull Runnable engine) {
        this.engine = () -> {
            engine.run();
            //noinspection ReturnOfNull
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
     *     of execution of this action is ignored.</li>
     *     <li>This method can be called multiple times. However, in case of calling it
     *     more than once side effects caused by a previous call are possible.</li>
     * </ol>
     * @throws Exception if an {@link Exception} during execution of this action was thrown;
     *         note that the {@link Exception} isn't specified in a method declaration in 
     *         a {@code throws...} clause in order to avoid enforcing that {@link Exception}
     *         handling (omitting of specifying the {@link Exception} in the method 
     *         declaration is achieved via {@link SneakyThrows})
     */
    @SneakyThrows(Exception.class)
    public void execute() {
        engine.call();
    }

    /**
     * Executes this action and sets the passed exception class as a unary element of
     * an exception list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling an {@link Action#execute()} method.
     *     Therefore, for details on the execution see documentation for {@link Action#execute()}.</li>
     *     <li>The only relevant difference between this method and an {@link Action#execute()} method
     *     is that this method declaration has an exception list ({@code throws...} clause) with one
     *     element inside, which is the passed exception class. The reason behind such solution
     *     is that an {@link Action#execute()} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows}), which allows to avoid enforcing of {@link Exception} handling.
     *     In cases, where enforcing of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException}, which is
     *     a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Action<?> action = new Action<>(() -> {
     *          throw new IOException();
     *     });
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     action.execute(); // Doesn't enforce exception handling
     *
     *     try {
     *         action.execute(IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }
     *     }</pre>
     *     </li>
     * </ol>
     * @param expectedException class representing an exception class that will be set as a unary
     *                          element of an exception list belonging to this method declaration,
     *                          i.e. as a unary element of the {@code throws...} clause
     * @param <X> an exception class that will be set as a unary
     *            element of an exception list belonging to this method declaration,
     *            i.e. as a unary element of the {@code throws...} clause
     * @throws X if an {@link Exception} of {@code X} type during execution of this action was thrown
     */
    @SuppressWarnings({"unused", "RedundantThrows"})
    public <X extends Exception>
    void execute(@Nullable Class<X> expectedException)
    throws X {
        execute();
    }

    /**
     * Executes this action and respectively sets the passed exception classes as elements of
     * an exception list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling an {@link Action#execute()} method.
     *     Therefore, for details on the execution see documentation for {@link Action#execute()}.</li>
     *     <li>The only relevant difference between this method and an {@link Action#execute()} method
     *     is that this method declaration has an exception list ({@code throws...} clause), which
     *     is respectively defined by the passed exception classes. The reason behind such solution
     *     is that an {@link Action#execute()} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows}), which allows to avoid enforcing of {@link Exception} handling.
     *     In cases, where enforcing of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException}, which is
     *     a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Action<?> action = new Action<>(() -> {
     *          throw new IOException();
     *     });
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     action.execute(); // Doesn't enforce exception handling
     *
     *     try {
     *         action.execute(IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }
     *     }</pre>
     *     </li>
     * </ol>
     * @param expectedExceptionOne class representing an exception class that will be set as a first
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a first element of the {@code throws...} clause
     * @param expectedExceptionTwo class representing an exception class that will be set as a second
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a second element of the {@code throws...} clause
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of this action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of this action was thrown
     */
    @SuppressWarnings({"unused", "RedundantThrows"})
    public <X1 extends Exception, X2 extends Exception>
    void execute(@Nullable Class<X1> expectedExceptionOne,
                 @Nullable Class<X2> expectedExceptionTwo)
    throws X1, X2 {
        execute();
    }

    /**
     * Executes this action and respectively sets the passed exception classes as elements of
     * an exception list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling an {@link Action#execute()} method.
     *     Therefore, for details on the execution see documentation for {@link Action#execute()}.</li>
     *     <li>The only relevant difference between this method and an {@link Action#execute()} method
     *     is that this method declaration has an exception list ({@code throws...} clause), which
     *     is respectively defined by the passed exception classes. The reason behind such solution
     *     is that an {@link Action#execute()} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows}), which allows to avoid enforcing of {@link Exception} handling.
     *     In cases, where enforcing of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException}, which is
     *     a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Action<?> action = new Action<>(() -> {
     *          throw new IOException();
     *     });
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     action.execute(); // Doesn't enforce exception handling
     *
     *     try {
     *         action.execute(IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }
     *     }</pre>
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
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @param <X3> an exception class that will be set as a third
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a third element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of this action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of this action was thrown
     * @throws X3 if an {@link Exception} of {@code X3} type during execution of this action was thrown
     */
    @SuppressWarnings({"unused", "RedundantThrows"})
    public <X1 extends Exception, X2 extends Exception, X3 extends Exception>
    void execute(@Nullable Class<X1> expectedExceptionOne,
                 @Nullable Class<X2> expectedExceptionTwo,
                 @Nullable Class<X3> expectedExceptionThree)
    throws X1, X2, X3 {
        execute();
    }

    /**
     * Executes this action and respectively sets the passed exception classes as elements of
     * an exception list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling an {@link Action#execute()} method.
     *     Therefore, for details on the execution see documentation for {@link Action#execute()}.</li>
     *     <li>The only relevant difference between this method and an {@link Action#execute()} method
     *     is that this method declaration has an exception list ({@code throws...} clause), which
     *     is respectively defined by the passed exception classes. The reason behind such solution
     *     is that an {@link Action#execute()} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows}), which allows to avoid enforcing of {@link Exception} handling.
     *     In cases, where enforcing of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException}, which is
     *     a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Action<?> action = new Action<>(() -> {
     *          throw new IOException();
     *     });
     *     }</pre>
     *     However, the first execution method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     action.execute(); // Doesn't enforce exception handling
     *
     *     try {
     *         action.execute(IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }
     *     }</pre>
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
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of this action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of this action was thrown
     * @throws X3 if an {@link Exception} of {@code X3} type during execution of this action was thrown
     * @throws X4 if an {@link Exception} of {@code X4} type during execution of this action was thrown
     */
    @SuppressWarnings({"unused", "RedundantThrows"})
    public <X1 extends Exception, X2 extends Exception, X3 extends Exception, X4 extends Exception>
    void execute(@Nullable Class<X1> expectedExceptionOne,
                 @Nullable Class<X2> expectedExceptionTwo,
                 @Nullable Class<X3> expectedExceptionThree,
                 @Nullable Class<X4> expectedExceptionFour)
    throws X1, X2, X3, X4 {
        execute();
    }

    /**
     * Executes this action and returns a return value that is
     * produced in the result of execution of this action.
     * <ol>
     * <li>This method can be called multiple times. However, in case of calling it
     * more than once side effects caused by a previous call are possible.</li>
     * <li>For details on the execution see documentation for {@link Action#execute()}.</li>
     * </ol>
     * @return return value that is produced in the result of execution of this action;
     *         if {@code null} is produced, then {@code null} is returned;
     *         if this action was constructed with the use of {@link Runnable}, then {@code null} is always returned
     * @throws Exception if an {@link Exception} during execution of this action was thrown;
     *         note that the {@link Exception} isn't specified in a method declaration in 
     *         a {@code throws...} clause in order to avoid enforcing that {@link Exception}
     *         handling (omitting of specifying the {@link Exception} in the method 
     *         declaration is achieved via {@link SneakyThrows})
     */
    @Nullable
    @SneakyThrows(Exception.class)
    @SuppressWarnings("unused")
    public T get() {
        return engine.call();
    }

    /**
     * Executes this action and returns a return value that is produced in
     * the result of execution of this action, but cast into a specified type.
     * <ol>
     * <li>This method can be called multiple times. However, in case of calling it
     * more than once side effects caused by a previous call are possible.</li>
     * <li>For details on the execution see documentation for {@link Action#execute()}.</li>
     *     
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
     *         cast into a specified type ({@code typeToGet}); if {@code null} is produced, then
     *         {@code null} is returned regardless of the passed {@code typeToGet}; if this action
     *         was constructed with the use of {@link Runnable}, then {@code null} is always returned
     * @throws MismatchedReturnTypeException if a return value cannot be cast into a specified type 
     *                                       ({@code typeToGet}) due to {@link ClassCastException}
     * @throws Exception if an {@link Exception} during execution of this action was thrown;
     *         note that the {@link Exception} isn't specified in a method declaration in 
     *         a {@code throws...} clause in order to avoid enforcing that {@link Exception}
     *         handling (omitting of specifying the {@link Exception} in the method
     *         declaration is achieved via {@link SneakyThrows})
     */
    @Nullable
    @SneakyThrows(Exception.class)
    public <S> S get(@Nonnull Class<S> typeToGet) {
        T resultOfActionCall = null;
        try {
            resultOfActionCall = engine.call();
            return typeToGet.cast(resultOfActionCall);
        } catch (@SuppressWarnings("squid:S1166") ClassCastException exception) {
            T resultOfActionCallNotNull = Objects.requireNonNull(resultOfActionCall);
            Class<?> actualReturnType = resultOfActionCallNotNull.getClass();
            String actualReturnTypeAsString = actualReturnType.getName();
            String demandedReturnTypeAsString = typeToGet.getName();
            throw new MismatchedReturnTypeException(actualReturnTypeAsString, demandedReturnTypeAsString);
        }
    }

    /**
     * Executes this action and returns a return value that is produced in the result of execution
     * of this action, but cast into a specified type. The passed exception class is set as a unary
     * element of an exception list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling a {@link Action#get(Class)} method.
     *     Therefore, for details on the execution see documentation for {@link Action#get(Class)}.</li>
     *     <li>The only relevant difference between this method and a {@link Action#get(Class)} method
     *     is that this method declaration has an exception list ({@code throws...} clause) with one
     *     element inside, which is the passed exception class. The reason behind such solution
     *     is that a {@link Action#get(Class)} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows}), which allows to avoid enforcing of {@link Exception} handling.
     *     In cases, where enforcing of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException}, which is
     *     a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Action<?> action = new Action<>(() -> {
     *         throw new IOException();
     *     });
     *     }</pre>
     *     However, the first get method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     String resultOne = action.get(String.class); // Doesn't enforce exception handling
     *
     *     try {
     *         String resultTwo = action.get(String.class, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }
     *     }</pre>
     *     </li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <S>}) to which the return value will be cast into
     * @param expectedException class representing an exception class that will be set as a unary
     *                          element of an exception list belonging to this method declaration,
     *                          i.e. as a unary element of the {@code throws...} clause
     * @return return value that is produced in the result of execution of this action and
     *         cast into a specified type ({@code typeToGet}); if {@code null} is produced, then
     *         {@code null} is returned regardless of the passed {@code typeToGet}; if this action
     *         was constructed with the use of {@link Runnable}, then {@code null} is always returned
     * @param <S> type to which the return value will be cast into
     * @param <X> an exception class that will be set as a unary
     *            element of an exception list belonging to this method declaration,
     *            i.e. as a unary element of the {@code throws...} clause
     * @throws X if an {@link Exception} of {@code X} type during execution of this action was thrown
     */
    @Nullable
    @SuppressWarnings({"unused", "RedundantThrows"})
    public <S, X extends Exception>
    S get(@Nonnull Class<S> typeToGet,
          @Nullable Class<X> expectedException)
    throws X {
        return get(typeToGet);
    }

    /**
     * Executes this action and returns a return value that is produced in the result of execution
     * of this action, but cast into a specified type. The passed exception classes are set as
     * elements of an exception list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling a {@link Action#get(Class)} method.
     *     Therefore, for details on the execution see documentation for {@link Action#get(Class)}.</li>
     *     <li>The only relevant difference between this method and a {@link Action#get(Class)} method
     *     is that this method declaration has an exception list ({@code throws...} clause), which
     *     is respectively defined by the passed exception classes. The reason behind such solution
     *     is that a {@link Action#get(Class)} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows}), which allows to avoid enforcing of {@link Exception} handling.
     *     In cases, where enforcing of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException}, which is
     *     a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Action<?> action = new Action<>(() -> {
     *         throw new IOException();
     *     });
     *     }</pre>
     *     However, the first get method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     String resultOne = action.get(String.class); // Doesn't enforce exception handling
     *
     *     try {
     *         String resultTwo = action.get(String.class, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }
     *     }</pre>
     *     </li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <S>}) to which the return value will be cast into
     * @param expectedExceptionOne class representing an exception class that will be set as a first
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a first element of the {@code throws...} clause
     * @param expectedExceptionTwo class representing an exception class that will be set as a second
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a second element of the {@code throws...} clause
     * @return return value that is produced in the result of execution of this action and
     *         cast into a specified type ({@code typeToGet}); if {@code null} is produced, then
     *         {@code null} is returned regardless of the passed {@code typeToGet}; if this action
     *         was constructed with the use of {@link Runnable}, then {@code null} is always returned
     * @param <S> type to which the return value will be cast into
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of this action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of this action was thrown
     */
    @Nullable
    @SuppressWarnings({"unused", "RedundantThrows"})
    public <S, X1 extends Exception, X2 extends Exception>
    S get(@Nonnull Class<S> typeToGet,
          @Nullable Class<X1> expectedExceptionOne,
          @Nullable Class<X2> expectedExceptionTwo)
    throws X1, X2 {
        return get(typeToGet);
    }

    /**
     * Executes this action and returns a return value that is produced in the result of execution
     * of this action, but cast into a specified type. The passed exception classes are set as
     * elements of an exception list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling a {@link Action#get(Class)} method.
     *     Therefore, for details on the execution see documentation for {@link Action#get(Class)}.</li>
     *     <li>The only relevant difference between this method and a {@link Action#get(Class)} method
     *     is that this method declaration has an exception list ({@code throws...} clause), which
     *     is respectively defined by the passed exception classes. The reason behind such solution
     *     is that a {@link Action#get(Class)} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows}), which allows to avoid enforcing of {@link Exception} handling.
     *     In cases, where enforcing of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException}, which is
     *     a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Action<?> action = new Action<>(() -> {
     *         throw new IOException();
     *     });
     *     }</pre>
     *     However, the first get method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     String resultOne = action.get(String.class); // Doesn't enforce exception handling
     *
     *     try {
     *         String resultTwo = action.get(String.class, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }
     *     }</pre>
     *     </li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <S>}) to which the return value will be cast into
     * @param expectedExceptionOne class representing an exception class that will be set as a first
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a first element of the {@code throws...} clause
     * @param expectedExceptionTwo class representing an exception class that will be set as a second
     *                             element of an exception list belonging to this method declaration,
     *                             i.e. as a second element of the {@code throws...} clause
     * @param expectedExceptionThree class representing an exception class that will be set as a third
     *                               element of an exception list belonging to this method declaration,
     *                               i.e. as a third element of the {@code throws...} clause
     * @return return value that is produced in the result of execution of this action and
     *         cast into a specified type ({@code typeToGet}); if {@code null} is produced, then
     *         {@code null} is returned regardless of the passed {@code typeToGet}; if this action
     *         was constructed with the use of {@link Runnable}, then {@code null} is always returned
     * @param <S> type to which the return value will be cast into
     * @param <X1> an exception class that will be set as a first
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a first element of the {@code throws...} clause
     * @param <X2> an exception class that will be set as a second
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a second element of the {@code throws...} clause
     * @param <X3> an exception class that will be set as a third
     *             element of an exception list belonging to this method declaration,
     *             i.e. as a third element of the {@code throws...} clause
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of this action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of this action was thrown
     * @throws X3 if an {@link Exception} of {@code X3} type during execution of this action was thrown
     */
    @Nullable
    @SuppressWarnings({"unused", "RedundantThrows"})
    public <S, X1 extends Exception, X2 extends Exception, X3 extends Exception>
    S get(@Nonnull Class<S> typeToGet,
          @Nullable Class<X1> expectedExceptionOne,
          @Nullable Class<X2> expectedExceptionTwo,
          @Nullable Class<X3> expectedExceptionThree)
    throws X1, X2, X3 {
        return get(typeToGet);
    }

    /**
     * Executes this action and returns a return value that is produced in the result of execution
     * of this action, but cast into a specified type. The passed exception classes are set as
     * elements of an exception list belonging to this method declaration ({@code throws...} clause).
     * If the passed exception class is {@code null}, it will be ignored.
     * <ol>
     *     <li>The only thing this method does internally is calling a {@link Action#get(Class)} method.
     *     Therefore, for details on the execution see documentation for {@link Action#get(Class)}.</li>
     *     <li>The only relevant difference between this method and a {@link Action#get(Class)} method
     *     is that this method declaration has an exception list ({@code throws...} clause), which
     *     is respectively defined by the passed exception classes. The reason behind such solution
     *     is that a {@link Action#get(Class)} method doesn't have a {@code throws...} clause,
     *     although it is capable of throwing an {@link Exception} (the clause is omitted via
     *     {@link SneakyThrows}), which allows to avoid enforcing of {@link Exception} handling.
     *     In cases, where enforcing of such handling is, however, required, this method can be used.</li>
     *     <li>Consider the following action. It always throws an {@link java.io.IOException}, which is
     *     a subclass of an {@link Exception}:
     *     <pre>{@code
     *     Action<?> action = new Action<>(() -> {
     *         throw new IOException();
     *     });
     *     }</pre>
     *     However, the first get method below will not enforce any
     *     {@link Exception} handling, while the second one - will:
     *     <pre>{@code
     *     String resultOne = action.get(String.class); // Doesn't enforce exception handling
     *
     *     try {
     *         String resultTwo = action.get(String.class, IOException.class); // Does enforce exception handling
     *     } catch (IOException exception) {
     *         log.error("Unable to read a file", exception);
     *     }
     *     }</pre>
     *     </li>
     * </ol>
     * @param typeToGet {@link Class} representing a type ({@code <S>}) to which the return value will be cast into
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
     * @return return value that is produced in the result of execution of this action and
     *         cast into a specified type ({@code typeToGet}); if {@code null} is produced, then
     *         {@code null} is returned regardless of the passed {@code typeToGet}; if this action
     *         was constructed with the use of {@link Runnable}, then {@code null} is always returned
     * @param <S> type to which the return value will be cast into
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
     * @throws X1 if an {@link Exception} of {@code X1} type during execution of this action was thrown
     * @throws X2 if an {@link Exception} of {@code X2} type during execution of this action was thrown
     * @throws X3 if an {@link Exception} of {@code X3} type during execution of this action was thrown
     * @throws X4 if an {@link Exception} of {@code X4} type during execution of this action was thrown
     */
    @Nullable
    @SuppressWarnings({"unused", "RedundantThrows"})
    public <S, X1 extends Exception, X2 extends Exception, X3 extends Exception, X4 extends Exception>
    S get(@Nonnull Class<S> typeToGet,
          @Nullable Class<X1> expectedExceptionOne,
          @Nullable Class<X2> expectedExceptionTwo,
          @Nullable Class<X3> expectedExceptionThree,
          @Nullable Class<X4> expectedExceptionFour)
    throws X1, X2, X3, X4 {
        return get(typeToGet);
    }
}
