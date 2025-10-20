---------------------------- MODULE queue ----------------------------
EXTENDS Naturals


CONSTANT defaultInitValue

Spec == True


MutEx == True

Freedom == Spec /\ MutEx

=============================================================================
