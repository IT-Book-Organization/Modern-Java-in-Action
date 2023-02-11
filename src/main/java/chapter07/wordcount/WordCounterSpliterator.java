package chapter07.wordcount;

import java.util.Spliterator;
import java.util.function.Consumer;

public class WordCounterSpliterator implements Spliterator<Character> {

    private final String string;
    private int currentChar = 0;

    public WordCounterSpliterator(String string) {
        this.string = string;
    }

    /**
     * 문자열에서 현재 인덱스에 해당하는 문자를 Consumer에 제공한 다음, 인데스를 증가시킨다.
     *
     * @param action 소비한 문자를 전달
     * @return 소비할 문자가 남아있으면 true를 반환 (반복해야 할 문자가 남아있음을 의미)
     */
    @Override
    public boolean tryAdvance(Consumer<? super Character> action) {
        action.accept(string.charAt(currentChar++)); // 현재 문자를 소비
        return currentChar < string.length();
    }

    /**
     * 반복될 자료구조를 분할하는 로직을 포함한다.
     * 분할이 필요한 상황에서는 파싱해야 할 문자열 청크의 중간 위치를 기준으로 분할하도록 지시한다.
     *
     * @return 남은 문자 수가 한계값 이하면 null 반환 -> 분할을 중지하도록 지시
     */
    @Override
    public Spliterator<Character> trySplit() {
        int currentSize = string.length() - currentChar;
        if (currentSize < 10) {
            return null; // 파싱할 문자열이 순차 처리할 수 있을 만큼 충분히 작아졌음을 알림
        }

        // 1. 파싱할 문자열의 중간을 분할 위치로 설정
        for (int splitPos = currentSize / 2 + currentChar; splitPos < string.length(); splitPos++) {
            // 2. 다음 공백이 나올 때까지 분할 위치를 뒤로 이동시킴
            if (Character.isWhitespace(string.charAt(splitPos))) {

                // 3. 처음부터 분할위치까지 문자열을 파싱할 새로운 WordCounterSpliterator를 생성
                Spliterator<Character> spliterator = new WordCounterSpliterator(string.substring(currentChar, splitPos));

                // 4. 이 WordCounterSpliterator의 시작 위치를 분할 위치로 설정
                currentChar = splitPos;

                // 5. 공백을 찾았고 문자열을 분리했으므로 루프를 종료
                return spliterator;
            }
        }
        return null;
    }

    /**
     * @return 탐색해야 할 요소의 개수
     */
    @Override
    public long estimateSize() {
        return string.length() - currentChar;
    }

    /**
     * @return 특성들
     */
    @Override
    public int characteristics() {
        return ORDERED // 문자열의 문자 등장 순서가 유의미함
                + SIZED // estimatedSize의 메서드의 반환값이 정확함
                + SUBSIZED // trySplit으로 생성된 Spliterator도 정확한 크기를 가짐
                + NONNULL // 문자열에는 null 문자가 존재하지 않음
                + IMMUTABLE // 문자열 자체가 불변 클래스이므로 문자열을 파싱하면서 속성이 추가되지 않음
                ;
    }

}
