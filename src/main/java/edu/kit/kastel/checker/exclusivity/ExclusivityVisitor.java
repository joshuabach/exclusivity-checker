package edu.kit.kastel.checker.exclusivity;

import com.sun.source.tree.*;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

public class ExclusivityVisitor extends BaseTypeVisitor<ExclusivityAnnotatedTypeFactory> {
    public ExclusivityVisitor(BaseTypeChecker checker) {
        super(checker);
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        System.out.printf("%s: %s = %s\n", node,
                atypeFactory.getAnnotatedType(node.getVariable()),
                atypeFactory.getAnnotatedType(node.getExpression()));

        visitExpression(node.getExpression());

        try {
            MemberSelectTree lhs = (MemberSelectTree) node.getVariable();
            try {
                IdentifierTree ident = (IdentifierTree) lhs.getExpression();
                if (ident.getName().contentEquals("this")) {
                    if (!atypeFactory.isMutable(atypeFactory.getAnnotatedType(ident))) {
                        // T-Assign: lhs is local var OR this is modifiable
                        checker.reportError(node, "assignment.this-not-writable");
                    }
                } else {
                    // Field access is only allowed to fields of this, not other objects
                    checker.reportError(node, "assignment.invalid-lhs");
                }
            } catch (ClassCastException e) {
                // No field access to arbitrary expressions is allowed
                checker.reportError(node, "assignment.invalid-lhs");
            }
        } catch (ClassCastException ignored) {}

        //return super.visitAssignment(node, p);
        return p;
    }

    void visitExpression(ExpressionTree expr) {
        if (!atypeFactory.isValid(atypeFactory.getAnnotatedType(expr))) {
            checker.reportError(expr, "expr.invalid-ref");
        }
    }

    @Override
    public Void visitExpressionStatement(ExpressionStatementTree node, Void unused) {
        visitExpression(node.getExpression());
        return super.visitExpressionStatement(node, unused);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        // TODO What do we need to do here?
        return p;
    }
}
