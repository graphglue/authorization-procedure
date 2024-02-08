package io.github.graphglue;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.internal.helpers.collection.Iterables;

class PermissionFieldPathExpander implements PathExpander {

    private final String permission;

    public PermissionFieldPathExpander(final String permission) {
        this.permission = permission;
    }

    @Override
    public ResourceIterable<Relationship> expand(Path path, BranchState state) {
        final Node node = path.endNode();
        final ResourceIterable<Relationship> outgoingRelationships = node.getRelationships(Direction.OUTGOING);
        return Iterables.asResourceIterable(Iterables.filter(relationship -> {
            final Object propertyValue = relationship.getProperty(this.permission, null);
            return propertyValue != null && propertyValue.equals(true);
        }, outgoingRelationships));
    }

    @Override
    public PathExpander reverse() {
        throw new RuntimeException("Not implemented");
    }
}