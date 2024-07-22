package com.example.vibora.utils;

import com.example.vibora.model.UserModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class FirebaseUtils {

    public static UserModel currentUserModel;

    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn(){
        if(currentUserId()!=null) return true;
        else return false;
    }

    public static Task<String> getUsernameFromId(String userId) {
        DocumentReference doc = FirebaseUtils.getUserDetails(userId);
        return doc.get().continueWith(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    return documentSnapshot.getString("username");
                }
            }
            return null;
        });
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }

    public static DocumentReference getUserDetails(String userId){
        return FirebaseFirestore.getInstance().collection("users").document(userId);
    }

    public static DocumentReference getFieldDetails(String fieldId){
        return FirebaseFirestore.getInstance().collection("fields").document(fieldId);
    }

    public static StorageReference getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(currentUserId());
    }

    public static StorageReference getFieldImageStorageRef(String imageName){
        return FirebaseStorage.getInstance().getReference().child("field_images")
                .child(imageName);
    }

    public static StorageReference getUserProfilePicStorageRef(String userId){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(userId);
    }

    public static void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static CollectionReference allFieldsCollectionReference(){
        return FirebaseFirestore.getInstance().collection("fields");
    }

    public static CollectionReference dailyBookingsCollectionReference(String fieldId, String formattedDay){
        return FirebaseFirestore.getInstance().collection("fields").document(fieldId).collection("bookings").document(formattedDay).collection("bookings");
    }

    public static CollectionReference allLessonsCollectionReference(){
        return FirebaseFirestore.getInstance().collection("lessons");
    }

    public static CollectionReference userReportsCollectionReference(String UserId){
        return FirebaseFirestore.getInstance().collection("reports").document(UserId).collection("reports");
    }

    public static Query allBookingsCollectionReference(){
        return FirebaseFirestore.getInstance().collectionGroup("bookings");
    }

    public static Task<Void> deleteField(String fieldId){
        return allFieldsCollectionReference().document(fieldId).delete();
    }

    public static Task<Void> updateFieldName(String fieldId, String name){
        DocumentReference oldDocRef = allFieldsCollectionReference().document(fieldId);
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        oldDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Map<String, Object> data = documentSnapshot.getData();
                data.replace("name", name);
                data.replace("fieldId", name);


                if (data != null) {
                    DocumentReference newDocRef = allFieldsCollectionReference().document(name);
                    newDocRef.set(data).addOnSuccessListener(aVoid -> {
                        oldDocRef.delete().addOnSuccessListener(aVoid1 -> {
                            taskCompletionSource.setResult(null); // Indica che l'operazione è completata con successo
                        }).addOnFailureListener(e -> {
                            taskCompletionSource.setException(e); // Propaga l'eccezione in caso di errore
                        });
                    }).addOnFailureListener(e -> {
                        taskCompletionSource.setException(e); // Propaga l'eccezione in caso di errore
                    });
                } else {
                    taskCompletionSource.setException(new Exception("No data found in the document"));
                }
            } else {
                taskCompletionSource.setException(new Exception("No such document"));
            }
        }).addOnFailureListener(e -> {
            taskCompletionSource.setException(e); // Propaga l'eccezione in caso di errore
        });

        return taskCompletionSource.getTask();
    }
}
