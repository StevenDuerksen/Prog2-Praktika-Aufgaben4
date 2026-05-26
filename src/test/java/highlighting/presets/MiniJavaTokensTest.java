package highlighting.presets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import highlighting.regex.Token;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;

class MiniJavaTokensTest {

    private Token tokenByColour(Color colour) {
        return MiniJavaTokens.defaultTokens().stream()
            .filter(token -> token.colour().equals(colour))
            .findFirst()
            .orElseThrow();
    }

    private List<String> matches(Token token, String text) {
        List<String> result = new ArrayList<>();
        Matcher matcher = token.pattern().matcher(text);

        while (matcher.find()) {
            result.add(matcher.group(token.matchingGroup()));
        }

        return result;
    }

    @Test
    void stringLiteralMatchesAtStartMiddleAndEnd() {
        // Given
        Token token = tokenByColour(MiniJavaColours.STRING_LITERAL_COLOUR);
        String text = "\"start\" String text = \"middle\"; return \"end\"";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("\"start\"", "\"middle\"", "\"end\""), result);
    }

    @Test
    void stringLiteralCanContainCommentMarkers() {
        // Given
        Token token = tokenByColour(MiniJavaColours.STRING_LITERAL_COLOUR);
        String text = "String text = \"this is not // a comment and not /* a block comment */\";";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(
            List.of("\"this is not // a comment and not /* a block comment */\""),
            result);
    }

    @Test
    void stringLiteralDoesNotMatchUnclosedString() {
        // Given
        Token token = tokenByColour(MiniJavaColours.STRING_LITERAL_COLOUR);
        String text = "String text = \"not closed;";

        // When
        List<String> result = matches(token, text);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void charLiteralMatchesAtStartMiddleAndEnd() {
        // Given
        Token token = tokenByColour(MiniJavaColours.CHAR_LITERAL_COLOUR);
        String text = "'a' char middle = 'b'; char end = '\\n'";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("'a'", "'b'", "'\\n'"), result);
    }

    @Test
    void charLiteralDoesNotMatchInvalidCharLiterals() {
        // Given
        Token token = tokenByColour(MiniJavaColours.CHAR_LITERAL_COLOUR);
        String text = "char invalidOne = 'ab'; char invalidTwo = '';";

        // When
        List<String> result = matches(token, text);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void keywordMatchesAtStartMiddleAndEnd() {
        // Given
        Token token = tokenByColour(MiniJavaColours.KEYWORD_COLOUR);
        String text = "package demo; public class Test { return null";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("package", "public", "class", "return", "null"), result);
    }

    @Test
    void keywordMatchesMultipleKeywordsInSameText() {
        // Given
        Token token = tokenByColour(MiniJavaColours.KEYWORD_COLOUR);
        String text = "private final Object value = new Object(); if (this != null) return value; else return null;";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(
            List.of("private", "final", "new", "if", "this", "null", "return", "else", "return", "null"),
            result);
    }

    @Test
    void keywordDoesNotMatchInsideOtherIdentifiers() {
        // Given
        Token token = tokenByColour(MiniJavaColours.KEYWORD_COLOUR);
        String text = "packageName imported myclass publicValue privateField finally returnValue nullable newest";

        // When
        List<String> result = matches(token, text);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void annotationMatchesAtLineStartWithWhitespaceAndInMiddle() {
        // Given
        Token token = tokenByColour(MiniJavaColours.ANNOTATION_COLOUR);
        String text = "@Override\n    @Deprecated\nclass Test { @Test void method() {} }";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("@Override", "@Deprecated", "@Test"), result);
    }

    @Test
    void annotationDoesNotMatchWithoutAtSign() {
        // Given
        Token token = tokenByColour(MiniJavaColours.ANNOTATION_COLOUR);
        String text = "Override Deprecated Test";

        // When
        List<String> result = matches(token, text);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void lineCommentMatchesAtStartMiddleAndEnd() {
        // Given
        Token token = tokenByColour(MiniJavaColours.LINE_COMMENT_COLOUR);
        String text = "// start\nint value = 1; // middle\nreturn value; // end";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("// start", "// middle", "// end"), result);
    }

    @Test
    void lineCommentCanContainKeywordLikeText() {
        // Given
        Token token = tokenByColour(MiniJavaColours.LINE_COMMENT_COLOUR);
        String text = "// public class return null new private final";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("// public class return null new private final"), result);
    }

    @Test
    void lineCommentDoesNotMatchNormalCode() {
        // Given
        Token token = tokenByColour(MiniJavaColours.LINE_COMMENT_COLOUR);
        String text = "public class Test { return null; }";

        // When
        List<String> result = matches(token, text);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void blockCommentMatchesAtStartMiddleAndEnd() {
        // Given
        Token token = tokenByColour(MiniJavaColours.BLOCK_COMMENT_COLOUR);
        String text = "/* start */ int value = 1; /* middle */ return value; /* end */";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("/* start */", "/* middle */", "/* end */"), result);
    }

    @Test
    void blockCommentCanContainKeywordLikeText() {
        // Given
        Token token = tokenByColour(MiniJavaColours.BLOCK_COMMENT_COLOUR);
        String text = "/* public class return null new private final */";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("/* public class return null new private final */"), result);
    }

    @Test
    void blockCommentDoesNotConsumeMultipleCommentsAtOnce() {
        // Given
        Token token = tokenByColour(MiniJavaColours.BLOCK_COMMENT_COLOUR);
        String text = "/* first */ int value = 1; /* second */";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("/* first */", "/* second */"), result);
    }

    @Test
    void blockCommentDoesNotMatchUnclosedComment() {
        // Given
        Token token = tokenByColour(MiniJavaColours.BLOCK_COMMENT_COLOUR);
        String text = "/* not closed";

        // When
        List<String> result = matches(token, text);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void javadocCommentMatchesAtStartMiddleAndEnd() {
        // Given
        Token token = tokenByColour(MiniJavaColours.JAVADOC_COMMENT_COLOUR);
        String text = "/** start */ class Test { /** middle */ void method() {} } /** end */";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("/** start */", "/** middle */", "/** end */"), result);
    }

    @Test
    void javadocCommentCanContainKeywordLikeText() {
        // Given
        Token token = tokenByColour(MiniJavaColours.JAVADOC_COMMENT_COLOUR);
        String text = """
        /**
         * public class return null new private final
         */
        """;

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of(text.stripTrailing()), result);
    }

    @Test
    void javadocCommentDoesNotMatchNormalCode() {
        // Given
        Token token = tokenByColour(MiniJavaColours.JAVADOC_COMMENT_COLOUR);
        String text = "public class Test { return null; }";

        // When
        List<String> result = matches(token, text);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void funnyStuffMatchesEverySecondCharacter() {
        // Given
        Token token = tokenByColour(MiniJavaColours.FUNNY_STUFF_HIHI);
        String text = "123456";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("2", "4", "6"), result);
    }

    @Test
    void funnyStuffWorksAcrossWholeTextIncludingLineBreaks() {
        // Given
        Token token = tokenByColour(MiniJavaColours.FUNNY_STUFF_HIHI);
        String text = "12\n34";

        // When
        List<String> result = matches(token, text);

        // Then
        assertEquals(List.of("2", "3"), result);
    }

    @Test
    void funnyStuffDoesNotMatchSingleCharacterText() {
        // Given
        Token token = tokenByColour(MiniJavaColours.FUNNY_STUFF_HIHI);
        String text = "1";

        // When
        List<String> result = matches(token, text);

        // Then
        assertTrue(result.isEmpty());
    }
}
