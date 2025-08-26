package kr.bi.greenmate.common.util;

public final class StringUtils {

  private StringUtils() {
  }

  /**
   * 문자열을 지정한 길이만큼 잘라 말줄임표("...")를 붙여 미리보기 용도로 반환한다.
   *
   * @param text      원본 문자열
   * @param maxLength 최대 허용 길이 (본문 길이, "..." 제외)
   * @return 잘린 문자열 (maxLength 이하일 경우 원문 그대로 반환)
   */
  public static String truncateWithEllipsis(String text, int maxLength) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    int cpCount = text.codePointCount(0, text.length());
    if (cpCount <= maxLength) {
      return text;
    }
    int endIdx = text.offsetByCodePoints(0, maxLength);
    return text.substring(0, endIdx) + "...";
  }
}
