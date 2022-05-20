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
        @ReadOnly Foo y;
        @ExclMut Foo a;
        x = new Foo();   // x is refined to @ExclMut
        this.mth(x);        // x is refined to @ShrMut
        a = x;           // invalid, x is not @ExclMut anymore
        // :: error: expr.invalid-ref
        y = a;
    }

    void invokeAssign() {
        @ExclMut Foo b;
        b = this.mthret();
    }
}
