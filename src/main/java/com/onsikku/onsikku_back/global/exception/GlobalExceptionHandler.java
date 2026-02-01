package com.onsikku.onsikku_back.global.exception;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.onsikku.onsikku_back.global.response.BaseResponse;
import com.onsikku.onsikku_back.global.response.BaseResponseStatus;
import jakarta.persistence.QueryTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    protected BaseResponse<String> handleBaseException(BaseException e) {
        log.error(e.getMessage());
        return new BaseResponse<>(e.getStatus());
    }

    @ExceptionHandler({QueryTimeoutException.class, RedisConnectionFailureException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // HTTP 500 상태 코드를 유지합니다.
    public BaseResponse<Object> handleRedisException(Exception e) {
        log.error("Redis Operation Failed (500): {}", e.getMessage(), e);
        return new BaseResponse<>(BaseResponseStatus.REDIS_OPERATION_FAILED);
    }

    // JSON 역직렬화 실패(잘못된 Enum 값 등) 처리
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    protected BaseResponse<String> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        // Enum 역직렬화 실패 시 허용 값 안내
        if (ex.getCause() instanceof InvalidFormatException ife) {
            Class<?> targetType = ife.getTargetType();  // 여기서 Class<?>로 받아야 함

            if (targetType != null && targetType.isEnum()) {
                String invalidValue = String.valueOf(ife.getValue());
                String allowed = Arrays.stream(targetType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

                return new BaseResponse<>(400,
                    String.format("잘못된 값 '%s'이(가) 전달되었습니다. 허용 가능한 값: [%s]",
                        invalidValue, allowed)
                );
            }
        }
        return new BaseResponse<>(400,"요청 본문을 읽을 수 없습니다. JSON 형식과 필드 값을 확인해 주세요.");
    }
    @ExceptionHandler(HttpMessageConversionException.class)
    public BaseResponse<String> handleLazyLoadingSerializationError(HttpMessageConversionException e) {

        // 1. root cause가 InvalidDefinitionException인지 확인
        Throwable rootCause = e.getRootCause();
        if (rootCause instanceof InvalidDefinitionException ide) {

            String errorMessage = "엔티티의 지연 로딩 필드를 직렬화할 수 없습니다.";

            // 2. 오류 체인에서 구체적인 문제 엔티티/필드 추출 (선택적)
            String referenceChain = "확인된 오류 경로: " + ide.getPathReference();

            log.error("[Hibernate Lazy Loading Error] {} - {}", errorMessage, referenceChain, ide);

            // 사용자에게는 상세한 내부 스택 트레이스 대신 깔끔한 오류 응답 반환
            return new BaseResponse<>(BaseResponseStatus.SERIALIZATION_ERROR);
        }

        // 해당되는 다른 HttpMessageConversionException이 아닌 경우, 일반적인 500 에러 처리
        log.error("[HTTP Conversion Error] ", e);
        return new BaseResponse<>(BaseResponseStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DB 제약 조건 위반 처리 (NOT NULL, UNIQUE, CHECK constraint 위반)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected BaseResponse<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();

        // 내부 로그 기록
        log.error("[DB Integrity Violation] Error: {}", message, ex);

        // 사용자에게 노출할 메시지 결정
        String userMessage;
        if (message != null && message.contains("violates not-null constraint")) {
            userMessage = "필수 입력 항목이 누락되었거나 데이터베이스에 잘못된 값이 전달되었습니다. (NOT NULL 위반)";
        } else if (message != null && message.contains("violates unique constraint")) {
            userMessage = "이미 존재하는 값입니다. (UNIQUE 제약 조건 위반)";
        } else {
            userMessage = "데이터 무결성 제약 조건을 위반했습니다. 요청 데이터를 확인해주세요.";
        }

        return new BaseResponse<>(500, userMessage);
    }

    /**
     * 예측하지 못한 모든 예외를 잡아서
     * 발생한 메시지를 그대로 사용자에게 전달합니다.
     */
    @ExceptionHandler(Exception.class)
    protected BaseResponse<String> handleAllException(Exception ex) {
        // 로그에는 전체 스택 트레이스를 남겨서 디버깅을 돕습니다.
        log.error("[Unhandled Exception] ", ex);

        // 원인(Cause)이 있다면 더 구체적인 원인 메시지를 가져오고, 없으면 기본 메시지를 사용합니다.
        String errorMessage = ex.getMessage();

        // 만약 메시지조차 null이라면 예외 클래스 이름을 보냅니다.
        if (errorMessage == null) {
            errorMessage = ex.getClass().getSimpleName();
        }

        return new BaseResponse<>(500, "예상치 못한 에러가 발생했습니다. : " + errorMessage);
    }
}