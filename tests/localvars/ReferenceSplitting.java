import edu.kit.kastel.checker.exclusivity.qual.*;

// Test reference splitting rules for the Exclusivity Checker.
class ReferenceSplitting {
    // :: warning: inconsistent.constructor.type
    public ReferenceSplitting() {}

    void splitMut() {
        @ReadOnly Foo x;
        @ReadOnly Foo y;
        @ShrMut Foo a;
        @ExclMut Foo b;
        x = new Foo();      // x is refined to @ExclMut
        a = x;              // x is updated to @ShrMut
        b = x;              // invalid, x is not @ExclMut anyomre
        // :: error: expr.invalid-ref
        y = b;
    }

    void splitImmut() {
        @ReadOnly Foo x;
        @ReadOnly Foo y;
        @Immutable Foo a;
        @ExclMut Foo b;
        x = new Foo();      // x is refined to @ExclMut
        a = x;              // x is updated to @Immut
        b = x;              // invalid, x is not @ExclMut anyomre
        // :: error: expr.invalid-ref
        y = b;
    }

    void refTransfer() {
        @ReadOnly Foo x;
        @ReadOnly Foo y;
        @ExclMut Foo a;
        @ExclMut Foo b;

        x = new Foo();  // x is refined to @ExclMut
        a = x;          // x is updated to @ReadOnly
        b = x;          // invalid, x is not @ExclMut anymore
        // :: error: expr.invalid-ref
        y = b;
    }
}
