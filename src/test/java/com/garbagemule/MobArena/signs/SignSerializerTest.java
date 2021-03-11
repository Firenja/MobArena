package com.garbagemule.MobArena.signs;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SignSerializerTest {

    SignSerializer subject;

    @Before
    public void setup() {
        subject = new SignSerializer();
    }

    @Test
    public void serializeReturnsSemicolonSeparatedRepresentation() {
        World world = mock(World.class);
        Location location = new Location(world, 1, 2, 3);
        String arenaId = "castle";
        String type = "join";
        String templateId = "status";
        when(world.getUID()).thenReturn(UUID.fromString("cafebabe-ea75-dead-beef-deadcafebabe"));
        when(world.getName()).thenReturn("world");
        ArenaSign sign = new ArenaSign(location, templateId, arenaId, type);

        String result = subject.serialize(sign);

        String expected = "cafebabe-ea75-dead-beef-deadcafebabe;world;1;2;3;castle;join;status";
        assertThat(result, equalTo(expected));
    }

    @Test
    public void equalReturnsTrueIfStringsAreEqual() {
        String line = "cafebabe-ea75-dead-beef-deadcafebabe;world;1;2;3;castle;join;status";

        boolean result = subject.equal(line, line);

        assertThat(result, equalTo(true));
    }

    @Test
    public void equalReturnsTrueIfWorldIdAndLocationMatch() {
        String line1 = "cafebabe-ea75-dead-beef-deadcafebabe;right-world;1;2;3;jungle;leave;out";
        String line2 = "cafebabe-ea75-dead-beef-deadcafebabe;wrong-world;1;2;3;castle;join;status";

        boolean result = subject.equal(line1, line2);

        assertThat(result, equalTo(true));
    }

    @Test
    public void equalReturnsFalseIfOnlyWorldIdIsDifferent() {
        String line1 = "cafebabe-ea75-dead-beef-deadcafebabe;right-world;1;2;3;castle;join;status";
        String line2 = "deadbeef-feed-cafe-babe-a70ff1cecafe;right-world;1;2;3;castle;join;status";

        boolean result = subject.equal(line1, line2);

        assertThat(result, equalTo(false));
    }

    @Test
    public void equalReturnsFalseIfOnlyCoordsAreDifferent() {
        String line1 = "cafebabe-ea75-dead-beef-deadcafebabe;right-world;1;2;3;castle;join;status";
        String line2 = "cafebabe-ea75-dead-beef-deadcafebabe;right-world;4;5;6;castle;join;status";

        boolean result = subject.equal(line1, line2);

        assertThat(result, equalTo(false));
    }

}
