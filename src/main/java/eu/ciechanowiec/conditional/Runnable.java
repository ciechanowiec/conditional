package eu.ciechanowiec.conditional;

/**
 * Functional interface that takes any command and runs it
 * in a void manner, without returning a result.
 * <p>
 * It is similar to {@link java.lang.Runnable}, but differs from it in such way that its unary
 * {@link Runnable#run()} method declares an {@link Exception}. Such declaration is required
 * to enable the possibility of passing to respective methods and constructors of {@link Conditional}
 * and {@link Action} commands that throw checked {@link Exception}s without handling them.
 */
@FunctionalInterface
public interface Runnable {

    /**
     * Takes any command and runs it in a void manner, without returning a result.
     * @throws Exception if during the run of the passed command(s) an {@link Exception} occurred
     */
    void run() throws Exception;
}
