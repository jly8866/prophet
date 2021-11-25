package com.prophet.common;

import java.io.Serializable;

/*
 * 献给最爱的铭铭丫头 ^_^
 */
public class RestResponse<T> implements Serializable {
    private static final long serialVersionUID = 605544666575940834L;
    public static final int SUCCESS = 0;
    public static final int FAIL = 1;
    
    private final int status;
    private final String message;
    private final T data;

    public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}

	private RestResponse(final int status, final String msg, final T result) {
        this.status = status;
        this.message = msg;
        this.data = result;
    }

//    public static <T> RestResponse<T> of(final ErrorCode code, final T result) {
//        return new RestResponse<>(code.getUniverseErrorCode(), result, code.getErrorMsg());
//    }
	public static <T> RestResponse<T> success() {
		return new RestResponse<T>(SUCCESS, null, null);
	}
	
    public static <T> RestResponse<T> success(final T result) {
        return new RestResponse<T>(SUCCESS, null, result);
    }

    public static <T> RestResponse<T> fail(final String msg) {
        return new RestResponse<T>(FAIL, msg, null);
    }

}

