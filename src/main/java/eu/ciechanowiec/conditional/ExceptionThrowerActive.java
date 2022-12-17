package eu.ciechanowiec.conditional;

import javax.annotation.Nonnull;

/**
 * Entity that throws an {@link Exception} when
 * an implemented method is called.
 */
class ExceptionThrowerActive implements ExceptionThrower {

    /**
     * Constructs an instance of an {@link ExceptionThrowerActive}.
     */
    @SuppressWarnings("RedundantNoArgConstructor")
    ExceptionThrowerActive() {
        // Constructor to keep javadoc
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Exception> void throwIfActive(@Nonnull T exceptionToThrowOrSwallow) throws T {
        throw exceptionToThrowOrSwallow;
    }
}
