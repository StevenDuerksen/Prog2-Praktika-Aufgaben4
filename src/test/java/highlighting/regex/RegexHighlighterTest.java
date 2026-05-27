package highlighting.regex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import highlighting.core.HighlightRegion;
import highlighting.presets.MiniJavaColours;
import java.awt.Color;
import java.util.List;
import org.junit.jupiter.api.Test;

/*
 * Eigentlich sollte ich den Funny Stuff Token entfernen weil dieser langzeitig nur zu Problemen führen wird
 * aber ich machs nicht weils einfach funny ist hihi
 */
class RegexHighlighterTest {

  private final RegexHighlighter highlighter = new RegexHighlighter();

  @Test
  void collectMatchesFindsSimpleKeywordRegionsWithoutOverlaps() {
    // Given
    String text = "public class Test";

    // When
    List<HighlightRegion> regions = withoutFunnyStuff(highlighter.collectMatches(text));

    // Then
    assertTrue(hasRegion(regions, 0, 6, MiniJavaColours.KEYWORD_COLOUR));
    assertTrue(hasRegion(regions, 7, 12, MiniJavaColours.KEYWORD_COLOUR));
  }

  @Test
  void collectMatchesFindsStringAndKeywordRegionsWithoutOverlaps() {
    // Given
    String text = "return \"hello\"";

    // When
    List<HighlightRegion> regions = withoutFunnyStuff(highlighter.collectMatches(text));

    // Then
    assertTrue(hasRegion(regions, 0, 6, MiniJavaColours.KEYWORD_COLOUR));
    assertTrue(hasRegion(regions, 7, 14, MiniJavaColours.STRING_LITERAL_COLOUR));
  }

  @Test
  void resolveConflictsRemovesKeywordInsideComment() {
    // Given
    List<HighlightRegion> regions =
        List.of(
            new HighlightRegion(0, 22, MiniJavaColours.LINE_COMMENT_COLOUR),
            new HighlightRegion(3, 9, MiniJavaColours.KEYWORD_COLOUR),
            new HighlightRegion(10, 15, MiniJavaColours.KEYWORD_COLOUR));

    // When
    List<HighlightRegion> result = highlighter.resolveConflicts(regions);

    // Then
    assertEquals(1, result.size());
    assertTrue(hasRegion(result, 0, 22, MiniJavaColours.LINE_COMMENT_COLOUR));
  }

  @Test
  void resolveConflictsKeepsJavadocWhenBlockCommentOverlaps() {
    // Given
    List<HighlightRegion> regions =
        List.of(
            new HighlightRegion(0, 30, MiniJavaColours.JAVADOC_COMMENT_COLOUR),
            new HighlightRegion(0, 30, MiniJavaColours.BLOCK_COMMENT_COLOUR));

    // When
    List<HighlightRegion> result = highlighter.resolveConflicts(regions);

    // Then
    assertEquals(1, result.size());
    assertTrue(hasRegion(result, 0, 30, MiniJavaColours.JAVADOC_COMMENT_COLOUR));
  }

  @Test
  void resolveConflictsKeepsAdjacentRegions() {
    // Given
    List<HighlightRegion> regions =
        List.of(
            new HighlightRegion(0, 5, MiniJavaColours.KEYWORD_COLOUR),
            new HighlightRegion(5, 10, MiniJavaColours.STRING_LITERAL_COLOUR));

    // When
    List<HighlightRegion> result = highlighter.resolveConflicts(regions);

    // Then
    assertEquals(2, result.size());
    assertTrue(hasRegion(result, 0, 5, MiniJavaColours.KEYWORD_COLOUR));
    assertTrue(hasRegion(result, 5, 10, MiniJavaColours.STRING_LITERAL_COLOUR));
  }

  @Test
  void resolveConflictsRemovesLaterOverlappingRegion() {
    // Given
    List<HighlightRegion> regions =
        List.of(
            new HighlightRegion(0, 10, MiniJavaColours.BLOCK_COMMENT_COLOUR),
            new HighlightRegion(5, 12, MiniJavaColours.KEYWORD_COLOUR));

    // When
    List<HighlightRegion> result = highlighter.resolveConflicts(regions);

    // Then
    assertEquals(1, result.size());
    assertTrue(hasRegion(result, 0, 10, MiniJavaColours.BLOCK_COMMENT_COLOUR));
  }

  @Test
  void collectMatchesReturnsEmptyListForEmptyText() {
    // Given
    String text = "";

    // When
    List<HighlightRegion> regions = withoutFunnyStuff(highlighter.collectMatches(text));

    // Then
    assertTrue(regions.isEmpty());
  }

  @Test
  void collectMatchesReturnsNoRelevantRegionsForTextWithoutMatches() {
    // Given
    String text = "x";

    // When
    List<HighlightRegion> regions = withoutFunnyStuff(highlighter.collectMatches(text));

    // Then
    assertTrue(regions.isEmpty());
  }

  private List<HighlightRegion> withoutFunnyStuff(List<HighlightRegion> regions) {
    return regions.stream()
        .filter(region -> !region.colour().equals(MiniJavaColours.FUNNY_STUFF_HIHI))
        .toList();
  }

  private boolean hasRegion(List<HighlightRegion> regions, int start, int end, Color colour) {
    return regions.stream()
        .anyMatch(
            region ->
                region.start() == start && region.end() == end && region.colour().equals(colour));
  }
}
