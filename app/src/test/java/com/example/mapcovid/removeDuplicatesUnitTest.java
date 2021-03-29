package com.example.mapcovid;

import android.content.Context;
import android.widget.LinearLayout;

import com.example.mapcovid.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({FirebaseDatabase.class})
public class removeDuplicatesUnitTest {
    HomeActivity ha;
    ArrayList<PathItem> p;

    @Mock
    Context mockContext;

    private DatabaseReference mockDB;

    public ArrayList<PathItem> createArray(boolean hasDuplicates){
        ArrayList<PathItem> pathList = new ArrayList<>();

        for(int i = 0; i < 100; i++)
        {
            pathList.add(new PathItem(i+"", ("c"+i), ( double)i*4, (double) i*5));
        }

        if(hasDuplicates)
        {
            pathList.add(new PathItem(99+"", "c" + 99, (double)99*4, (double)99*5));
            pathList.add(new PathItem(98+"", "c" + 98, (double)98*4, (double)98*5));
        }


        return pathList;
    }

    @Test
    public void removeDuplicates(){
        mockDB = Mockito.mock(DatabaseReference.class);
        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);
        Mockito.when(mockFDB.getReference()).thenReturn(mockDB);
        PowerMockito.mockStatic(FirebaseDatabase.class);
        Mockito.when(FirebaseDatabase.getInstance()).thenReturn(mockFDB);

        ha = new HomeActivity();

        p = createArray(true);
        p = ha.removeConsecutiveDuplicates(p);
        assertNotNull(p);
        assertEquals(101, p.size());
        assertEquals("98",p.get(p.size()-1).getTime());
        assertEquals("99",p.get(p.size()-2).getTime());
    }

    @Test
    public void noRemoval(){
        mockDB = Mockito.mock(DatabaseReference.class);
        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);
        Mockito.when(mockFDB.getReference()).thenReturn(mockDB);
        PowerMockito.mockStatic(FirebaseDatabase.class);
        Mockito.when(FirebaseDatabase.getInstance()).thenReturn(mockFDB);
        ha = new HomeActivity();

        p = createArray(false);
        p = ha.removeConsecutiveDuplicates(p);
        assertNotNull(p);
        assertEquals(100, p.size());
        assertEquals("99",p.get(p.size()-1).getTime());
        assertEquals("98",p.get(p.size()-2).getTime());
    }

    @Test
    public void noItems(){
        mockDB = Mockito.mock(DatabaseReference.class);
        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);
        Mockito.when(mockFDB.getReference()).thenReturn(mockDB);
        PowerMockito.mockStatic(FirebaseDatabase.class);
        Mockito.when(FirebaseDatabase.getInstance()).thenReturn(mockFDB);
        ha = new HomeActivity();

        p = ha.removeConsecutiveDuplicates(p);
        assertNull(p);

        p = new ArrayList<>();
        p = ha.removeConsecutiveDuplicates(p);
        assertNotNull(p);
        assertEquals(0, p.size());
    }

}
