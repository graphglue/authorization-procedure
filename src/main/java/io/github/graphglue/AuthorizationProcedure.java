package io.github.graphglue;

import java.util.stream.Stream;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.internal.helpers.collection.Iterables;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class AuthorizationProcedure {

	@Context
	public Transaction tx;

	@Procedure(name = "io.github.graphglue.authorizationPath", mode = Mode.READ)
	public Stream<AuthorizationPathResult> authorizationPath(
			@Name("startNode") Node start,
			@Name("permission") String permission
	) {
		final TraversalDescription traversalDescription = tx.traversalDescription()
				.breadthFirst()
				.expand(new PermissionFieldPathExpander(permission))
				.uniqueness(Uniqueness.RELATIONSHIP_PATH);

		final Traverser traverser = traversalDescription.traverse(start);
		return Iterables.stream(traverser).map(path -> new AuthorizationPathResult(path, path.endNode()));
	}
}
