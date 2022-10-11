package eu.ciechanowiec.conditional;

/**
 * Entity that throws a {@link RuntimeException} when
 * an implemented method is called.
 */
class ActiveUncheckedThrower implements ExceptionThrower {

    /**
     * Constructs an instance of an {@link ActiveUncheckedThrower}.
     */
    ActiveUncheckedThrower() {
        // Constructor to keep javadoc
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends RuntimeException> void throwUncheckedIfActive(T exceptionToThrowOrSwallow) throws T {
        throw exceptionToThrowOrSwallow;
    }
}
