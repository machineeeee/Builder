package camp.computer.clay.util.geometry;

import java.util.LinkedList;
import java.util.List;

import camp.computer.clay.engine.entity.Entity;
import camp.computer.clay.engine.component.Transform;

public class Line extends Shape {

    public Line() {
        super();
        setup();
    }

    public Line(Transform position, double rotation) {
        setup();
        position.set(position);
        position.setRotation(rotation);
    }

    private void setup() {
        setupGeometry();
    }

    private void setupGeometry() {
    }

    @Override
    public List<Transform> getVertices() {
        List<Transform> vertices = new LinkedList<>();
//        vertices.add(new Transform(source));
//        vertices.add(new Transform(target));
        return vertices;
    }

    /**
     * Returns a {@code Transform} on the {@code Line} offset from {@code position} by {@code offset}.
     *
     * @param offset
     * @return
     */
    public Transform getPoint(double offset) {
        return Geometry.getRotateTranslatePoint(position, position.rotation, offset);
    }
}
