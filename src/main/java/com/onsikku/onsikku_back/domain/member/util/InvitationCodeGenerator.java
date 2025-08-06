package com.onsikku.onsikku_back.domain.member.util;

import java.security.SecureRandom;

public class InvitationCodeGenerator {
  private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom rnd = new SecureRandom();
  private static final int DEFAULT_LENGTH = 8;

  public static String generate() {
    StringBuilder sb = new StringBuilder(DEFAULT_LENGTH);
    for (int i = 0; i < DEFAULT_LENGTH; i++) {
      sb.append(ALPHANUM.charAt(rnd.nextInt(ALPHANUM.length())));
    }
    return sb.toString();
  }
}