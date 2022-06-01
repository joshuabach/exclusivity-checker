import edu.kit.kastel.checker.exclusivity.qual.*;

// Test reference splitting rules for the Exclusivity Checker.
class ReferenceSplitting {
    // :: warning: inconsistent.constructor.type
    public ReferenceSplitting() {}

    void splitMut() {
        @ReadOnly Foo x;
        @ShrMut Foo a;
        @ExclMut Foo b;
        x = new Foo();      // x is refined to @ExclMut
        a = x;              // x is updated to @ShrMut
        // :: error: type.invalid
        b = x;              // invalid, x is not @ExclMut anyomre
    }

    void splitImmut() {
        @ReadOnly Foo x;
        @Immutable Foo a;
        @ExclMut Foo b;
        x = new Foo();      // x is refined to @ExclMut
        a = x;              // x is updated to @Immut
        // :: error: type.invalid
        b = x;              // invalid, x is not @ExclMut anyomre
    }

    void refTransfer() {
        @ReadOnly Foo x;
        @ExclMut Foo a;
        @ExclMut Foo b;

        x = new Foo();  // x is refined to @ExclMut
        a = x;          // x is updated to @ReadOnly
        // :: error: type.invalid
        b = x;          // invalid, x is not @ExclMut anymore
    }
}
