package exception;

/**
 * Checked exception raised whenever user-supplied data fails a business rule.
 * Being checked, it forces the Controller layer to handle the error, so invalid
 * input can never silently reach the Model.
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String field;

    public ValidationException(String message) {
        this("", message);
    }

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
