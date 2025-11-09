package chalkbox.stages.conformance.comparator;

import chalkbox.stages.conformance.comparator.flags.Flag;
import chalkbox.stages.conformance.comparator.flags.SingularFlag;
import java.lang.reflect.Field;

public class FieldComparator extends CodeComparator<Field> {

    public FieldComparator(Field expected, Field actual) {
        super(expected, actual);
        name =
        "Field " +
        expected.getDeclaringClass().getName() +
        "." +
        expected.getName();
    }

    @Override
    protected void compare(Field expected, Field actual) {
        compareModifier(expected.getModifiers(), actual.getModifiers());

        Flag modifierFlag = new SingularFlag<>(
            "Field type does not match.",
            expected.getType().getName(),
            actual.getType().getName()
        );
        flags.add(modifierFlag);
    }
}
