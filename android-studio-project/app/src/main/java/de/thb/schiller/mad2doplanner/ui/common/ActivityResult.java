package de.thb.schiller.mad2doplanner.ui.common;

/**
 * @author Dominic Schiller
 * @since 02.07.17
 */

public enum ActivityResult {
    RESULT_TODO_CREATED(1 << 1),
    RESULT_TODO_UPDATED(1 << 2),
    RESULT_TODO_DELETED(1 << 3);

    private final int resultCode;

    ActivityResult(int resultCode) {
        this.resultCode = resultCode;
    }

    public int getResultCode() {
        return this.resultCode;
    }
}
