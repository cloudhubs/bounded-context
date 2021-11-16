package edu.baylor.ecs.prophet.bounded.context.utils.impl.util;

import java.util.Map;
import java.util.Objects;

import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.Entity;
import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.Field;
import lombok.Data;
import lombok.NonNull;

/**
 * Wraps an entity, an entity that has been identified as "similar", and the
 * mapping between which entity fields are similar.
 * 
 * @author Micah
 */
@Data
public class SimilarityRecord {
	@NonNull Entity entity;
	@NonNull Entity similarEntity;
	@NonNull Map<Field, Field> fieldMap;
}
