package highlighting.regex;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaColours;
import highlighting.presets.MiniJavaTokens;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Implement a simple regex-based highlighting strategy. Unlike the scanning approach, this
// strategy applies each token independently to the entire input text and collects all resulting
// {@code HighlightRegion}s, even if they overlap. Conflicts are resolved in a separate step.

// TODO: Make this class extend {@code SyntaxHighlighter}, implement the abstract method {@code
// collectMatches}, and override {@code resolveConflicts} to handle overlapping regions produced by
// the naive regex-based strategy.
public class RegexHighlighter extends SyntaxHighlighter {

    private final Random random = new Random();
  // TODO: For each token, find all matches of its pattern in the input text, convert them into
  // {@code HighlightRegion}s, and combine all of these regions into a single list.
  @Override
  public List<HighlightRegion> collectMatches(String text) {
      List<HighlightRegion> regions = new ArrayList<>();

      for (Token token : MiniJavaTokens.defaultTokens()) {
          Matcher matcher = token.pattern().matcher(text);

          while (matcher.find()) {
              regions.add(
                  new HighlightRegion(
                      matcher.start(),
                      matcher.end(),
                      colourFor(token)
                  )
              );
          }
      }

      return regions;
  }

  // Kleine Hilfsmethode um funny stuff zu machen. Das verwenden von Color als identifier ist unsauber aber hatte keine Lust nen Token-Typ oder so hinzuzufügen lol
    private Color colourFor(Token token) {
        if (token.colour().equals(MiniJavaColours.FUNNY_STUFF_HIHI)) {
            return new Color(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            );
        }

        return token.colour();
    }

  // TODO: Resolve overlapping regions. Assume that {@code regions} has been normalised and sorted.
  // For any overlapping regions, keep the one that appears first in this list (which reflects the
  // token order) and discard all later overlapping regions. Longer regions that start at the same
  // position are preferred because of the sorting in {@code normalize}.
  @Override
  public List<HighlightRegion> resolveConflicts(List<HighlightRegion> regions) {
    List<HighlightRegion> result = new ArrayList<>();
    for(HighlightRegion current: regions){
        if(result.isEmpty()){
            result.add(current);
            continue;
        }

        HighlightRegion previous = result.getLast();

        if(current.start() >= previous.end()){
            result.add(current);
        }
    }
    return result;
  }
}
