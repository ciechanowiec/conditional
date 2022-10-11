package eu.ciechanowiec.conditional;

/**
 * Entity that does nothing when an implemented method is called.
 */
class VoidUncheckedThrower implements ExceptionThrower {

    /**
     * Constructs an instance of a {@link VoidUncheckedThrower}.
     */
    VoidUncheckedThrower() {
        // Constructor to keep javadoc
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends RuntimeException> void throwUncheckedIfActive(T exceptionToThrowOrSwallow) throws T {
        // Do nothing
    }
}
