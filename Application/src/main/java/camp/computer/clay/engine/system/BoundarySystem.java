package camp.computer.clay.engine.system;

import java.util.ArrayList;
import java.util.List;

import camp.computer.clay.engine.World;
import camp.computer.clay.engine.component.Boundary;
import camp.computer.clay.engine.component.Primitive;
import camp.computer.clay.engine.component.Transform;
import camp.computer.clay.engine.entity.Entity;
import camp.computer.clay.engine.manager.Group;
import camp.computer.clay.lib.Geometry.Shape;
import camp.computer.clay.util.Geometry;

public class BoundarySystem extends System {

    Group<Entity> entitiesWithBoundary;

    public BoundarySystem(World world) {
        super(world);

        entitiesWithBoundary = world.entityManager.subscribe(Group.Filters.filterWithComponents, Primitive.class, Boundary.class);
    }

    @Override
    public void update(long dt) {

        // Updates Boundary components
        for (int i = 0; i < entitiesWithBoundary.size(); i++) {
            computeBoundary(entitiesWithBoundary.get(i));
        }
    }

    private void computeBoundary(Entity entity) {
        // TODO: Cache_OLD the boundary and only update when it has been invalidated!

        Shape shape = entity.getComponent(Primitive.class).shape;

        if (shape.getVertices() != null && shape.getVertices().size() > 0) {
            List<Transform> vertices = shape.getVertices();
            List<Transform> boundary = new ArrayList<>(vertices);

            // Translate and rotate the boundary about the updated position
            for (int i = 0; i < vertices.size(); i++) {
                boundary.get(i).set(vertices.get(i));
                Geometry.rotatePoint(boundary.get(i), entity.getComponent(Transform.class).rotation); // Rotate Shape boundary about ModelBuilder position
                Geometry.translatePoint(boundary.get(i), entity.getComponent(Transform.class).x, entity.getComponent(Transform.class).y); // Translate Shape
            }

            // Set the Boundary
//            entity.getComponent(Boundary.class).boundary.clear();
//            entity.getComponent(Boundary.class).boundary.addAll(vertices);
            entity.getComponent(Boundary.class).boundary = vertices;
        }
    }
}
