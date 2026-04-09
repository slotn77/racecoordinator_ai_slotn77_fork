package com.antigravity.models;

import com.antigravity.race.ServerToClientObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public class Model extends ServerToClientObject {

  private final ObjectId id;

  @BsonProperty("entity_id")
  @JsonProperty("entity_id")
  private final String entityId;

  @BsonCreator
  public Model(
      @BsonId @BsonProperty("_id") @JsonProperty("_id") ObjectId id,
      @BsonProperty("entity_id") @JsonProperty("entity_id") String entityId) {
    super(entityId);
    this.id = id;
    this.entityId = entityId;
  }

  public ObjectId getId() {
    return id;
  }

  public String getEntityId() {
    return entityId;
  }
}
