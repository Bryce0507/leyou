package com.leyou.common.exceptions;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

@Getter
public class LyException extends RuntimeException {
    private int status;
    private String message;

    public LyException(ExceptionEnum em) {
//        super(em.getMessage());
        this.message=em.getMessage();
        this.status = em.getStatus();
    }

    public LyException(ExceptionEnum em, Throwable cause) {
//        super(em.getMessage(), cause);
        super(cause);
        this.message = em.getMessage();
        this.status = em.getStatus();
    }

    public LyException(int status, String message) {
        this.status = status;
        this.message = message;
    }





}
