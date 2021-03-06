package r.builtins;

import r.data.*;
import r.errors.*;
import r.nodes.*;
import r.nodes.truffle.*;

import com.oracle.truffle.api.frame.*;
import java.lang.Integer;

/**
 * "sum"
 * 
 * <pre>
 * ... -- numeric or complex or logical vectors.
 * na.rm -- logical. Should missing values (including NaN) be removed?
 * </pre>
 */
// FIXME: optimize for single argument
// NOTE: we could probably get some performance if we gave up on preserving NA vs NaN in double computations; the current implementation strives to be strict
final class Sum extends CallFactory {

    static final CallFactory _ = new Sum("sum", new String[]{"...", "na.rm"}, new String[]{});

    Sum(String name, String[] params, String[] required) {
        super(name, params, required);
    }

    public static double sum(RDouble v, boolean narm) {
        int size = v.size();
        double res = 0;
        for (int i = 0; i < size; i++) {
            double d = v.getDouble(i);
            if (narm) {
                if (RDouble.RDoubleUtils.isNAorNaN(d)) {
                    continue;
                }
            } else {
                if (RDouble.RDoubleUtils.isNAorNaN(d)) { return d; }// FIXME: this is to retain NA vs NaN distinction, but indeed would have overhead in common case
            }
            res += d;
        }
        return res;
    }

    public static long sum(RInt v, boolean narm) {
        int size = v.size();
        long res = 0;
        for (int i = 0; i < size; i++) {
            long l = v.getInt(i);
            if (l == RInt.NA) {
                if (narm) {
                    continue;
                } else {
                    return RInt.NA;
                }
            } else {
                res += l;
            }
        }
        return res;
    }

    @Override public RNode create(ASTNode call, RSymbol[] names, RNode[] exprs) {
        if (exprs.length == 0) { return new Builtin.Builtin0(call, names, exprs) {
            @Override public RAny doBuiltIn(Frame frame) {
                return RInt.BOXED_ZERO;
            }
        }; }
        ArgumentInfo ia = check(call, names, exprs);

        final boolean neverRemoveNA = !ia.provided("na.rm");
        final int narmPosition = ia.provided("na.rm") ? ia.position("na.rm") : -1;

        return new Builtin(call, names, exprs) {
            @Override public RAny doBuiltIn(Frame frame, RAny[] args) {
                boolean naRM = false;
                if (!neverRemoveNA) {
                    RAny v = args[narmPosition];
                    if (v instanceof RLogical) {
                        RLogical l = (RLogical) v;
                        naRM = l.size() == 0 || l.getLogical(0) != RLogical.FALSE;
                    } else if (v instanceof RInt) {
                        RInt i = (RInt) v;
                        naRM = i.size() == 0 || i.getInt(0) != 0;
                    } else if (v instanceof RDouble) {
                        RDouble d = (RDouble) v;
                        naRM = d.size() == 0 || d.getDouble(0) != 0;
                    } else {
                        naRM = true;
                    }
                }
                boolean hasDouble = false;

                for (int i = 0; i < args.length; i++) {
                    if (!neverRemoveNA && i == narmPosition) {
                        continue;
                    }
                    RAny v = args[i];
                    if (v instanceof RDouble) {
                        hasDouble = true;
                    } else if (v instanceof RList) { throw RError.getInvalidTypeList(ast); }
                }

                if (hasDouble) {
                    double res = 0;
                    for (int i = 0; i < args.length; i++) {
                        if (!neverRemoveNA && i == narmPosition) {
                            continue;
                        }
                        RAny v = args[i];
                        if (v instanceof RNull) {
                            continue;
                        }
                        double d = sum(v.asDouble(), naRM);
                        if (RDouble.RDoubleUtils.isNAorNaN(d)) { // FIXME: this is to retain NA vs NaN distinction, but indeed would have overhead in common case
                            res = d;
                            break;
                        } else {
                            res += d;
                        }
                    }
                    return RDouble.RDoubleFactory.getScalar(res);
                } else {
                    long res = 0;
                    for (int i = 0; i < args.length; i++) {
                        if (!neverRemoveNA && i == narmPosition) {
                            continue;
                        }
                        RAny v = args[i];
                        if (v instanceof RNull) {
                            continue;
                        }
                        res += sum(v.asInt(), naRM);
                    }
                    if (!(res < Integer.MIN_VALUE || res > Integer.MAX_VALUE)) { // FIXME: this may not rigorously reflect R semantics, check if the
                                                                                 //        range should be checked for individual elements or not
                        return RInt.RIntFactory.getScalar((int) res);
                    } else {
                        return RInt.BOXED_NA;
                    }

                }
            }
        };
    }
}
