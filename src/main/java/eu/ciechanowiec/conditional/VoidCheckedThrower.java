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
    public void throwCheckedIfActive(Exception exceptionToThrowOrSwallow) {
        // Do nothing
    }
}
