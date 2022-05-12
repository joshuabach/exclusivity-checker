import edu.kit.kastel.checker.exclusivity.qual.*;

class Foo {
    // :: warning: inconsistent.constructor.type
    public Foo() {}
}

// Test reference splitting rules for the Exclusivity Checker.
class ReferenceSplitting {
    @ExclMut Foo foo;

    // :: warning: inconsistent.constructor.type
    public ReferenceSplitting() {}

    void assignReadOnlyThis(@ReadOnly ReferenceSplitting this) {
        // :: error: assignment.this-not-writable
        this.foo = new Foo();
    }

    void assignWritableThis(@ExclMut ReferenceSplitting this) {
        this.foo = new Foo();
    }

    void refNew() {
        @ReadOnly Foo x;
        x = new Foo();  // T-Ref-New
        @ExclMut Foo y;
        y = x;          // T-Ref-Transfer
    }

    void refCopyRo(@ExclMut Foo a) {
        @ReadOnly Foo x;
        @ReadOnly Foo y;
        @ReadOnly Foo z;

        // a stays @ExclMut for all of these
        x = a;
        y = a;
        z = a;
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

    void splitImmut() {
        @ReadOnly Foo x;
        @ReadOnly Foo y;
        @Immutable Foo a;
        @ExclMut Foo b;
        x = new Foo();      // x is refined to @ExclMut
        a = x;              // x is updated to @Immut
        b = x;              // invalid, x is not @ExclMut anyomre
        // :: error: assignment.use-killed-ref
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
        // :: error: assignment.use-killed-ref
        y = b;
    }
}
