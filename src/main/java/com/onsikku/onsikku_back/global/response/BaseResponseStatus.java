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

    // 질문/답변 관련
    INVALID_MEMBER(HttpStatus.UNAUTHORIZED, "자신에게 할당된 질문만 답변할 수 있습니다."),
    INVALID_STORY(HttpStatus.BAD_REQUEST, "존재하지 않는 스토리입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    INVALID_QUESTION(HttpStatus.BAD_REQUEST, "유효하지 않은 질문입니다."),
    INVALID_ANSWER(HttpStatus.BAD_REQUEST, "답변을 입력해주세요."),
    ANSWER_NOT_FOUND(HttpStatus.NOT_FOUND, "답변이 존재하지 않습니다."),

    // 서버 오류
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
