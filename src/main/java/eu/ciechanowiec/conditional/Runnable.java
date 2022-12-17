package eu.ciechanowiec.conditional;

/**
 * Functional interface that takes any command and runs it
 * in a void manner, without returning a result.
 * <p>
 * It is similar to {@link java.lang.Runnable}, but differs from it in such way that its unary
 * {@link Runnable#run()} method has an {@link Exception} specified in the method declaration
 * within a {@code throws...} clause. Such declaration is required to enable the possibility
 * of passing to respective methods and constructors of {@link Conditional} and {@link Action}
 * commands that throw checked {@link Exception}s without enforcing to handle them.
 */
@FunctionalInterface
public interface Runnable {

    /**
     * Takes any command and runs it in a void manner, without returning a result value.
     * @throws Exception if during the run of the passed command(s) an {@link Exception} occurred
     */
    @SuppressWarnings("squid:S112")
    void run() throws Exception;
}
