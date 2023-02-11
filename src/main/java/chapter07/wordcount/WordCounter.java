package chapter07.wordcount;

public class WordCounter {

    private final int counter;
    private final boolean lastSpace;

    public WordCounter(int counter, boolean lastSpace) {
        this.counter = counter;
        this.lastSpace = lastSpace;
    }

    public WordCounter accumulate(Character c) {
        if (Character.isWhitespace(c)) { // 문자열의 문자를 하나씩 탐색
            return lastSpace ? this : new WordCounter(counter, true);
        } else {
            // 문자를 하나씩 탐색하다 공백 문자를 만났을 경우 -> 지금까지 탐색한 문자를 단어로 간주
            return lastSpace ? new WordCounter(counter + 1, false) : this;
        }
    }

    public WordCounter combine(WordCounter wordCounter) {
        return new WordCounter(counter + wordCounter.counter, wordCounter.lastSpace);
    }

    public int getCounter() {
        return counter;
    }

}
