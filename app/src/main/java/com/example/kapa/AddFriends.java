package com.example.kapa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AddFriends extends AppCompatActivity {
    // TODO add friends

    private String user_id;
    private String user_name;

    TextView af_main,af_enterUid,af_myUid;
    Button af_search,af_seeFriendReq;
    EditText af_et_uid;
    RecyclerView af_rv_reqList;
    List<FriendReqModel> friendReqModelList;
    RecyclerView.Adapter adapter;

    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        user_name = intent.getStringExtra("user_name");

        af_main = findViewById(R.id.af_mainTitle);
        af_enterUid = findViewById(R.id.af_enter_uid);
        af_myUid = findViewById(R.id.af_my_uid);

        af_search = findViewById(R.id.af_srch);
        af_seeFriendReq = findViewById(R.id.af_see_friend_req);

        af_et_uid = findViewById(R.id.af_et_uid);

        af_rv_reqList = findViewById(R.id.af_rv_req_list);
        af_rv_reqList.setLayoutManager(new LinearLayoutManager(this));
        af_rv_reqList.setHasFixedSize(true);
        friendReqModelList = new ArrayList<>();

        database = FirebaseDatabase.getInstance("https://kapa-ce822-default-rtdb.asia-southeast1.firebasedatabase.app");

        af_seeFriendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(af_main.getText().equals("Add Friends"))
                {
                    af_main.setText("Friend Requests");
                    af_enterUid.setVisibility(View.INVISIBLE);
                    af_myUid.setVisibility(View.INVISIBLE);
                    af_search.setVisibility(View.INVISIBLE);
                    af_et_uid.setVisibility(View.INVISIBLE);

                    af_rv_reqList.setVisibility(View.VISIBLE);
                    af_seeFriendReq.setText("Add Friends");
                }
                else if(af_main.getText().equals("Friend Requests"))
                {
                    af_main.setText("Add Friends");
                    af_rv_reqList.setVisibility(View.INVISIBLE);
                    af_seeFriendReq.setText("See Friend requests");

                    af_enterUid.setVisibility(View.VISIBLE);
                    af_myUid.setVisibility(View.VISIBLE);
                    af_search.setVisibility(View.VISIBLE);
                    af_et_uid.setVisibility(View.VISIBLE);
                }
            }
        });
        af_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(af_et_uid.getText().charAt(0) != '-' || af_et_uid.getText().equals("") || af_et_uid.getText().length() < 18)
                    Toast.makeText(AddFriends.this, "Uid Not Valid", Toast.LENGTH_SHORT).show();
                else
                {
                    // TODO search friend in database user and sent friend request if found else say friend/Uid not found
                    String tempUid = af_et_uid.getText().toString();
                    DatabaseReference myRef = database.getReference("user");
                    Query query = myRef.orderByChild("userStrId").equalTo(tempUid);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists())
                            {
                                UserModel model = new UserModel();
                                for(DataSnapshot i : snapshot.getChildren())
                                    model = i.getValue(UserModel.class);
                                reqFriend(model.getUserStrId());
                            }
                            else
                            {
                                Toast.makeText(AddFriends.this, "Uid not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });

        // get the values for list for recycler view
        DatabaseReference myRef = database.getReference("friend_req").child(user_id);
        Query query = myRef.orderByChild("fromUid");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Toast.makeText(AddFriends.this, "You have new Friend request", Toast.LENGTH_SHORT).show();
                    for(DataSnapshot i : snapshot.getChildren())
                    {
                        FriendReqModel temp = i.getValue(FriendReqModel.class);
                        if(temp != null)
                            friendReqModelList.add(temp);
                    }

                    adapter = new AdapterAddFriendRequestRv(friendReqModelList,database,user_name);
                    af_rv_reqList.setAdapter(adapter);
                }
                else
                {
                    Toast.makeText(AddFriends.this, "No new friend requests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Done: add friends to database(1/2)
    }

    private void reqFriend(String frId)
    {
        DatabaseReference myRef = database.getReference("friend_req")
                .child(frId).child(user_id);
        myRef.setValue(new FriendReqModel(frId,user_id,user_name));
    }

}