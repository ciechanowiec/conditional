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
    public void throwUncheckedIfActive(RuntimeException exceptionToThrowOrSwallow) {
        throw exceptionToThrowOrSwallow;
    }
}
