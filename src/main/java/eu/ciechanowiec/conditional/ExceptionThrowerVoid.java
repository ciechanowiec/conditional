package eu.ciechanowiec.conditional;

import javax.annotation.Nullable;

/**
 * Entity that does nothing when an implemented method is called.
 */
class ExceptionThrowerVoid implements ExceptionThrower {

    /**
     * Constructs an instance of a {@link ExceptionThrowerVoid}.
     */
    @SuppressWarnings("RedundantNoArgConstructor")
    ExceptionThrowerVoid() {
        // Constructor to keep javadoc
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Exception> void throwIfActive(@Nullable T exceptionToThrowOrSwallow) throws T {
        // Do nothing
    }
}
