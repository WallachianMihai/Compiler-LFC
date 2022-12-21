import BlueJay.BlueJayBaseVisitor;
import BlueJay.BlueJayLexer;
import BlueJay.BlueJayParser;
import org.antlr.v4.runtime.misc.*;

import java.util.*;

public class AntlrToProgram extends BlueJayBaseVisitor<Value>
{
    public static final double SMALL_VALUE = 0.00000000001;
    public static int CURRENT_DEPTH = 0;
    //1. duplicate declaration 2. reference to undeclared variables
    private List<String> semanticErrors = new ArrayList<>();
    private Map<String, Value> memory = new HashMap<String, Value>();
    private Vector<Set<String>> depthMemory = new Vector<Set<String>>(0);

    @Override
    public Value visitProgram(BlueJayParser.ProgramContext ctx) {
        depthMemory.add(0, new HashSet<String>());
        return super.visitProgram(ctx);
    }

    @Override
    public Value visitAssignment(BlueJayParser.AssignmentContext ctx)
    {
        String id = ctx.ID().getText();
        Value value = this.visit(ctx.expr());
        if (!memory.containsKey(id))
            depthMemory.elementAt(CURRENT_DEPTH).add(id);
        memory.put(id, value);
        return value;
    }

    @Override
    public Value visitIdAtom(BlueJayParser.IdAtomContext ctx)
    {
        String id = ctx.ID().getText();
        Value value = memory.get(id);
        if(value == null)
        {
            throw new RuntimeException("undefined symbol: " + id);
        }
        return value;
    }

    @Override
    public Value visitNumberAtom(BlueJayParser.NumberAtomContext ctx)
    {
        return new Value(Double.valueOf(ctx.getText()));
    }

    @Override
    public Value visitStringAtom(BlueJayParser.StringAtomContext ctx)
    {
        String str = ctx.getText();
        str = str.substring(1, str.length()-1).replace("\"\"", "\"");
        return new Value(str);
    }

    @Override
    public Value visitBooleanAtom(BlueJayParser.BooleanAtomContext ctx)
    {
        return new Value(Boolean.valueOf(ctx.getText()));
    }

    @Override
    public Value visitNilAtom(BlueJayParser.NilAtomContext ctx)
    {
        return new Value(null);
    }

    @Override
    public Value visitParExpr(BlueJayParser.ParExprContext ctx)
    {
        return this.visit(ctx.expr());
    }


    @Override
    public Value visitNotExpr(BlueJayParser.NotExprContext ctx)
    {
        Value value = this.visit(ctx.expr());
        return new Value(!value.asBoolean());
    }

    @Override
    public Value visitMinusExpr(BlueJayParser.MinusExprContext ctx)
    {
        Value value = this.visit(ctx.expr());
        return new Value(-value.asFloatingPoint());
    }

