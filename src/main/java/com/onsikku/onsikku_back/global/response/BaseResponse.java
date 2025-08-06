package com.onsikku.onsikku_back.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"code", "message", "result"})
public class BaseResponse<T> {
    private final String message;
    private final int code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    /**
     * 요청 성공 시
     * Ex) return new BaseResponse<>(signUpUserRes);
     *
     * @param result
     */
    public BaseResponse(T result) {
        this.message = BaseResponseStatus.SUCCESS.getMessage();
        this.code = BaseResponseStatus.SUCCESS.getCode();
        this.result = result;
    }

    /**
     * 요청 실패 시
     * Ex) return new BaseResponse<>(BaseResponseStatus.DATABASE_INSERT_ERROR);
     *
     * @param status
     */
    public BaseResponse(BaseResponseStatus status) {
        this.message = status.getMessage();
        this.code = status.getCode();
    }

    /**
     * 직접 설정할 수 있게 생성자 만듦
     * Ex) return new BaseResponse<>(401, "권한이 없습니다.")
     *
     * @param code
     * @param message
     */
    public BaseResponse( int code, String message) {
        this.code = code;
        this.message = message;
    }
}
