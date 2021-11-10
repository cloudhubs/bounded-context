package edu.baylor.ecs.prophet.bounded.context.utils.impl.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;

import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.Entity;
import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.Field;
import edu.baylor.ecs.cloudhubs.prophetdto.systemcontext.Module;
import lombok.Getter;

/**
 * An container that takes two modules and stores all entities within the first
 * based on whether they have a likely duplicate in another specified module.
 * 
 * @author Micah
 */
@Getter
public class EntityCollection {

	/** Mapping of entity */
	private final Set<SimilarityRecord> similarEntities = new HashSet<>();

	/** Entities with no similar entities to map to */
	private final Set<Entity> distinctEntities = new HashSet<>();

	/**
	 * Construct a collection that partitions the merged
	 * 
	 * @param mod1
	 * @param mod2
	 * @param matchThreshhold
	 * @param computeSimilarity
	 */
	public EntityCollection(Module mod1, Module mod2, double matchThreshhold,
			BiFunction<Entity, Entity, ImmutablePair<Double, Map<Field, Field>>> computeSimilarity) {
		// Validate input
		Objects.requireNonNull(mod1, "mod1 cannot be null");
		Objects.requireNonNull(mod2, "mod2 cannot be null");

		// Determine which entities have no matches at all
		var splitEntityRecords = mod1.getEntities().stream()
				// Map entities in module 1 to their most similar counterpart in module 2
				.map(mod1Entity -> findMostSimilar(mod1Entity, mod2, computeSimilarity))
				// Split into those with a match in the module 2 and those without
				.collect(Collectors.partitioningBy(Optional::isPresent));

		// All entities with no match are distinct; put them in distinct lists
		splitEntityRecords.get(false).stream().map(entry -> entry.get().getRight().entity().clone())
				.forEach(distinctEntities::add);

		// Use similarity score to categorize similar and distinct entities
		splitEntityRecords.get(true).forEach(similarityRecord -> {
			ImmutablePair<Double, SimilarityRecord> record = similarityRecord.get();
			Entity entity = record.getRight().entity();
			if (record.getLeft() > matchThreshhold) {
				// Distinct, store to distinct list
				distinctEntities.add(entity.copyWithNamePreface(mod1.getName() + "::"));
			} else {
				// Similar, store to similar map
				similarEntities.add(record.getRight());
			}
		});
	}

	/**
	 * Find the highest similarity entity within the provided module
	 * 
	 * @param entity            Entity to compare
	 * @param mod2              Module to search
	 * @param computeSimilarity Callback to compute similarity between entities
	 * @return tuple (similarity, entity, field_mapping)
	 */
	private Optional<ImmutablePair<Double, SimilarityRecord>> findMostSimilar(Entity entity, Module mod2,
			BiFunction<Entity, Entity, ImmutablePair<Double, Map<Field, Field>>> computeSimilarity) {
		return mod2.getEntities().stream().map(mod2Entity -> {
			// Convert into a (similarity, entity, field_mappings) tuple
			var simil = computeSimilarity.apply(entity, mod2Entity);
			return ImmutablePair.of(simil.getLeft(), new SimilarityRecord(entity, mod2Entity, simil.getRight()));
		})
				// Sort by similarity score, highest to lowest
				.sorted((entity1, entity2) -> entity2.getLeft().compareTo(entity1.getLeft()))

				// Take highest score
				.findFirst();
	}
}
