import org.checkerframework.checker.exclusivity.qual.*;

// Test basic subtyping relationships for the Exclusivity Checker.
class SubtypeTest {
    void allSubtypingRelationships(@ExclusivityUnknown int x, @ExclusivityBottom int y) {
        @ExclusivityUnknown int a = x;
        @ExclusivityUnknown int b = y;
        // :: error: assignment
        @ExclusivityBottom int c = x; // expected error on this line
        @ExclusivityBottom int d = y;
    }
}
