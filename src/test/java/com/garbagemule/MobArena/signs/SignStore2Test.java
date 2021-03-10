package com.garbagemule.MobArena.signs;

import org.bukkit.Location;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SignStore2Test {

    SignStore2 subject;

    @Before
    public void setup() {
        subject = new SignStore2();
    }

    @Test
    public void removeReturnsNullIfSignDoesNotExist() {
        Location location = mock(Location.class);
        ArenaSign sign = new ArenaSign(location, "cool-sign", "castle", "join");

        ArenaSign result = subject.removeByLocation(location);

        assertThat(result, nullValue());

        subject.add(sign);
        assertThat(subject.removeByLocation(location), equalTo(sign));

        assertThat(subject.removeByLocation(location), nullValue());
    }

    @Test
    public void removeReturnsSignIfItExists() {
        Location location = mock(Location.class);
        ArenaSign sign = new ArenaSign(location, "cool-sign", "castle", "join");

        subject.add(sign);
        ArenaSign result = subject.removeByLocation(location);

        assertThat(result, equalTo(sign));
    }

    @Test
    public void removeReturnsNullIfSignWasRemoved() {
        Location location = mock(Location.class);
        ArenaSign sign = new ArenaSign(location, "cool-sign", "castle", "join");

        subject.add(sign);
        subject.removeByLocation(location);
        ArenaSign result = subject.removeByLocation(location);

        assertThat(result, nullValue());
    }

}
