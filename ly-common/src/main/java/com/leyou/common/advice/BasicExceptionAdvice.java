package com.leyou.common.advice;

import com.leyou.common.vo.ExceptionResult;
import com.leyou.common.exceptions.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class BasicExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e) {
        e.printStackTrace();
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handleException(LyException e) {
        e.printStackTrace();
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));
    }
}
