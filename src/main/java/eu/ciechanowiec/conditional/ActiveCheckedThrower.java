package eu.ciechanowiec.conditional;

/**
 * Entity that throws an {@link Exception} when
 * an implemented method is called.
 */
class ActiveCheckedThrower implements ExceptionThrower {

    /**
     * Constructs an instance of an {@link ActiveCheckedThrower}.
     */
    ActiveCheckedThrower() {
        // Constructor to keep javadoc
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Exception> void throwCheckedIfActive(T exceptionToThrowOrSwallow) throws T {
        throw exceptionToThrowOrSwallow;
    }
}
