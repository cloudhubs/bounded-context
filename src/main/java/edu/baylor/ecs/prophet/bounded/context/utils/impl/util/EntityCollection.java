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

		// Split entities into two sets: those with a match, and those without
		var splitEntityRecords = mod1.getEntities().stream()
				.map(mod1Entity -> findMostSimilar(mod1Entity, mod2, computeSimilarity))
				.collect(Collectors.partitioningBy(Optional::isPresent));

		// All entities with no match are distinct; put them in the distinct list
		splitEntityRecords.get(false).stream().map(entry -> entry.get().getRight().entity().clone())
				.forEach(distinctEntities::add);

		// Use similarity score to find which matches are false positives
		Set<Entity> mappedInTwo = new HashSet<>(); // Index for which entities already handled
		for (var similarityRecord : splitEntityRecords.get(true)) {
			// Unwrap the optional/tuple
			ImmutablePair<Double, SimilarityRecord> record = similarityRecord.get();
			Entity entity = record.getRight().entity();

			// If the similarity threshhold is exceeded, this is a true match; otherwise,
			// record as a distinct entity.
			if (record.getLeft() > matchThreshhold) {
				mappedInTwo.add(entity);
				similarEntities.add(record.getRight());
			} else {
				distinctEntities.add(entity.copyWithNamePreface(mod1.getName() + "::"));
			}
		}

		// Take all fields from module 2 not already handled, and record them
		mod2.getEntities().stream().filter(e -> !mappedInTwo.contains(e)).forEach(distinctEntities::add);
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
