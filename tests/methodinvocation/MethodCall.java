import edu.kit.kastel.checker.exclusivity.qual.*;

class MethodCall {
    // :: warning: inconsistent.constructor.type
    MethodCall() {}

    void mth(@ShrMut MethodCall this, @ShrMut Foo arg) {}

    @ExclMut Foo
    mthret(@ShrMut MethodCall this) {
        return new Foo();
    }

    void invoke() {
        @ReadOnly Foo x;
        @ExclMut Foo a;
        x = new Foo();   // x is refined to @ExclMut
        this.mth(x);     // x is refined to @ShrMut
        // :: type.invalid
        a = x;           // invalid, x is not @ExclMut anymore
    }

    void invokeAssign() {
        @ExclMut Foo b;
        b = this.mthret();
    }
}
