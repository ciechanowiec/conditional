package eu.ciechanowiec.conditional;

/**
 * Entity that throws or swallows passed
 * {@link Exception}s, depending on the implementation.
 */
interface ExceptionThrower {

    /**
     * Throws the passed {@link RuntimeException} if this exception thrower is of active type.
     * <p>
     * Otherwise, the passed {@link RuntimeException} is swallowed, i.e. the call of this method has no effects.
     * @param exceptionToThrowOrSwallow exception to be thrown or swallowed
     * @param <T> type of the passed exception
     * @throws T if this exception thrower is of active type
     * @throws UnsupportedOperationException if this method hasn't been implemented by
     * this exception thrower
     */
    default <T extends RuntimeException> void throwUncheckedIfActive(T exceptionToThrowOrSwallow) throws T {
        throw new UnsupportedOperationException("The method hasn't been implemented");
    }

    /**
     * Throws the passed {@link Exception} if this exception thrower is of active type.
     * <p>
     * Otherwise, the passed {@link Exception} is swallowed, i.e. the call of this method has no effects.
     * @param exceptionToThrowOrSwallow exception to be thrown or swallowed
     * @param <T> type of the passed exception
     * @throws T if this exception thrower is of active type
     * @throws UnsupportedOperationException if this method hasn't been implemented by
     * this exception thrower
     */
    default <T extends Exception> void throwCheckedIfActive(T exceptionToThrowOrSwallow) throws T {
        throw new UnsupportedOperationException("The method hasn't been implemented");
    }
}