    @Override
    public Value visitMultiplicationExpr(@NotNull BlueJayParser.MultiplicationExprContext ctx)
    {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));

        switch(ctx.op.getType())
        {
            case BlueJayParser.MULT:
                return new Value(left.asFloatingPoint() * right.asFloatingPoint());
            case BlueJayParser.DIV:
                return new Value(left.asFloatingPoint() / right.asFloatingPoint());
            case BlueJayParser.MOD:
                return new Value(left.asFloatingPoint() % right.asFloatingPoint());
            default:
                throw new RuntimeException("unknown operator: " + BlueJayParser.tokenNames[ctx.op.getType()]);
        }
    }

    @Override
    public Value visitAdditiveExpr(@NotNull BlueJayParser.AdditiveExprContext ctx)
    {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));

        switch(ctx.op.getType())
        {
            case BlueJayParser.PLUS:
                return left.isDouble() && right.isDouble() ?
                        new Value(left.asFloatingPoint() + right.asFloatingPoint()) :
                        new Value(left.asString() + right.asString());
            case BlueJayParser.MINUS:
                return new Value(left.asFloatingPoint() / right.asFloatingPoint());
            default:
                throw new RuntimeException("unknown operator: " + BlueJayParser.tokenNames[ctx.op.getType()]);
        }
    }

    @Override
    public Value visitRelationalExpr(@NotNull BlueJayParser.RelationalExprContext ctx)
    {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));

        switch(ctx.op.getType())
        {
            case BlueJayParser.LT:
                return new Value(left.asFloatingPoint() < right.asFloatingPoint());
            case BlueJayParser.LTEQ:
                return new Value(left.asFloatingPoint() <= right.asFloatingPoint());
            case BlueJayParser.GT:
                return new Value(left.asFloatingPoint() > right.asFloatingPoint());
            case BlueJayParser.GTEQ:
                return new Value(left.asFloatingPoint() >= right.asFloatingPoint());
            default:
                throw new RuntimeException("unknown operator: " + ctx.op.getType());
        }
    }

    @Override
    public Value visitEqualityExpr(@NotNull BlueJayParser.EqualityExprContext ctx) {

        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));

        switch (ctx.op.getType())
        {
            case BlueJayParser.EQ:
                return left.isDouble() && right.isDouble() ?
                        new Value(Math.abs(left.asFloatingPoint() - right.asFloatingPoint()) < SMALL_VALUE) :
                        new Value(left.equals(right));
            case BlueJayLexer.NEQ:
                return left.isDouble() && right.isDouble() ?
                        new Value(Math.abs(left.asFloatingPoint() - right.asFloatingPoint()) >= SMALL_VALUE) :
                        new Value(!left.equals(right));
            default:
                throw new RuntimeException("unknown operator: " + ctx.op.getType());
        }
    }

    @Override
    public Value visitAndExpr(BlueJayParser.AndExprContext ctx)
    {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        return new Value(left.asBoolean() && left.asBoolean());
    }

    @Override
    public Value visitOrExpr(BlueJayParser.OrExprContext ctx) {
        Value left = this.visit(ctx.expr(0));
        Value right = this.visit(ctx.expr(1));
        return new Value(left.asBoolean() || right.asBoolean());
    }


    @Override
    public Value visitPreDec(BlueJayParser.PreDecContext ctx)
    {
        String id = ctx.ID().toString();
        Value value = memory.get(id);
        if(value == null)
        {
            throw new RuntimeException("undefined symbol: " + id);
        }
        memory.put(id, new Value(memory.get(id).asFloatingPoint() - 1));
        return memory.get(id);
    }

    @Override
    public Value visitPreInc(BlueJayParser.PreIncContext ctx)
    {
        String id = ctx.ID().toString();
        Value value = memory.get(id);
        if(value == null)
        {
            throw new RuntimeException("undefined symbol: " + id);
        }
        memory.put(id, new Value(memory.get(id).asFloatingPoint() + 1));
        return memory.get(id);
    }

    @Override
    public Value visitPostDec(BlueJayParser.PostDecContext ctx)
    {
        String id = ctx.ID().toString();
        Value value = memory.get(id);
        if(value == null)
        {
            throw new RuntimeException("undefined symbol: " + id);
        }
        memory.put(id, new Value(memory.get(id).asFloatingPoint() - 1));
        return value;
    }

    @Override
    public Value visitPostInc(BlueJayParser.PostIncContext ctx)
    {
        String id = ctx.ID().toString();
        Value value = memory.get(id);
        if(value == null)
        {
            throw new RuntimeException("undefined symbol: " + id);
        }
        memory.put(id, new Value(memory.get(id).asFloatingPoint() + 1));
        return value;
    }

    @Override
    public Value visitCompoundAssignment(BlueJayParser.CompoundAssignmentContext ctx)
    {
        String id = ctx.ID().toString();
        Value value = this.visit(ctx.expr());
        switch (ctx.op.getType())
        {
            case BlueJayParser.PLUSASSIGN:
               if(value.isDouble())
               {
                   memory.put(id, new Value(memory.get(id).asFloatingPoint() + value.asFloatingPoint()));
               }
               else
               {
                   memory.put(id, new Value(memory.get(id).asString() + value.asString()));
               }
               break;
            case BlueJayParser.MINUSASSIGN:
                memory.put(id, new Value(memory.get(id).asFloatingPoint() - value.asFloatingPoint()));
                break;
            case BlueJayParser.MODASSIGN:
                memory.put(id, new Value(memory.get(id).asFloatingPoint() % value.asFloatingPoint()));
                break;
            case BlueJayParser.DIVASSIGN:
                memory.put(id, new Value(memory.get(id).asFloatingPoint() / value.asFloatingPoint()));
                break;
            case BlueJayParser.MULTASSIGN:
                memory.put(id, new Value(memory.get(id).asFloatingPoint() * value.asFloatingPoint()));
                break;
        }
        return this.visit(ctx.ID());
    }

    @Override
    public Value visitPrintValue(BlueJayParser.PrintValueContext ctx) {
        Value value = this.visit(ctx.expr());
        System.out.print(value.toString());
        return value;
    }

    @Override
    public Value visitPrintNewLine(BlueJayParser.PrintNewLineContext ctx) {
        Value value = this.visit(ctx.expr());
        System.out.println(value.toString());
        return value;
    }

    @Override
    public Value visitCondition_block(BlueJayParser.Condition_blockContext ctx) {
        return this.visit(ctx.expr());
    }

    @Override
    public Value visitIf_statement_block(BlueJayParser.If_statement_blockContext ctx) {
        return this.visit(ctx.block());
    }

    @Override
    public Value visitIf_condition_block(BlueJayParser.If_condition_blockContext ctx) {
        if (this.visit(ctx.condition_block()).asBoolean())
            return this.visit(ctx.if_statement_block());
        return null;
    }

    @Override
    public Value visitIf_stat(BlueJayParser.If_statContext ctx) {
        boolean goOnElse = true;
        for (BlueJayParser.If_condition_blockContext block : ctx.if_condition_block()) {
            if (this.visit(block) != null)
                goOnElse = false;
        }
        if (ctx.block() != null && goOnElse)
            return this.visit(ctx.block());
        return null;
    }

    @Override
    public Value visitWhile_condition_block(BlueJayParser.While_condition_blockContext ctx) {
        while (this.visit(ctx.condition_block()).asBoolean())
            this.visit(ctx.loop_statement_block());
        return null;
    }

    @Override
    public Value visitWhile_stat(BlueJayParser.While_statContext ctx) {
        return this.visit(ctx.while_condition_block());
    }

    @Override
    public Value visitFor_stat(BlueJayParser.For_statContext ctx) {
        while (this.visit(ctx.for_condition_block()).asBoolean()) {
            this.visit(ctx.loop_statement_block());
            this.visit(ctx.for_condition_block().expr(1));
        }
        return null;
    }

    @Override
    public Value visitFor_condition_block(BlueJayParser.For_condition_blockContext ctx) {
        if (ctx.assignment() != null && !memory.containsKey(ctx.assignment().ID().getText())) {
            this.visit(ctx.assignment());
        }
        return this.visit(ctx.expr(0));
    }

    @Override
    public Value visitLoop_statement_block(BlueJayParser.Loop_statement_blockContext ctx) {
        return this.visit(ctx.block());
    }

    @Override
    public Value visitBlock(BlueJayParser.BlockContext ctx) {
        CURRENT_DEPTH++;
        depthMemory.add(CURRENT_DEPTH, new HashSet<String>());
        super.visitBlock(ctx);

        for (String id : depthMemory.get(CURRENT_DEPTH))
            memory.remove(id);
        depthMemory.remove(CURRENT_DEPTH);
        CURRENT_DEPTH--;

        return null;
    }


}
