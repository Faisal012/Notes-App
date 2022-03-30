package com.example.notesapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.example.notesapp.Adapters.NotesListAdapter;
import com.example.notesapp.Database.RoomDB;
import com.example.notesapp.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> notes=new ArrayList<>();
    RoomDB database;
    FloatingActionButton fab_add;
    SearchView searchViw_home;
    Notes selectedNotes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchViw_home=findViewById(R.id.searchViw_home);

        recyclerView=findViewById(R.id.recycler_home);
        fab_add=findViewById(R.id.fab_add);

        database=RoomDB.getInstance(this);
        notes=database.mainDAO().getAll();

        updateRecycler(notes);

        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,NotesTakerActivity.class);
                startActivityForResult(intent,101);
            }
        });
        searchViw_home.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String newText) {
        List<Notes> filteredList=new ArrayList<>();
        for(Notes singleNotes:notes){
            if(singleNotes.getTitle().toLowerCase().contains(newText.toLowerCase())
             || singleNotes.getNotes().toLowerCase().contains(newText.toLowerCase())){
                filteredList.add(singleNotes);
            }
        }
        notesListAdapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==101){
            if(resultCode== Activity.RESULT_OK){
                Notes new_notes=(Notes) data.getSerializableExtra("note");
                database.mainDAO().insert(new_notes);
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();

            }
        }
        if(requestCode==102){
            if(resultCode==Activity.RESULT_OK){
                Notes new_notes=(Notes)data.getSerializableExtra("note");
                database.mainDAO().update(new_notes.getID(),new_notes.getTitle(),new_notes.getNotes());
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateRecycler(List<Notes> notes) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayout.VERTICAL));
        notesListAdapter=new NotesListAdapter(MainActivity.this,notes, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }
    private final  NotesClickListener notesClickListener=new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent=new Intent(MainActivity.this,NotesTakerActivity.class);
            intent.putExtra("old_notes",notes);
            startActivityForResult(intent,102);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectedNotes=new Notes();
            selectedNotes=notes;
            showPopup(cardView);
        }
    };

    private void showPopup(CardView cardView) {
        PopupMenu popupMenu=new PopupMenu(this,cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.pin:
                if(selectedNotes.isPinned()){
                    database.mainDAO().pin(selectedNotes.getID(),false);
                    Toast.makeText(MainActivity.this,"Unpinned",Toast.LENGTH_SHORT).show();
                }
                else{
                    database.mainDAO().pin(selectedNotes.getID(),true);
                    Toast.makeText(MainActivity.this,"pinned",Toast.LENGTH_SHORT).show();
                }
                notes.clear();
                notes.addAll(database.mainDAO().getAll());
                notesListAdapter.notifyDataSetChanged();
                return true;
            case R.id.delete:
                database.mainDAO().delete(selectedNotes);
                notes.remove(selectedNotes);
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this,"Deleted",Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
}