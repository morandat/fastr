package r.builtins;

import java.util.*;

import r.*;
import r.data.*;
import r.data.internal.*;

public class Primitives {

    public static final boolean STATIC_LOOKUP = false;

    private static Map<RSymbol, PrimitiveEntry> map;
    static {
        map = new HashMap<>();
        initializePrimitives();
    }

    public static final class PrimitiveEntry {

        final RSymbol name;
        final CallFactory factory;
        final RBuiltIn builtIn;

        PrimitiveEntry(RSymbol name, CallFactory bodyFactory) {
            this.name = name;
            this.factory = bodyFactory;
            this.builtIn = new BuiltInImpl(factory);
        }

        @Override public String toString() {
            return "PrimitiveEntry[" + name + "," + factory + "," + builtIn;
        }
    }

    public static void initializePrimitives() {
        map.clear();
        add(":", Colon._);
        add("+", OpAdd._);
        add("-", OpSub._);
        add("*", OpMult._);
        add("/", OpDiv._);
        add("==", OpEq._);
        add("!=", OpNe._);
        add(">", OpGt._);
        add("<", OpLt._);
        add("<=", OpLe._);
        add(">=", OpGe._);
        add("%%", OpMod._);
        add("%/%", OpIntDiv._);
        add("^", OpPow._);
        add("%*%", OpMatMult._);
        add("%o%", OpOuterMult._);
        add("|", OpOrElem._);
        add("&", OpAndElem._);
        add("||", OpOr._);
        add("&&", OpAnd._);
        add("!", OpNot._);
        add(Abs._);
        add(Aperm._);
        add(Array._);
        add(Assign._);
        add(AsCharacter._);
        add(AsComplex._);
        add(AsDouble._);
        add(AsEnvironment._);
        add(AsInteger._);
        add(AsLogical._);
        add(AsRaw._);
        add(AsVector._);
        add(Attr._);
        add(AttrAssign._);
        add(Attributes._);
        add(AttributesAssign._);
        add(C._);
        add(Cat._);
        add(Character._);
        add(Close._);
        add(ColMeans._);
        add(ColSums._);
        add(Cumsum._);
        add(DiagAssign._);
        add(Dim._);
        add(Double._);
        add(Get._);
        add(Gregexpr._);
        add(Gsub._);
        add(Eigen._);
        add(Emptyenv._);
        add(Exists._);
        add(Exp._);
        add(File._);
        add(Flush._);
        add(Integer._);
        add(IsCharacter._);
        add(IsComplex._);
        add(IsDouble._);
        add(IsInteger._);
        add(IsList._);
        add(IsLogical._);
        add(IsNull._);
        add(IsNumeric._);
        add(IsNA._);
        add(IsRaw._);
        add(LApply._);
        add(Length._);
        add(LengthAssign._);
        add(List._);
        add(Log._);
        add(Log10._);
        add(Log2._);
        add(Logical._);
        add(LowerTri._);
        add(Ls._);
        add(Matrix._);
        add(Max._);
        add(Min._);
        add(Names._);
        add(NamesAssign._);
        add(Nchar._);
        add(Ncol._);
        add(Newenv._);
        add(Nrow._);
        add(Options._);
        add("order", Sort._);
        add(Outer._);
        add(Paste._);
        add(Pipe._);
        add(Raw._);
        add(ReadLines._);
        add(Regexpr._);
        add(Rep._);
        add("rep.int", RepInt._);
        add(Return._);
        add(Rev._);
        add("rev.default", Rev._);
        add(RowMeans._);
        add(RowSums._);
        add(SApply._);
        add(Scan._);
        add(Seq._); // in fact seq.default (and only part of it)
        add("seq.default", Seq._);
        add(Strsplit._);
        add(Sub._);
        add(Substr._);
        add(Substring._);
        add(Sum._);
        add(Sqrt._);
        add(T._);
        add("t.default", T._);
        add(Tolower._);
        add(Toupper._);
        add(Typeof._);
        add(Unlist._);
        add(UpperTri._);
        add(Which._);
        add(WriteBin._);
        add(CommandArgs._);
        // fastr specific
        add("__inspect", Inspect._);
    }

    public static boolean hasCallFactory(final RSymbol name, final RFunction enclosing) {
        return Primitives.get(name, enclosing) != null;
    }

    public static CallFactory getCallFactory(RSymbol name, RFunction enclosing) {
        final PrimitiveEntry pe = Primitives.get(name, enclosing);
        if (pe == null) {
            return null;
        } else {
            return pe.factory;
        }
    }

    public static RBuiltIn getBuiltIn(RSymbol name, RFunction enclosing) {
        final PrimitiveEntry pe = Primitives.get(name, enclosing);
        if (pe == null) {
            return null;
        } else {
            return pe.builtIn;
        }
    }

    public static PrimitiveEntry get(RSymbol name, RFunction fun) {
        PrimitiveEntry pe = get(name);
        if (pe != null && fun != null && fun.isInWriteSet(name)) { // TODO: fix these checks
            Utils.debug("IGNORING over-shadowing of built-in " + name.pretty() + "!!!");
            throw Utils.nyi(); // TODO the case when a primitive is shadowed by a local symbol
            // FIXME: but shouldn't we keep traversing recursively through all frames of the caller?
            // FIXME: also, what about reflections?
        }
        return pe;
    }

    public static PrimitiveEntry get(RSymbol name) {
        return map.get(name);
    }

    private static void add(String name, CallFactory body) {
        add(RSymbol.getSymbol(name), body);
    }

    private static void add(CallFactory body) {
        add(body.name(), body);
    }

    private static void add(RSymbol sym, CallFactory body) {
        map.put(sym, new PrimitiveEntry(sym, body));
    }
}
