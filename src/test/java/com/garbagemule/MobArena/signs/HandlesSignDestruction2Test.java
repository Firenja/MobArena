package com.garbagemule.MobArena.signs;

import com.garbagemule.MobArena.Messenger;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class HandlesSignDestruction2Test {

    SignStore2 store;
    SignWriter writer;
    Messenger messenger;
    Logger log;

    HandlesSignDestruction2 subject;

    @Before
    public void setup() {
        store = mock(SignStore2.class);
        writer = mock(SignWriter.class);
        messenger = mock(Messenger.class);
        log = mock(Logger.class);

        subject = new HandlesSignDestruction2(
            store,
            writer,
            messenger,
            log
        );
    }

    @Test
    public void noSignNoAction() {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        BlockBreakEvent event = new BlockBreakEvent(block, null);
        when(block.getLocation()).thenReturn(location);
        when(store.removeByLocation(location)).thenReturn(null);

        subject.on(event);

        verifyNoInteractions(writer, messenger, log);
    }

    @Test
    public void erasesFoundSign() throws IOException {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        Player player = mock(Player.class);
        ArenaSign sign = new ArenaSign(location, "cool-sign", "castle", "join");
        when(block.getLocation()).thenReturn(location);
        when(store.removeByLocation(location)).thenReturn(sign);
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        subject.on(event);

        verify(writer).erase(sign);
    }

    @Test
    public void successMessageOnDestruction() {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        Player player = mock(Player.class);
        ArenaSign sign = new ArenaSign(location, "cool-sign", "castle", "join");
        when(block.getLocation()).thenReturn(location);
        when(store.removeByLocation(location)).thenReturn(sign);
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        subject.on(event);

        verify(messenger).tell(eq(player), anyString());
        verify(log).info(anyString());
    }

    @Test
    public void errorMessageIfWriterThrows() throws Exception {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        Player player = mock(Player.class);
        ArenaSign sign = new ArenaSign(location, "cool-sign", "castle", "join");
        IOException exception = new IOException("it's bad");
        when(block.getLocation()).thenReturn(location);
        when(store.removeByLocation(location)).thenReturn(sign);
        doThrow(exception).when(writer).erase(sign);
        BlockBreakEvent event = new BlockBreakEvent(block, player);

        subject.on(event);

        verify(messenger).tell(eq(player), anyString());
        verify(log).log(eq(Level.SEVERE), anyString(), eq(exception));
    }

}
