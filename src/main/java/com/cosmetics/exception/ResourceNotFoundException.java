package com.cosmetics.exception;

public class ResourceNotFoundException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);  // Gọi constructor của lớp cha (RuntimeException)
    }
    public class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
}
