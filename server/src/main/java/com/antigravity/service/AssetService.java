package com.antigravity.service;

import com.antigravity.proto.AssetMessage;
import com.antigravity.proto.ImageSetEntry;
import com.antigravity.proto.Model;
import com.antigravity.proto.SaveImageSetEntry;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bson.Document;
import org.bson.conversions.Bson;

public class AssetService {

  private final String assetDir;
  private final MongoDatabase database;
  private final MongoCollection<Document> collection;

  private static class DefaultAsset {
    final String id;
    final String filename;
    final String displayName;

    DefaultAsset(String id, String filename, String displayName) {
      this.id = id;
      this.filename = filename;
      this.displayName = displayName;
    }
  }

  private static class FuelDefaultAsset extends DefaultAsset {
    final int percentage;

    FuelDefaultAsset(String id, String filename, String displayName, int percentage) {
      super(id, filename, displayName);
      this.percentage = percentage;
    }
  }

  private static final List<DefaultAsset> DEFAULT_IMAGE_ASSETS = new ArrayList<>();

  static {
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset(
            "default_avatar_helmet_4", "default_avatar_helmet_4.png", "Helmet Futuristic 1"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset(
            "default_avatar_helmet_5", "default_avatar_helmet_5.png", "Helmet Futuristic 2"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_green-white", "green-white.png", "Helmet Green-White"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset(
            "default_grey-black-gold", "grey-black-gold.png", "Helmet Grey-Black-Gold"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_grey-red-white", "grey-red-white.png", "Helmet Grey-Red-White"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_orange-blue", "orange-blue.png", "Helmet Orange-Blue"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_red-gold-blue", "red-gold-blue.png", "Helmet Red-Gold-Blue"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_red-orange", "red-orange.png", "Helmet Red-Orange"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_red-yellow", "red-yellow.png", "Helmet Red-Yellow"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_silver-green", "silver-green.png", "Helmet Silver-Green"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_silver-red", "silver-red.png", "Helmet Silver-Red"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset(
            "default_white-blue-yellow", "white-blue-yellow.png", "Helmet White-Blue-Yellow"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_white-blue", "white-blue.png", "Helmet White-Blue"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset(
            "default_white-red-yellow", "white-red-yellow.png", "Helmet White-Red-Yellow"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_black-grey", "black-grey.png", "Helmet Black-Grey"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_black-white", "black-white.png", "Helmet Black-White"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_black-white2", "black-white2.png", "Helmet Black-White2"));
    DEFAULT_IMAGE_ASSETS.add(new DefaultAsset("default_black", "black.png", "Helmet Black"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_flag_green", "flag_green.png", "Green Flag"));
    DEFAULT_IMAGE_ASSETS.add(new DefaultAsset("default_flag_red", "flag_red.png", "Red Flag"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_flag_yellow", "flag_yellow.png", "Yellow Flag"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset(
            "default_flag_green_yellow", "flag_green_yellow.png", "Yellow Green Flag"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_flag_black", "flag_black.png", "Black Flag"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_flag_white", "flag_white.png", "White Flag"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_flag_checkered", "flag_checkered.png", "Checkered Flag"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_start_red_on", "start_red_on.png", "Start Lamp Red"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_start_red_dim", "start_red_dim.png", "Start Lamp Dim"));
    DEFAULT_IMAGE_ASSETS.add(
        new DefaultAsset("default_start_green", "start_green.png", "Start Lamp Green"));
  }

  private static final List<FuelDefaultAsset> DEFAULT_FUEL_IMAGE_ASSETS = new ArrayList<>();

  static {
    // TODO(aufderheide): For now the order here controls how it animates
    // in the asset editor. The order shouldn't matter.
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_100", "fuel_100.png", "Fuel Gauge 100%", 100));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_90", "fuel_90.png", "Fuel Gauge 90%", 90));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_80", "fuel_80.png", "Fuel Gauge 80%", 80));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_70", "fuel_70.png", "Fuel Gauge 70%", 70));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_60", "fuel_60.png", "Fuel Gauge 60%", 60));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_50", "fuel_50.png", "Fuel Gauge 50%", 50));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_40", "fuel_40.png", "Fuel Gauge 40%", 40));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_30", "fuel_30.png", "Fuel Gauge 30%", 30));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_20", "fuel_20.png", "Fuel Gauge 20%", 20));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_10", "fuel_10.png", "Fuel Gauge 10%", 10));
    DEFAULT_FUEL_IMAGE_ASSETS.add(
        new FuelDefaultAsset("default_fuel_0", "fuel_0.png", "Fuel Gauge 0%", 0));
  }

  private static final List<DefaultAsset> DEFAULT_AUDIO_ASSETS = new ArrayList<>();

  static {
    DEFAULT_AUDIO_ASSETS.add(new DefaultAsset("default_beep", "beep.wav", "Lap Beep"));
    DEFAULT_AUDIO_ASSETS.add(new DefaultAsset("default_chimes", "chimes.wav", "Lap Chimes"));
    DEFAULT_AUDIO_ASSETS.add(new DefaultAsset("default_driveby", "driveby.wav", "Lap Driveby"));
    DEFAULT_AUDIO_ASSETS.add(
        new DefaultAsset(
            "default_yellow_flag", "audio/english/woman/w_yellowflag.wav", "Yellow Flag"));
    DEFAULT_AUDIO_ASSETS.add(
        new DefaultAsset(
            "default_countdown_go", "audio/english/woman/w_countdown_0.wav", "Countdown Go"));
    DEFAULT_AUDIO_ASSETS.add(
        new DefaultAsset(
            "default_countdown_1", "audio/english/woman/w_countdown_1.wav", "Countdown 1"));
    DEFAULT_AUDIO_ASSETS.add(
        new DefaultAsset(
            "default_countdown_2", "audio/english/woman/w_countdown_2.wav", "Countdown 2"));
    DEFAULT_AUDIO_ASSETS.add(
        new DefaultAsset(
            "default_countdown_3", "audio/english/woman/w_countdown_3.wav", "Countdown 3"));
    DEFAULT_AUDIO_ASSETS.add(
        new DefaultAsset(
            "default_countdown_4", "audio/english/woman/w_countdown_4.wav", "Countdown 4"));
    DEFAULT_AUDIO_ASSETS.add(
        new DefaultAsset(
            "default_countdown_5", "audio/english/woman/w_countdown_5.wav", "Countdown 5"));
  }

  public AssetService(MongoDatabase database, String assetDir) {
    this.database = database;
    this.collection = database.getCollection("assets");
    this.assetDir = assetDir;
    File directory = new File(assetDir);
    System.out.println(
        "Path DEBUG: AssetService constructor. assetDir="
            + assetDir
            + " absolute="
            + directory.getAbsolutePath());
    if (!directory.exists()) {
      boolean created = directory.mkdirs();
      if (!created) {
        System.err.println(
            "CRITICAL: Failed to create asset directory: " + directory.getAbsolutePath());
      } else {
        System.out.println("Created asset directory: " + directory.getAbsolutePath());
      }
    }
  }

  public String getAssetDir() {
    return assetDir;
  }

  public List<AssetMessage> getAllAssets() {
    List<AssetMessage> assets = new ArrayList<>();
    for (Document doc : collection.find(Filters.ne("deleted", true))) {
      assets.add(documentToAsset(doc));
    }
    return assets;
  }

  public AssetMessage getAssetById(String id) {
    Document doc = collection.find(Filters.eq("_id", id)).first();
    if (doc == null) {
      return null;
    }
    return documentToAsset(doc);
  }

  public AssetMessage saveAsset(String name, String type, byte[] data) throws IOException {
    return saveAsset(null, name, type, data);
  }

  public AssetMessage saveAsset(String id, String name, String type, byte[] data)
      throws IOException {
    if (id == null) {
      id = UUID.randomUUID().toString();
    }
    // Simple sanitization
    String safeName = name.replaceAll("[^a-zA-Z0-9.-]", "_");
    String filename = id + "_" + safeName;
    Path path = Paths.get(assetDir, filename);

    try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
      fos.write(data);
    }

    String sizeStr = humanReadableByteCountBin(data.length);
    String url =
        "/assets/" + filename; // Assuming static file serving is set up or we add a handler

    boolean isDefault = id.startsWith("default_");
    Document doc =
        new Document("_id", id)
            .append("name", name)
            .append("type", type)
            .append("size", sizeStr)
            .append("filename", filename) // Store internal filename
            .append("url", url);
    if (isDefault) {
      doc.append("is_default", true);
    }

    collection.insertOne(doc);

    return documentToAsset(doc);
  }

  public boolean deleteAsset(String id) {
    Document doc = collection.find(Filters.eq("_id", id)).first();
    if (doc == null) {
      return false;
    }

    // Default assets get soft-deleted to preserve deletion status for backfills
    if (doc.getBoolean("is_default", false) || id.startsWith("default_")) {
      collection.updateOne(Filters.eq("_id", id), Updates.set("deleted", true));
      return true;
    }

    // Non-default assets get physically deleted
    // Delete single file if present
    String filename = doc.getString("filename");
    if (filename != null) {
      deletePhysicalFile(filename);
    }

    // Delete images in set if present
    @SuppressWarnings("unchecked")
    List<Document> imagesList = (List<Document>) doc.get("images");
    if (imagesList != null) {
      for (Document imageDoc : imagesList) {
        String url = imageDoc.getString("url");
        if (url != null && url.startsWith("/assets/")) {
          String setFilename = url.substring("/assets/".length());
          deletePhysicalFile(setFilename);
        }
      }
    }

    collection.deleteOne(Filters.eq("_id", id));
    return true;
  }

  private void deletePhysicalFile(String filename) {
    File file = new File(assetDir, filename);
    if (file.exists()) {
      if (!file.delete()) {
        System.err.println("Failed to delete file: " + file.getAbsolutePath());
      }
    }
  }

  public boolean renameAsset(String id, String newName) {
    Bson filter = Filters.eq("_id", id);
    Bson update = Updates.set("name", newName);
    long modifiedCount = collection.updateOne(filter, update).getModifiedCount();
    return modifiedCount > 0;
  }

  public AssetMessage saveImageSet(String id, String name, List<SaveImageSetEntry> entries)
      throws IOException {
    boolean isNew = (id == null || id.isEmpty());
    if (isNew) {
      id = UUID.randomUUID().toString();
    }

    List<Document> imageDocs = new ArrayList<>();
    long totalSize = 0;

    for (SaveImageSetEntry entry : entries) {
      String url = entry.getUrl();
      String entryName = entry.getName();
      int percentage = entry.getPercentage();
      String sizeStr = "";

      if (entry.getData() != null && !entry.getData().isEmpty()) {
        // New image data uploaded as part of the set
        String entryId = UUID.randomUUID().toString();
        String safeName = entryName.replaceAll("[^a-zA-Z0-9.-]", "_");
        String filename = entryId + "_" + safeName;
        Path path = Paths.get(assetDir, filename);

        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
          fos.write(entry.getData().toByteArray());
        }

        url = "/assets/" + filename;
        sizeStr = humanReadableByteCountBin(entry.getData().size());
        totalSize += entry.getData().size();
      } else if (url != null && !url.isEmpty()) {
        // Existing image reference
        if (url.startsWith("/assets/")) {
          String filename = url.substring("/assets/".length());
          File file = new File(assetDir, filename);
          if (file.exists()) {
            totalSize += file.length();
            sizeStr = humanReadableByteCountBin(file.length());
          }
        }
      }

      imageDocs.add(
          new Document()
              .append("url", url)
              .append("percentage", percentage)
              .append("name", entryName)
              .append("size", sizeStr));
    }

    Document doc =
        new Document("_id", id)
            .append("name", name)
            .append("type", "image_set")
            .append("is_default", id.startsWith("default_"))
            .append("size", humanReadableByteCountBin(totalSize))
            .append("url", imageDocs.isEmpty() ? "" : imageDocs.get(0).getString("url"))
            .append("images", imageDocs);

    if (isNew) {
      collection.insertOne(doc);
    } else {
      collection.replaceOne(Filters.eq("_id", id), doc);
    }

    return documentToAsset(doc);
  }

  private AssetMessage documentToAsset(Document doc) {
    AssetMessage.Builder builder =
        AssetMessage.newBuilder()
            .setModel(Model.newBuilder().setEntityId(doc.getString("_id")).build())
            .setName(doc.getString("name"))
            .setType(doc.getString("type"))
            .setSize(doc.getString("size"))
            .setUrl(doc.getString("url") != null ? doc.getString("url") : "");

    @SuppressWarnings("unchecked")
    List<Document> imagesList = (List<Document>) doc.get("images");
    if (imagesList != null) {
      for (Document imageDoc : imagesList) {
        builder.addImages(
            ImageSetEntry.newBuilder()
                .setUrl(imageDoc.getString("url"))
                .setPercentage(imageDoc.getInteger("percentage"))
                .setName(imageDoc.getString("name"))
                .setSize(imageDoc.getString("size"))
                .build());
      }
    }

    return builder.build();
  }

  private byte[] readResource(String path) throws IOException {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) {
        throw new IOException("Resource not found: " + path);
      }
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int nRead;
      byte[] data = new byte[1024];
      while ((nRead = is.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
      return buffer.toByteArray();
    }
  }

  private static String humanReadableByteCountBin(long bytes) {
    long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
    if (absB < 1024) {
      return bytes + " B";
    }
    long value = absB;
    CharacterIterator ci = new StringCharacterIterator("KMGTPE");
    for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
      value >>= 10;
      ci.next();
    }
    value *= Long.signum(bytes);
    return String.format("%.1f %ciB", value / 1024.0, ci.current());
  }

  public void resetAssets() {
    // 1. Clear directory
    File directory = new File(assetDir);
    if (directory.exists()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (!file.delete()) {
            System.err.println("Failed to delete file during reset: " + file.getAbsolutePath());
          }
        }
      }
    }

    // 2. Clear DB
    collection.drop();

    // 3. Restore defaults
    List<ImageSetEntry> fuelImages = new ArrayList<>();
    long fuelTotalSize = 0;

    for (DefaultAsset asset : DEFAULT_IMAGE_ASSETS) {
      try {
        byte[] data = readResource("/defaults/" + asset.filename);
        saveAsset(asset.id, asset.filename, "image", data);
      } catch (IOException | NumberFormatException e) {
        System.err.println(
            "Failed to restore default asset " + asset.filename + ": " + e.getMessage());
      }
    }

    for (DefaultAsset asset : DEFAULT_FUEL_IMAGE_ASSETS) {
      try {
        byte[] data = readResource("/defaults/" + asset.filename);
        // It's a fuel gauge, part of the set
        String safeName = asset.filename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String internalFilename = asset.id + "_" + safeName;
        Path path = Paths.get(assetDir, internalFilename);
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
          fos.write(data);
        }
        String url = "/assets/" + internalFilename;
        String sizeStr = humanReadableByteCountBin(data.length);

        fuelImages.add(
            ImageSetEntry.newBuilder()
                .setUrl(url)
                .setPercentage(((FuelDefaultAsset) asset).percentage)
                .setName(asset.displayName)
                .setSize(sizeStr)
                .build());
        fuelTotalSize += data.length;
      } catch (IOException | NumberFormatException e) {
        System.err.println(
            "Failed to restore default asset " + asset.filename + ": " + e.getMessage());
      }
    }

    // Save the Fuel Gauge image set
    if (!fuelImages.isEmpty()) {
      // Images are already in descending order (100 to 0) from
      // DEFAULT_FUEL_IMAGE_ASSETS

      String id = "default_fuel-gauge-builtin";
      List<Document> imageDocs = new ArrayList<>();
      for (ImageSetEntry entry : fuelImages) {
        imageDocs.add(
            new Document()
                .append("url", entry.getUrl())
                .append("percentage", entry.getPercentage())
                .append("name", entry.getName())
                .append("size", entry.getSize()));
      }

      Document doc =
          new Document("_id", id)
              .append("name", "Fuel Gauge")
              .append("type", "image_set")
              .append("is_default", true)
              .append("size", humanReadableByteCountBin(fuelTotalSize))
              .append("url", fuelImages.get(0).getUrl()) // Use 100% (now at index 0) as thumbnail
              .append("images", imageDocs);

      collection.insertOne(doc);
    }

    for (DefaultAsset asset : DEFAULT_AUDIO_ASSETS) {
      try {
        saveAsset(
            asset.id, asset.displayName, "sound", readResource("/defaults/" + asset.filename));
      } catch (IOException e) {
        System.err.println(
            "Failed to restore default asset " + asset.filename + ": " + e.getMessage());
      }
    }
  }

  public void backfillDefaults() {
    List<ImageSetEntry> fuelImages = new ArrayList<>();
    long fuelTotalSize = 0;

    for (DefaultAsset asset : DEFAULT_IMAGE_ASSETS) {
      if (collection.find(Filters.eq("_id", asset.id)).first() != null) {
        continue;
      }
      try {
        byte[] data = readResource("/defaults/" + asset.filename);
        saveAsset(asset.id, asset.filename, "image", data);
      } catch (IOException | NumberFormatException e) {
        System.err.println(
            "Failed to backfill default asset " + asset.filename + ": " + e.getMessage());
      }
    }

    String fuelId = "default_fuel-gauge-builtin";
    if (collection.find(Filters.eq("_id", fuelId)).first() == null) {
      for (DefaultAsset asset : DEFAULT_FUEL_IMAGE_ASSETS) {
        try {
          byte[] data = readResource("/defaults/" + asset.filename);
          // It's a fuel gauge, part of the set
          String safeName = asset.filename.replaceAll("[^a-zA-Z0-9.-]", "_");
          String internalFilename = asset.id + "_" + safeName;
          Path path = Paths.get(assetDir, internalFilename);
          try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(data);
          }
          String url = "/assets/" + internalFilename;
          String sizeStr = humanReadableByteCountBin(data.length);

          fuelImages.add(
              ImageSetEntry.newBuilder()
                  .setUrl(url)
                  .setPercentage(((FuelDefaultAsset) asset).percentage)
                  .setName(asset.displayName)
                  .setSize(sizeStr)
                  .build());
          fuelTotalSize += data.length;
        } catch (IOException | NumberFormatException e) {
          System.err.println(
              "Failed to backfill default fuel asset " + asset.filename + ": " + e.getMessage());
        }
      }

      // Save the Fuel Gauge image set
      if (!fuelImages.isEmpty()) {
        List<Document> imageDocs = new ArrayList<>();
        for (ImageSetEntry entry : fuelImages) {
          imageDocs.add(
              new Document()
                  .append("url", entry.getUrl())
                  .append("percentage", entry.getPercentage())
                  .append("name", entry.getName())
                  .append("size", entry.getSize()));
        }

        Document doc =
            new Document("_id", fuelId)
                .append("name", "Fuel Gauge")
                .append("type", "image_set")
                .append("is_default", true)
                .append("size", humanReadableByteCountBin(fuelTotalSize))
                .append("url", fuelImages.get(0).getUrl())
                .append("images", imageDocs);

        collection.insertOne(doc);
      }
    }

    for (DefaultAsset asset : DEFAULT_AUDIO_ASSETS) {
      if (collection.find(Filters.eq("_id", asset.id)).first() != null) {
        continue;
      }
      try {
        saveAsset(
            asset.id, asset.displayName, "sound", readResource("/defaults/" + asset.filename));
      } catch (IOException e) {
        System.err.println(
            "Failed to backfill default asset " + asset.filename + ": " + e.getMessage());
      }
    }

    // Backfill default theme
    backfillDefaultTheme();

    // Ensure all themes have the new audio slot
    backfillThemeSlots();
  }

  /** Ensures all themes have the 'audio.yellowflag' slot in the audio_slots map. */
  public void backfillThemeSlots() {
    MongoCollection<Document> themes = database.getCollection("themes");
    for (Document theme : themes.find()) {
      Document slots = (Document) theme.get("slots");
      Document audioSlots = (Document) theme.get("audio_slots");
      if (audioSlots == null) {
        audioSlots = new Document();
      }

      List<Bson> allUpdates = new ArrayList<>();
      boolean audioSlotsChanged = false;

      if (slots == null) {
        slots = new Document();
        allUpdates.add(Updates.set("slots", slots));
      }

      // 1. Repair corrupted slots (nested documents caused by incorrect dot-notation updates)
      if (slots.get("flag") instanceof Document) {
        Document flagDoc = (Document) slots.get("flag");
        for (String subKey : flagDoc.keySet()) {
          allUpdates.add(Updates.set("slots.flag." + subKey, flagDoc.get(subKey)));
          slots.append("flag." + subKey, flagDoc.get(subKey));
        }
        allUpdates.add(Updates.unset("slots.flag"));
        slots.remove("flag");
      }
      if (slots.get("audio") instanceof Document) {
        Document audioDoc = (Document) slots.get("audio");
        for (String subKey : audioDoc.keySet()) {
          allUpdates.add(Updates.set("slots.audio." + subKey, audioDoc.get(subKey)));
          slots.append("audio." + subKey, audioDoc.get(subKey));
        }
        allUpdates.add(Updates.unset("slots.audio"));
        slots.remove("audio");
      }

      // 2. Migration: Move from slots to audio_slots if present
      if (slots.containsKey("audio.yellowflag")) {
        Object val = slots.get("audio.yellowflag");
        if (val instanceof String) {
          String assetId = (String) val;
          if (!audioSlots.containsKey("audio.yellowflag")) {
            audioSlots.append(
                "audio.yellowflag", new Document("type", "preset").append("url", assetId));
            audioSlotsChanged = true;
          }
        }
        allUpdates.add(Updates.unset("slots.audio.yellowflag"));
        slots.remove("audio.yellowflag");
      }

      // 3. Backfill flag.yellowgreen image slot
      if (!slots.containsKey("flag.yellowgreen")) {
        allUpdates.add(Updates.set("slots.flag.yellowgreen", "default_flag_green_yellow"));
        slots.append("flag.yellowgreen", "default_flag_green_yellow");
      }

      // 4. Default backfill for audio.yellowflag if still missing
      if (!audioSlots.containsKey("audio.yellowflag")) {
        audioSlots.append(
            "audio.yellowflag",
            new Document("type", "preset").append("url", "default_yellow_flag"));
        audioSlotsChanged = true;
      }

      // 5. Countdown backfill
      String[] countdownKeys = {
        "audio.countdown.5",
        "audio.countdown.4",
        "audio.countdown.3",
        "audio.countdown.2",
        "audio.countdown.1",
        "audio.countdown.go"
      };
      String[] defaultCountdownAssets = {
        "default_countdown_5",
        "default_countdown_4",
        "default_countdown_3",
        "default_countdown_2",
        "default_countdown_1",
        "default_countdown_go"
      };

      for (int i = 0; i < countdownKeys.length; i++) {
        String key = countdownKeys[i];
        String defaultAsset = defaultCountdownAssets[i];
        if (!audioSlots.containsKey(key)) {
          audioSlots.append(key, new Document("type", "preset").append("url", defaultAsset));
          audioSlotsChanged = true;
        }
      }

      // 6. Save updates if anything changed
      if (audioSlotsChanged) {
        allUpdates.add(Updates.set("audio_slots", audioSlots));
      }

      if (!allUpdates.isEmpty()) {
        themes.updateOne(Filters.eq("_id", theme.get("_id")), Updates.combine(allUpdates));
      }
    }
  }

  /**
   * Creates the "RaceCoordinator AI (default)" default theme if it doesn't already exist. This
   * theme maps all built-in asset slots (flags, start lamps, fuel gauge) to their default asset
   * IDs.
   */
  void backfillDefaultTheme() {
    MongoCollection<Document> themes = database.getCollection("themes");
    String themeId = "default_classic_rc_ai";

    // Migration: Remove any legacy theme document.
    themes.deleteOne(Filters.eq("entity_id", themeId));

    if (themes.find(Filters.eq("entity_id", themeId)).first() != null) {
      return; // Already exists with proper ObjectId
    }

    Document slots = new Document();
    // Flags
    slots.append("flag.green", "default_flag_green");
    slots.append("flag.red", "default_flag_red");
    slots.append("flag.yellow", "default_flag_yellow");
    slots.append("flag.yellowgreen", "default_flag_green_yellow");
    slots.append("flag.white", "default_flag_white");
    slots.append("flag.checkered", "default_flag_checkered");
    slots.append("flag.black", "default_flag_black");
    // Start lamps
    slots.append("lamp.red.on", "default_start_red_on");
    slots.append("lamp.red.dim", "default_start_red_dim");
    slots.append("lamp.green", "default_start_green");
    // Fuel gauge image set
    slots.append("gauge.fuel", "default_fuel-gauge-builtin");

    Document audioSlots = new Document();
    // Audio
    audioSlots.append(
        "audio.yellowflag", new Document("type", "preset").append("url", "default_yellow_flag"));
    audioSlots.append(
        "audio.countdown.5", new Document("type", "preset").append("url", "default_countdown_5"));
    audioSlots.append(
        "audio.countdown.4", new Document("type", "preset").append("url", "default_countdown_4"));
    audioSlots.append(
        "audio.countdown.3", new Document("type", "preset").append("url", "default_countdown_3"));
    audioSlots.append(
        "audio.countdown.2", new Document("type", "preset").append("url", "default_countdown_2"));
    audioSlots.append(
        "audio.countdown.1", new Document("type", "preset").append("url", "default_countdown_1"));
    audioSlots.append(
        "audio.countdown.go", new Document("type", "preset").append("url", "default_countdown_go"));

    Document theme =
        new Document()
            .append("entity_id", themeId)
            .append("name", "RaceCoordinator AI (default)")
            .append("is_default", true)
            .append("slots", slots)
            .append("audio_slots", audioSlots);

    themes.insertOne(theme);
    System.out.println("Default theme 'RaceCoordinator AI (default)' created.");
  }
}
