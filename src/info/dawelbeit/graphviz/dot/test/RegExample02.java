package info.dawelbeit.graphviz.dot.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExample02 {
    public static void main(String[] args) {
        String data = "digraph      travle_schedule      { ...";

        // "digraph" 다음에 오는 단어를 찾는 정규 표현식 패턴
        String patternString = "digraph\\s+(\\w+)\\s*\\{";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            System.out.println("Found: " + matcher.group(1));
        } else {
            System.out.println("Not Found");
        }
    }
}
