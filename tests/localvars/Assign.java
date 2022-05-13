import edu.kit.kastel.checker.exclusivity.qual.*;

class Assign {
    @ExclMut Foo foo;

    // :: warning: inconsistent.constructor.type
    public Assign() {}

    void assignReadOnlyThis(@ReadOnly Assign this) {
        // :: error: assignment.this-not-writable
        this.foo = new Foo();
    }

    void assignWritableThis(@ExclMut Assign this) {
        this.foo = new Foo();
    }
}