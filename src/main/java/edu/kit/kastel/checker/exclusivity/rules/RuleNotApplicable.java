package edu.kit.kastel.checker.exclusivity.rules;

import org.checkerframework.dataflow.cfg.node.Node;

@SuppressWarnings("serial")
public class RuleNotApplicable extends Exception {
    public RuleNotApplicable(String name, Node node, String reason) {
        super(String.format(
                "Rule %s not applicable to %s (%s)",
                name, node, reason
        ));
    }
}
