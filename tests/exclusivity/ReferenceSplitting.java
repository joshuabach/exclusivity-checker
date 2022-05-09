import edu.kit.kastel.checker.exclusivity.qual.*;

class Foo {
    // :: warning: inconsistent.constructor.type
    public Foo() {}
}

// Test reference splitting rules for the Exclusivity Checker.
class ReferenceSplitting {
    // :: warning: inconsistent.constructor.type
    public ReferenceSplitting() {}

    void refNew() {
        @ReadOnly Foo x;
        x = new Foo();  // T-Ref-New
        @ExclMut Foo y;
        y = x;          // T-Ref-Transfer
    }

    void splitMut() {
        @ReadOnly Foo x;
        @ReadOnly Foo y;
        @ShrMut Foo a;
        @ExclMut Foo b;
        x = new Foo();      // x is refined to @ExclMut
        a = x;              // x is updated to @ShrMut
        b = x;              // invalid, x is not @ExclMut anyomre
        // :: error: assignment.use-killed-ref
        y = b;
    }
}
