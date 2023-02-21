package chapter11.util;

import java.util.Optional;

public class OptionalUtility {

    /**
     * 문자열을 정수 Optional로 변환
     *
     * @param s : 문자열
     * @return s를 정수로 변환한 값
     */
    public static Optional<Integer> stringToInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

}
