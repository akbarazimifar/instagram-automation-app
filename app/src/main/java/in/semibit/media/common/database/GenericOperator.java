package in.semibit.media.common.database;

public enum GenericOperator {
    LESS_THAN("<"),

    LESS_THAN_OR_EQUAL("<="),

    EQUAL("=="),

    NOT_EQUAL("!="),

    GREATER_THAN(">"),

    GREATER_THAN_OR_EQUAL(">="),

    ARRAY_CONTAINS("array_contains"),

    ARRAY_CONTAINS_ANY("array_contains_any"),

    IN("in"),

    NOT_IN("not_in"),

    LIMIT("LIMIT"),

    OFFSETAFTER("OFFSET")


    ;

    private final String text;

    GenericOperator(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
