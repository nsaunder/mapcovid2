package com.example.mapcovid;

import android.content.Context;
import android.test.mock.MockContext;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.InstrumentationRegistry;

import com.example.mapcovid.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({FirebaseDatabase.class})
public class popPathPageUnitTest {
    HomeActivity ha;

    @Mock
    Context c = mock(Context.class);

    @Mock
    TextView t = new TextView(c);


    private DatabaseReference mockDB;

    public ArrayList<PathItem> initPath(){
        ArrayList<PathItem> pathList = new ArrayList<>();

        for(int i = 0; i < 100; i++)
        {
            pathList.add(new PathItem(i+"", ("c"+i), ( double)i*4, (double) i*5));
        }

        return pathList;
    }

    @Before
    public void initt(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPop(){
        getPathCallback callBack = mock(getPathCallback.class);

        ArrayList<PathItem> testPath = initPath();

        mockDB = mock(DatabaseReference.class);
        FirebaseDatabase mockFDB = mock(FirebaseDatabase.class);
        Mockito.when(mockFDB.getReference()).thenReturn(mockDB);
        PowerMockito.mockStatic(FirebaseDatabase.class);
        Mockito.when(FirebaseDatabase.getInstance()).thenReturn(mockFDB);
        ha = new HomeActivity();
        LinearLayout ll = new LinearLayout(c);
        int count = ha.getInfo(LocalDate.now().toString(), c, ll, true, testPath);

        assertNotNull(count);
        assertEquals(count, testPath.size());

    }

}