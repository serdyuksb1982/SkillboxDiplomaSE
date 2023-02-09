package searchengine.exception;

public class InternalServerError extends RuntimeException{
    private static final long seralVersionUID = 1L;
    public InternalServerError(String message) {
        super(message);
    }
}
