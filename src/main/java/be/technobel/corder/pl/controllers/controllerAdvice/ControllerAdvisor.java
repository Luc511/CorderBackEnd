package be.technobel.corder.pl.controllers.controllerAdvice;

import be.technobel.corder.pl.config.exceptions.*;
import be.technobel.corder.pl.models.dtos.ErrorDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailSendException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class ControllerAdvisor {
    @ExceptionHandler(DuplicateParticipationException.class)
    public ResponseEntity<ErrorDTO> handleDuplicateParticipationException(DuplicateParticipationException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PhotoException.class)
    public ResponseEntity<ErrorDTO> handlePhotoException(PhotoException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleEntityNotFoundException(EntityNotFoundException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorDTO> handleMultipartException(MultipartException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDTO> handleValidationExceptions(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorDTO> handleAuthenticationServiceException(AuthenticationException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDTO> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorDTO> handleInvalidPasswordException(InvalidPasswordException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<ErrorDTO> handleMailSendException(MailSendException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorDTO> handleTooManyRequestsException(TooManyRequestsException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorDTO> handleDuplicateUserException(DuplicateUserException e) {
        return new ResponseEntity<>(new ErrorDTO(e.getMessage(), ""), HttpStatus.NOT_ACCEPTABLE);
    }

}
