package eu.ciechanowiec.conditional;

/**
 * Entity that does nothing when an implemented method is called.
 */
class VoidCheckedThrower implements ExceptionThrower {

    /**
     * Constructs an instance of a {@link VoidCheckedThrower}.
     */
    VoidCheckedThrower() {
        // Constructor to keep javadoc
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends Exception> void throwCheckedIfActive(T exceptionToThrowOrSwallow) throws T {
        // Do nothing
    }
}
