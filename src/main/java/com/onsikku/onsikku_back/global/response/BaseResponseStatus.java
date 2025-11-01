package com.onsikku.onsikku_back.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseResponseStatus {

    // 2xx 성공
    SUCCESS(HttpStatus.OK, "요청에 성공하였습니다."),

    // 카카오 OAUTH2 관련
    KAKAO_INVALID_GRANT(HttpStatus.BAD_REQUEST, "카카오 code의 만료시간이 지났거나, 해당 code에 대한 인증 코드가 존재하지 않습니다."),

    // 인증 및 토큰 관련
    FAIL_TOKEN_AUTHORIZATION(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 이유로 토큰 인증에 실패하였습니다."),
    MISSING_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 엑세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "리프레시 토큰을 찾을 수 없습니다."),
    TOKEN_BLACKLISTED(HttpStatus.UNAUTHORIZED, "블랙리스트 처리된 토큰입니다."),

    // 회원 관련
    INVALID_REGISTRATION_TOKEN(HttpStatus.UNAUTHORIZED, "해당 회원가입용 토큰이 존재하지 않습니다."),
    INVALID_FAMILY_INVITATION_CODE(HttpStatus.NOT_FOUND, "가족 코드가 올바르지 않습니다."),
    INVALID_FAMILY_MODE(HttpStatus.NOT_FOUND, "가족 생성/참여 모드가 올바르지 않습니다."),
    ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 가입된 회원입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVITATION_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "초대 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_GENERATED_INVITATION_CODE(HttpStatus.INTERNAL_SERVER_ERROR, "생성된 초대코드가 8자리 대문자+숫자를 만족하지 않습니다."),

    // 가족 관련
    FAMILY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 가족입니다."),
    INVALID_FAMILY_MEMBER(HttpStatus.UNAUTHORIZED, "해당 가족에 속한 회원이 아닙니다."),

    // 질문/답변 관련
    CANNOT_ANSWER_OTHER_QUESTION(HttpStatus.UNAUTHORIZED, "질문이 할당된 사람만 가능한 권한입니단."),
    INVALID_ANSWER(HttpStatus.BAD_REQUEST, "답변을 입력해주세요."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "답변이 존재하지 않습니다."),
    QUESTION_ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "할당된 질문이 존재하지 않습니다."),

    // AI 서버 관련 (추가된 부분)
    AI_SERVER_VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_ENTITY, "AI 서버 요청 데이터가 유효하지 않습니다."),
    AI_SERVER_COMMUNICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI 서버와 통신 중 오류가 발생했습니다."),

    // 서버 오류
    REDIS_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서비스 이용이 불가합니다. Redis 서버에 문제가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버의 문제가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    BaseResponseStatus(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    // HTTP 상태 코드 반환
    public int getCode() {
        return httpStatus.value();
    }
}
