package com.antigravity.race;

import com.antigravity.context.DatabaseContext;
import com.antigravity.proto.InterfaceEvent;
import com.antigravity.proto.RaceData;
import com.antigravity.proto.RaceSubscriptionRequest;
import com.antigravity.protocols.ProtocolDelegate;
import com.antigravity.service.DatabaseService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.protobuf.GeneratedMessageV3;
import io.javalin.websocket.WsContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSubscriptionManager {

  private static final Logger logger = LoggerFactory.getLogger(ClientSubscriptionManager.class);
  private static ClientSubscriptionManager instance;
  private Race currentRace;
  private ProtocolDelegate currentProtocol;
  private DatabaseContext databaseContext;
  private volatile boolean isShuttingDown = false;
  private final Set<WsContext> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final Set<WsContext> raceDataSubscribers =
      Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final Set<WsContext> interfaceSubscribers =
      Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private ScheduledFuture<?> cleanupFuture;
  private long cleanupGracePeriodSeconds = 10;

  private ClientSubscriptionManager() {}

  public static synchronized ClientSubscriptionManager getInstance() {
    if (instance == null) {
      instance = new ClientSubscriptionManager();
    }
    return instance;
  }

  public synchronized void setDatabaseContext(DatabaseContext databaseContext) {
    this.databaseContext = databaseContext;
  }

  public synchronized DatabaseContext getDatabaseContext() {
    return databaseContext;
  }

  public void setShuttingDown(boolean shuttingDown) {
    this.isShuttingDown = shuttingDown;
    if (shuttingDown) {
      logger.info("Server shutting down. Cleaning up race and protocols...");
      setRace(null);
      setProtocol(null);
    }
  }

  public synchronized void setCleanupGracePeriodSeconds(long seconds) {
    this.cleanupGracePeriodSeconds = seconds;
  }

  public synchronized void setRace(Race race) {
    if (race != null && this.currentProtocol != null) {
      throw new IllegalStateException("Cannot set race while protocol is active");
    }
    if (this.currentRace != null) {
      if (race == null) {
        this.currentRace.setMainPower(true);
        this.currentRace.setLanePower(true, -1);
      }
      this.currentRace.stop();
    }
    this.currentRace = race;

    if (this.currentRace != null) {
      logger.info("New race set. Clients must explicitly subscribe to race data.");
    }
  }

  public synchronized Race getRace() {
    return currentRace;
  }

  public synchronized void setProtocol(ProtocolDelegate protocol) {
    logger.debug(
        "setProtocol called with: {}",
        (protocol == null ? "null" : protocol.getClass().getSimpleName()));
    if (protocol != null && this.currentRace != null) {
      logger.info("New protocol being set while race is active. Stopping race to allow take-over.");
      setRace(null);
    }
    if (this.currentProtocol != null) {
      try {
        this.currentProtocol.close();
      } catch (Exception e) {
        System.err.println("Error closing protocol: " + e.getMessage());
        e.printStackTrace();
      }
    }
    this.currentProtocol = protocol;
  }

  public synchronized ProtocolDelegate getProtocol() {
    return currentProtocol;
  }

  public void addSession(WsContext ctx) {
    sessions.add(ctx);
    // Remove auto-subscription: clients must call subscribe() explicitly
    // raceDataSubscribers.add(ctx);
    System.out.println("New WebSocket session added. Total sessions: " + sessions.size());

    if (currentRace != null) {
      RaceData snapshot = currentRace.createSnapshot();
      if (snapshot.hasRace() && snapshot.getRace().hasCurrentHeat()) {
        logger.debug(
            "Snapshot includes Current Heat: {}",
            snapshot.getRace().getCurrentHeat().getObjectId());
      } else {
        logger.warn("Snapshot MISSING Current Heat!");
      }
      ctx.send(ByteBuffer.wrap(snapshot.toByteArray()));
    }
  }

  public void removeSession(WsContext ctx) {
    sessions.remove(ctx);
    raceDataSubscribers.remove(ctx);
    System.out.println(
        "WebSocket session removed. Total sessions: "
            + sessions.size()
            + ", Subscribers: "
            + raceDataSubscribers.size());

    checkAndStopRace();
  }

  public synchronized void addInterfaceSession(WsContext ctx) {
    System.out.println(
        "DEBUG: New Interface WebSocket session added: " + System.identityHashCode(ctx));
    sessions.add(ctx);
    interfaceSubscribers.add(ctx);
    System.out.println(
        "New Interface WebSocket session added. Total sessions: "
            + sessions.size()
            + ", Interface Subscribers: "
            + interfaceSubscribers.size());
  }

  public synchronized void removeInterfaceSession(WsContext ctx) {
    sessions.remove(ctx);
    interfaceSubscribers.remove(ctx);
    logger.info(
        "Interface WebSocket session removed. Total sessions: {}, Interface Subscribers: {}",
        sessions.size(),
        interfaceSubscribers.size());
    checkAndCloseProtocol();
  }

  private synchronized void checkAndCloseProtocol() {
    if (interfaceSubscribers.isEmpty() && currentProtocol != null && currentRace == null) {
      logger.info("Last interested interface client disconnected. Closing current protocol.");
      try {
        currentProtocol.close();
      } catch (Exception e) {
        logger.error("Error closing protocol", e);
      }
      currentProtocol = null;
    }
  }

  public void handleRaceSubscription(WsContext ctx, RaceSubscriptionRequest request) {
    if (request.getSubscribe()) {
      cancelPendingCleanup();
      raceDataSubscribers.add(ctx);
      logger.info("Client subscribed to race data. Subscribers: {}", raceDataSubscribers.size());
      // Send current state immediately upon subscription if race exists
      if (currentRace != null) {
        RaceData snapshot = currentRace.createSnapshot();
        if (snapshot.hasRace() && snapshot.getRace().hasCurrentHeat()) {
          logger.debug(
              "Snapshot includes Current Heat: {}",
              snapshot.getRace().getCurrentHeat().getObjectId());
        } else {
          logger.warn("Snapshot MISSING Current Heat!");
        }
        ctx.send(ByteBuffer.wrap(snapshot.toByteArray()));
      }
    } else {
      raceDataSubscribers.remove(ctx);
      logger.info(
          "Client unsubscribed from race data. Subscribers: {}", raceDataSubscribers.size());
      checkAndStopRace();
    }
  }

  private synchronized void checkAndStopRace() {
    if (currentRace != null && raceDataSubscribers.isEmpty()) {
      if (!isShuttingDown) {
        // If there are NO sessions at all (not even splash screen), we should stop quickly
        long gracePeriod =
            sessions.isEmpty() ? Math.min(1, cleanupGracePeriodSeconds) : cleanupGracePeriodSeconds;

        if (gracePeriod <= 0) {
          performCleanup();
        } else if (cleanupFuture == null || cleanupFuture.isDone()) {
          logger.info(
              "No subscribers left (Sessions: {}). Scheduling race cleanup in {} seconds...",
              sessions.size(),
              gracePeriod);
          cleanupFuture =
              scheduler.schedule(
                  () -> {
                    performCleanup();
                  },
                  gracePeriod,
                  TimeUnit.SECONDS);
        }
      } else {
        logger.info("Server is shutting down, preserving race state and auto-save.");
      }
    }
  }

  private synchronized void performCleanup() {
    if (raceDataSubscribers.isEmpty() && currentRace != null) {
      logger.info(
          "Last interested client disconnected/unsubscribed. Stopping and clearing current race.");
      deleteAutoSave(currentRace.getRaceModel().getEntityId());
      setRace(null);
    }
  }

  private synchronized void cancelPendingCleanup() {
    if (cleanupFuture != null && !cleanupFuture.isDone()) {
      logger.info("Subscriber re-connected. Cancelling pending race cleanup.");
      cleanupFuture.cancel(false);
      cleanupFuture = null;
    }
  }

  public synchronized void autoSave(Race race) {
    if (race == null || databaseContext == null || race.isDemoMode()) {
      return;
    }
    try {
      RaceSaveData saveData = new RaceSaveData();
      saveData.setModel(race.getRaceModel());
      saveData.setTrack(race.getTrack());
      saveData.setDrivers(race.getDrivers());
      saveData.setHeats(race.getHeats());
      saveData.setStateClassName(race.getState().getClass().getName());
      saveData.setAccumulatedRaceTime(race.getRaceTime());
      saveData.setHasRacedInCurrentHeat(race.hasRacedInCurrentHeat());
      saveData.setCurrentHeatIndex(race.getHeats().indexOf(race.getCurrentHeat()));
      saveData.setDemoMode(race.isDemoMode());
      saveData.setStatistics(race.getStatistics());

      saveData.setAutoSave(true);
      String filename = "autosave_" + race.getRaceModel().getEntityId() + ".json";
      saveData.setSaveName(filename);

      DatabaseService dbService = DatabaseService.getInstance();
      dbService.upsertAutoSave(databaseContext.getDatabase(), saveData);
      logger.info("Auto-saved race to database: {}", filename);
    } catch (Exception e) {
      logger.error("Error during auto-save", e);
    }
  }

  public synchronized void deleteAutoSave(String raceId) {
    if (databaseContext == null || raceId == null) {
      return;
    }
    try {
      String filename = "autosave_" + raceId + ".json";
      DatabaseService dbService = DatabaseService.getInstance();
      boolean deleted = dbService.deleteSavedRace(databaseContext.getDatabase(), filename);
      if (deleted) {
        logger.info("Deleted auto-save from db: {}", filename);
      }
    } catch (Exception e) {
      logger.error("Error deleting auto-save", e);
    }
  }

  private ObjectMapper getObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    SimpleModule module = new SimpleModule();
    module.addSerializer(
        ObjectId.class,
        new JsonSerializer<ObjectId>() {
          @Override
          public void serialize(ObjectId value, JsonGenerator gen, SerializerProvider serializers)
              throws IOException {
            gen.writeString(value.toHexString());
          }
        });
    module.addDeserializer(
        ObjectId.class,
        new JsonDeserializer<ObjectId>() {
          @Override
          public ObjectId deserialize(JsonParser p, DeserializationContext ctxt)
              throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.isEmpty()) {
              return null;
            }
            try {
              return new ObjectId(value);
            } catch (IllegalArgumentException e) {
              return null;
            }
          }
        });
    mapper.registerModule(module);
    return mapper;
  }

  public boolean hasSubscribers() {
    return !raceDataSubscribers.isEmpty();
  }

  public void broadcast(GeneratedMessageV3 message) {
    if (raceDataSubscribers.isEmpty()) {
      return;
    }

    byte[] bytes = message.toByteArray();

    raceDataSubscribers.forEach(
        ctx -> {
          ctx.send(ByteBuffer.wrap(bytes));
        });
  }

  public void broadcastInterfaceEvent(InterfaceEvent event) {
    if (interfaceSubscribers.isEmpty()) {
      return;
    }

    byte[] bytes = event.toByteArray();
    for (WsContext session : interfaceSubscribers) {
      try {
        session.send(ByteBuffer.wrap(bytes));
      } catch (Exception e) {
        // Ignore or log
      }
    }
  }
}
