package io.github.graphglue;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.Iterables;

/**
 * Path expander which only expands outgoing relations,
 * and only where the permission property is set to true.
 */
class PermissionFieldPathExpander implements PathExpander<Object> {

    /**
     * The name of the permission property.
     */
    private final String permission;

    /**
     * Create a new instance of this path expander.
     *
     * @param permission The name of the permission property.
     */
    public PermissionFieldPathExpander(final String permission) {
        this.permission = permission;
    }

    @Override
    public ResourceIterable<Relationship> expand(Path path, BranchState state) {
        final Node node = path.endNode();
        final ResourceIterable<Relationship> outgoingRelationships = node.getRelationships(Direction.OUTGOING);
        return Iterables.asResourceIterable(Iterables.filter(outgoingRelationships, relationship -> {
            final Object propertyValue = relationship.getProperty(this.permission, null);
            return propertyValue != null && propertyValue.equals(true);
        }));
    }

    @Override
    public PathExpander<Object> reverse() {
        throw new RuntimeException("Not implemented");
    }
}