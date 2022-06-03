package edu.kit.kastel.checker.exclusivity.rules;

import com.sun.tools.javac.code.Type;
import edu.kit.kastel.checker.exclusivity.ExclusivityAnnotatedTypeFactory;
import org.checkerframework.dataflow.cfg.node.MethodInvocationNode;
import org.checkerframework.dataflow.cfg.node.Node;
import org.checkerframework.framework.flow.CFAbstractAnalysis;
import org.checkerframework.framework.flow.CFStore;
import org.checkerframework.framework.flow.CFTransfer;
import org.checkerframework.framework.flow.CFValue;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class TMethodInvocation extends AbstractTypeRule<MethodInvocationNode> {
    public TMethodInvocation(CFStore store, ExclusivityAnnotatedTypeFactory factory, CFAbstractAnalysis<CFValue, CFStore, CFTransfer> analysis) {
        super(store, factory, analysis);
    }

    @Override
    protected void applyInternal(MethodInvocationNode node) throws RuleNotApplicable {
        Node receiver = node.getTarget().getReceiver();
        TypeMirror receiverType;
        receiverType = node.getTarget().getMethod().getReceiverType();

        if (receiverType == null) {
            System.err.printf("warning: ignoring call to method without explicit 'this' parameter declaration: %s\n", node.getTarget());
            return;
        }

        System.out.printf("%s(", node.getTarget().getMethod().getSimpleName());
        AnnotationMirror receiverTypeAnno = factory.getExclusivityAnnotation(receiverType.getAnnotationMirrors());
        new TAssign(store, factory, analysis).applyOrInvalidate(receiverTypeAnno, receiver);

        // "param_i = arg_i;"
        int i = 0;
        for (VariableElement paramDecl : node.getTarget().getMethod().getParameters()) {
            Node paramValue = node.getArgument(i++);
            AnnotationMirror paramTypeAnno = factory.getExclusivityAnnotation(paramDecl.asType().getAnnotationMirrors());
            new TAssign(store, factory, analysis).applyOrInvalidate(paramTypeAnno, paramValue);
        }
        System.out.print(")");

        // Rule is for x = mth(...), logic for refinement of x is in T-Method-Invocation-Helper
        TypeMirror returnType = node.getTarget().getMethod().getReturnType();
        if (!(returnType instanceof Type.JCVoidType)) {
            AnnotationMirror returnTypeAnno = factory.getExclusivityAnnotation(returnType.getAnnotationMirrors());
            System.out.printf(" -> %s\n", prettyPrint(returnTypeAnno));
        }

        // TODO Remove possibly invalidated refinements
    }

    @Override
    public String getName() {
        return "T-Method-Invocation";
    }
}
