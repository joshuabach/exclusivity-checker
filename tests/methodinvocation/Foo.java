import edu.kit.kastel.checker.exclusivity.qual.*;

class Foo {
    // :: warning: inconsistent.constructor.type
    public Foo() {}

    public void mth(@ExclMut Foo this) {}
}

