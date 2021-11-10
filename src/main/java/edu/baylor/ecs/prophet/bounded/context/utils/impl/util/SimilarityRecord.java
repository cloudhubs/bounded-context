package edu.baylor.ecs.prophet.bounded.context.utils.impl.util;

import java.util.Map;
import java.util.Objects;

import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.Entity;
import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.Field;

/**
 * Wraps an entity, an entity that has been identified as "similar", and the
 * mapping between which entity fields are similar.
 * 
 * @author Micah
 */
public record SimilarityRecord(Entity entity, Entity similarEntity, Map<Field, Field> fieldMap) {
	public SimilarityRecord {
		Objects.requireNonNull(entity, "entity cannot be null");
		Objects.requireNonNull(similarEntity, "similarEntity cannot be null");
		Objects.requireNonNull(fieldMap, "fieldMap cannot be null");
	}
}
