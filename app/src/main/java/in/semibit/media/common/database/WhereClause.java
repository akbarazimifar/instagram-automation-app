package in.semibit.media.common.database;

import com.google.firebase.firestore.core.Filter;

public class WhereClause<T> {
    String field;
    GenericOperator operator;
    T value;

    public WhereClause(String field, GenericOperator operator, T value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public static WhereClause of(String field, GenericOperator operator, String value){
        return new WhereClause(field,operator,value);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public GenericOperator getOperator() {
        return operator;
    }

    public void setOperator(GenericOperator operator) {
        this.operator = operator;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
