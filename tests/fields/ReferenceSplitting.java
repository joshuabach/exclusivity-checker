import edu.kit.kastel.checker.exclusivity.qual.*;

// Test reference splitting rules for the Exclusivity Checker.
class ReferenceSplitting {
    @ExclMut Foo field;
    // :: warning: inconsistent.constructor.type
    public ReferenceSplitting() {}

    void refTransfer() {
        @ReadOnly Foo x;
        @ExclMut Foo a;

        x = new Foo();  // x is refined to @ExclMut
        a = x;          // x is updated to @ReadOnly
        // :: error: type.invalid
        this.field = x; // invalid, x is not @ExclMut anymore
    }
}
