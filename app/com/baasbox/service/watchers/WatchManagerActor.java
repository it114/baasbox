package com.baasbox.service.watchers;

import akka.actor.UntypedActor;
import akka.dispatch.sysmsg.Watch;
import com.baasbox.db.DbHelper;
import com.baasbox.service.events.EventSource;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by eto on 11/14/14.
 */
public class WatchManagerActor extends UntypedActor {

    private static class WatchState{
        long lastUpdate;
        long count;
    }

    private Map<WatchKey,EventSource> eventSources;
    private Map<WatchKey,WatchState> statusMap;
    private Map<String,Set<WatchKey>> collToKey;

    public WatchManagerActor(){
        eventSources = new HashMap<>();
        statusMap = new HashMap<>();
        collToKey = new HashMap<>();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Registration){
            handleRegistration((Registration)message);
        } else if (message instanceof Unregister){
            handleUnregister((Unregister)message);
        } else if (message instanceof Update){
            handleUpdate((Update)message);
        } else {
            unhandled(message);
        }
    }

    private void handleUpdate(Update message) {
        long now=System.currentTimeMillis();
        Set<WatchKey> keys = collToKey.get(message.collection);
        if (keys==null||keys.isEmpty()) return;
        for (WatchKey k:keys){
            WatchState s = statusMap.get(k);

        }
    }

    private void handleRegistration(Registration reg){
        WatchKey key=reg.key;
        EventSource src= reg.source;

        long now = System.currentTimeMillis();
        WatchState state = new WatchState();
        state.count = 0;
        state.lastUpdate=now;

        eventSources.put(key,src);
        statusMap.put(key, state);
        Set<WatchKey> keys = collToKey.get(key.collection);
        if (keys==null){
            keys = new HashSet<>();
            collToKey.put(key.collection,keys);
        }
        keys.add(key);

        if (key.wantsCurrent()){
            publish(key,src,state);
        }
    }

    private void handleUnregister(Unregister unregister){
        WatchKey k = unregister.key;
        statusMap.remove(k);
        EventSource src = eventSources.remove(k);
        Set<WatchKey> watchKeys = collToKey.get(k.collection);
        if (watchKeys!=null) {
            watchKeys.remove(k);
            if (watchKeys.isEmpty()) {
                collToKey.remove(k.collection);
            }
        }
        if (src!=null){
            src.close();
        }
    }


    private void publish(WatchKey key,EventSource src,WatchState state){

    }
}