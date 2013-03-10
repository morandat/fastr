package r.builtins;

import r.data.*;
import r.errors.*;
import r.nodes.*;
import r.nodes.truffle.*;

import com.oracle.truffle.api.frame.*;

/**
 * <pre>
 * x, y -- numeric or complex vectors or objects which can be coerced to such, or other objects for which methods have been 
 *         written.
 * </pre>
 */
abstract class OperationsBase extends CallFactory {

    OperationsBase(String name) {
        super(name, new String[]{"x", "y"}, null);
    }

    public static RNode idempotentNumeric(ASTNode ast, RSymbol[] names, RNode[] exprs) {
        return new BuiltIn.BuiltIn1(ast, names, exprs) {

            @Override public RAny doBuiltIn(Frame frame, RAny arg) {
                if (arg instanceof RDouble || arg instanceof RInt || arg instanceof RLogical) { return arg; }
                throw RError.getInvalidArgTypeUnary(ast);
            }
        };
    }
}
