package io.github.rodrigodsantos.upleveling.shared.exception;

/**
 * O recurso não existe ou pertence a outro usuário (os dois respondem 404, para não revelar que ele existe).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " " + id + " não encontrado(a)");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
