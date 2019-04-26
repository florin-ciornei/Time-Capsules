package com.LightMediaApps.TimeCapsules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.LightMediaApps.TimeCapsules.model.Capsule;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Date;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class SearchActivity extends AppCompatActivity implements CapsuleListAdapter.OnListItemClickListener {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Capsule> closedCapsules, openedCapsules;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Intent intent = null;
            switch (item.getItemId()) {
                case R.id.navigation_me:
                    intent = new Intent(getBaseContext(), ProfileActivity.class);
                case R.id.navigation_search:
                    intent = new Intent(getBaseContext(), SearchActivity.class);
                case R.id.navigation_create:
                    intent = new Intent(getBaseContext(), CreateActivity.class);
            }
            startActivity(intent);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent intent = new Intent(getBaseContext(), WelcomeActivity.class);
                    startActivity(intent);
                }
            }
        });

        setContentView(R.layout.activity_search);
        closedCapsules = new ArrayList<>();
        openedCapsules = new ArrayList<>();


        recyclerView = (RecyclerView) findViewById(R.id.browseCpasulesRecyclerView);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new CapsuleListAdapter(closedCapsules, this);
        recyclerView.setAdapter(mAdapter);

        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();


        mDatabase.child("capsules").orderByChild("inverseCreatedTime").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Capsule capsule = dataSnapshot.getValue(Capsule.class);
                if (capsule.isOpened())
                    openedCapsules.add(capsule);
                else
                    closedCapsules.add(capsule);
                mAdapter.notifyItemInserted(closedCapsules.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
            // TODO: implement the ChildEventListener methods as documented above
            // ...
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.createFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), CreateActivity.class);
                startActivity(intent);
            }
        });

        ((TabLayout) findViewById(R.id.tabLayout)).addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    ((CapsuleListAdapter) mAdapter).updateCapsules(closedCapsules);
                } else {
                    ((CapsuleListAdapter) mAdapter).updateCapsules(openedCapsules);
                }
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.menu_items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.menuProfile:
                intent = new Intent(getBaseContext(), ProfileActivity.class);
                startActivity(intent);
                return true;

            case R.id.menuAbout:
                intent = new Intent(getBaseContext(), AboutActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(Capsule capsule) {
        Intent intent = new Intent(getBaseContext(), CapsuleActivity.class);
        intent.putExtra("capsule", capsule);
        startActivity(intent);
    }
}
