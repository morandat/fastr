package r.builtins;

import r.data.*;
import r.nodes.*;
import r.nodes.truffle.*;

/**
 * "|"
 * 
 * <pre>
 * x, y -- numeric or complex vectors or objects which can be coerced to such, or other objects for which methods have been 
 *         written.
 * </pre>
 */
final class OpOrElem extends OperationsBase {
    static final CallFactory _ = new OpOrElem("|");

    private OpOrElem(String name) {
        super(name);
    }

    @Override public RNode create(ASTNode ast, RSymbol[] names, RNode[] exprs) {
        // exprs.length == 2
        return ElementwiseLogicalOperation.createUninitialized(ast, exprs[0], ElementwiseLogicalOperation.OR, exprs[1]);
    }

}
