import edu.kit.kastel.checker.exclusivity.qual.*;

class MethodCall {
    // :: warning: inconsistent.constructor.type
    MethodCall() {}

    void mth(@ShrMut Foo arg) {}

    void invoke() {
        @ReadOnly Foo x;
        @ReadOnly Foo y;
        @ExclMut Foo a;
        x = new Foo();   // x is refined to @ExclMut
        mth(x);        // x is refined to @ShrMut
        a = x;           // invalid, x is not @ExclMut anymore
        // :: error : expr.invalid-ref
        y = a;
    }
}
