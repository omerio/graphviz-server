package info.dawelbeit.graphviz.dot.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExample01 {

	public static void main(String[] args) {
        String data = "digraph travle_schedule { ...";

        // "digraph"과 "{" 사이에 있는 모든 문자를 찾는 정규 표현식 패턴
        String patternString = "digraph(.*?)\\{";

        Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            System.out.println("Found: " + matcher.group(1).trim());
        } else {
            System.out.println("Not Found");
        }

	}

}
