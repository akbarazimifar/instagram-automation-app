package in.semibit.media.common.database;

import com.google.firebase.firestore.core.Filter;

public class WhereClause {
    String field;
    Filter.Operator operator;
    String value;

    public WhereClause(String field, Filter.Operator operator, String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public static WhereClause of(String field, Filter.Operator operator, String value){
        return new WhereClause(field,operator,value);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Filter.Operator getOperator() {
        return operator;
    }

    public void setOperator(Filter.Operator operator) {
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
