package in.semibit.media.common.database;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.core.Filter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import in.semibit.media.common.database.GenericCompletableFuture;

public class DatabaseHelper {

    FirebaseFirestore db;
    Source source;

    public DatabaseHelper(Source source) {
        db = FirebaseFirestore.getInstance();
        this.source = source;
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();
        db.setFirestoreSettings(settings);
    }

    public Task<Void> goOffline() {
        return db.disableNetwork();
    }

    public Task<Void> goOnline() {
        return db.enableNetwork();
    }

    public String tablePrefix(String tab) {
        return "smauto_" + tab;
    }

    public <T> GenericCompletableFuture<T> taskWrapper(Task<T> job) {
        GenericCompletableFuture<T> completableFuture = new GenericCompletableFuture<>();
        job.addOnCompleteListener(new OnCompleteListener<T>() {
            @Override
            public void onComplete(@NonNull Task<T> task) {
                if (!task.isSuccessful()) {
                    completableFuture.completeExceptionally(task.getException());
                    return;
                }
                completableFuture.complete(job.getResult());
            }
        });
        return completableFuture;
    }

    public GenericCompletableFuture<Void> save(String tableName, IdentifiedModel model) throws Exception {
        Task<Void> task = db.collection(tablePrefix(tableName)).document(model.getId()).set(model);
        return taskWrapper(task);
    }

    public GenericCompletableFuture<Void> save(String tableName, List<IdentifiedModel> model) throws Exception {
        WriteBatch batch = db.batch();
        for (IdentifiedModel data : model) {
            DocumentReference ref = db.collection(tablePrefix(tableName)).document(data.getId());
            batch.set(ref, data);
        }
        return taskWrapper(batch.commit());
    }


    public GenericCompletableFuture<Void> updateOne(String tableName, IdentifiedModel model) throws Exception {
        Task<Void> task = null;
        try {
            if (model.getId() == null) {
                throw new RuntimeException("ID must be present");
            }
            ObjectMapper m = new ObjectMapper();
            Map<String, Object> props = m.convertValue(model, Map.class);
            task = db.collection(tablePrefix(tableName)).document(model.getId()).update(props);
            return taskWrapper(task);
        } catch (Exception e) {
            e.printStackTrace();
            GenericCompletableFuture<Void> completableFuture = new GenericCompletableFuture<>();
            completableFuture.completeExceptionally(e);
            return completableFuture;
        }
    }

    public GenericCompletableFuture<Void> deleteOne(String tableName, IdentifiedModel model) throws Exception {
        Task<Void> task = null;
        try {
            if (model.getId() == null) {
                throw new RuntimeException("ID must be present");
            }
            ObjectMapper m = new ObjectMapper();
            Map<String, Object> props = m.convertValue(model, Map.class);
            task = db.collection(tablePrefix(tableName)).document(model.getId()).delete();
            return taskWrapper(task);
        } catch (Exception e) {
            e.printStackTrace();
            GenericCompletableFuture<Void> completableFuture = new GenericCompletableFuture<>();
            completableFuture.completeExceptionally(e);
            return completableFuture;
        }
    }


    public Query fromWhere(Query query, List<WhereClause> clause) {
        if (clause != null) {
            for (WhereClause cl : clause) {
                if (cl.operator == Filter.Operator.EQUAL) {
                    query = query.whereEqualTo(cl.getField(), cl.getValue());
                } else if (cl.operator == Filter.Operator.NOT_EQUAL) {
                    query = query.whereNotEqualTo(cl.getField(), cl.getValue());
                } else if (cl.operator == Filter.Operator.GREATER_THAN_OR_EQUAL) {
                    query = query.whereGreaterThanOrEqualTo(cl.getField(), cl.getValue());
                } else if (cl.operator == Filter.Operator.GREATER_THAN) {
                    query = query.whereGreaterThan(cl.getField(), cl.getValue());
                } else if (cl.operator == Filter.Operator.LESS_THAN) {
                    query = query.whereLessThan(cl.getField(), cl.getValue());
                } else if (cl.operator == Filter.Operator.LESS_THAN_OR_EQUAL) {
                    query = query.whereLessThanOrEqualTo(cl.getField(), cl.getValue());
                }
            }
        }
        return query;
    }

    public <T extends IdentifiedModel> GenericCompletableFuture<List<T>> query(String tableName, List<WhereClause> clause, Class<T> type) {
        Query query = db.collection(tablePrefix(tableName));
        query = fromWhere(query, clause);

        GenericCompletableFuture<List<T>> completableFuture = new GenericCompletableFuture<>();

        query.get(source).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    completableFuture.completeExceptionally(task.getException());
                    return;
                }
                completableFuture.complete(task.getResult().toObjects(type));
            }
        });

        return completableFuture;
    }

}
