package edu.kit.kastel.checker.exclusivity.rules;

import org.checkerframework.dataflow.cfg.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChainRule<R extends AssignmentRule> implements TypeRule{
    List<R> typeRules;

    @SafeVarargs
    public ChainRule(R... typeRules) {
        this.typeRules = new ArrayList<R>(typeRules.length);
        for (R rule : typeRules) {
            this.typeRules.add(rule);
        }
    }

    @Override
    public void apply(Node node) throws RuleNotApplicable {
        for (int i = 0; i < typeRules.size(); ++i) {
            R rule = typeRules.get(i);
            try {
                rule.apply(node);
                break;
            } catch (RuleNotApplicable ignored) {
                if (i == typeRules.size() - 1) {
                    throw new RuleNotApplicable(getName(), node, "no rule was applicable");
                }
            }
        }
    }

    @Override
    public String getName() {
        return typeRules.stream().map(TypeRule::getName).collect(Collectors.joining(","));
    }
}
