import antlr.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        Path current = Paths.get("src/test.bj");
        String aPath = current.toAbsolutePath().toString();

        CharStream codePointCharStream = CharStreams.fromFileName(aPath);
        BlueJayLexer lexer = new BlueJayLexer(codePointCharStream);
        BlueJayParser parser = new BlueJayParser(new CommonTokenStream(lexer));
        ParseTree tree = parser.program();
        AntlrToProgram visitor = new AntlrToProgram();
        visitor.visit(tree);
    }
}