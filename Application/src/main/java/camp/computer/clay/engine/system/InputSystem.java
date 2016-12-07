package camp.computer.clay.engine.system;

import java.util.ArrayList;
import java.util.List;

import camp.computer.clay.engine.World;
import camp.computer.clay.engine.component.Boundary;
import camp.computer.clay.engine.component.Camera;
import camp.computer.clay.engine.component.Model;
import camp.computer.clay.engine.component.Primitive;
import camp.computer.clay.engine.component.Transform;
import camp.computer.clay.engine.entity.Entity;
import camp.computer.clay.engine.event.Event;
import camp.computer.clay.engine.manager.Group;

public class InputSystem extends System {

    private List<Event> eventQueue = new ArrayList<>();

    Group<Entity> cameraEntities;

    public InputSystem(World world) {
        super(world);
        setup();
    }

    private void setup() {
        cameraEntities = world.entities.subscribe(Group.Filters.filterWithComponents, Camera.class);
    }

    private Event previousEvent = null;

    public void update(long dt) {
        while (eventQueue.size() > 0) {
            world.getSystem(EventSystem.class).queue(process(dequeue()));
        }
    }

    public void queue(Event event) {
        eventQueue.add(event);
    }

    private Event dequeue() {
        return eventQueue.remove(0);
    }

    private Event process(Event event) {

        // Set world position
        for (int i = 0; i < Event.MAXIMUM_POINT_COUNT; i++) {
            // TODO: Update equations so cameraScale is always the correct scale, the current scale, and computed as needed.
            Transform origin = world.engine.platform.getRenderSurface().originTransform; // REFACTOR
            event.pointerCoordinates[i].x = (event.surfaceCoordinates[i].x - (origin.x + cameraEntities.get(0).getComponent(Transform.class).x)) / cameraEntities.get(0).getComponent(Transform.class).scale;
            event.pointerCoordinates[i].y = (event.surfaceCoordinates[i].y - (origin.y + cameraEntities.get(0).getComponent(Transform.class).y)) / cameraEntities.get(0).getComponent(Transform.class).scale;
        }

        if (event.getType() == world.events.getEventType("SELECT")) {
            // Set previous Event
            previousEvent = event;
        } else if (event.getType() == world.events.getEventType("HOLD")) {
            // Set previous Event
            if (previousEvent != null) {
                event.setPreviousEvent(previousEvent);
            } else {
                event.setPreviousEvent(null);
            }
            previousEvent = event;

            // <REFACTOR>
            // There might be a better way to do this. How can I assign reasonable coordinates to the synthetic HOLD event?
            // TODO: Set coordinates of hold... to first event?
            Event firstEvent = event.getFirstEvent();
            for (int i = 0; i < firstEvent.pointerCoordinates.length; i++) {
                event.pointerCoordinates[i].x = firstEvent.pointerCoordinates[i].x;
                event.pointerCoordinates[i].y = firstEvent.pointerCoordinates[i].y;
            }
            // </REFACTOR>

        } else if (event.getType() == world.events.getEventType("MOVE")) {

            // Set previous Event
            event.setPreviousEvent(previousEvent);
            previousEvent = event;

        } else if (event.getType() == world.events.getEventType("UNSELECT")) {

            // Set previous Event
            event.setPreviousEvent(previousEvent);
            previousEvent = event;

        }

//        switch (event.getType()) {
//            case SELECT: {
//                // Set previous Event
//                previousEvent = event;
//                break;
//            }
//
//            case HOLD: {
//
//                // Set previous Event
//                if (previousEvent != null) {
//                    event.setPreviousEvent(previousEvent);
//                } else {
//                    event.setPreviousEvent(null);
//                }
//                previousEvent = event;
//
//                // <REFACTOR>
//                // There might be a better way to do this. How can I assign reasonable coordinates to the synthetic HOLD event?
//                // TODO: Set coordinates of hold... to first event?
//                Event firstEvent = event.getFirstEvent();
//                for (int i = 0; i < firstEvent.pointerCoordinates.length; i++) {
//                    event.pointerCoordinates[i].x = firstEvent.pointerCoordinates[i].x;
//                    event.pointerCoordinates[i].y = firstEvent.pointerCoordinates[i].y;
//                }
//                // </REFACTOR>
//
//                break;
//            }
//
//            case MOVE: {
//
//                // Set previous Event
//                event.setPreviousEvent(previousEvent);
//                previousEvent = event;
//
//                break;
//            }
//
//            case UNSELECT: {
//
//                // Set previous Event
//                event.setPreviousEvent(previousEvent);
//                previousEvent = event;
//
//                break;
//            }
//        }

        setTargets(event);

        return event;
    }

    public Entity previousPrimaryTarget = null;

    private void setTargets(Event event) {

        Entity primaryTarget = null;

        // Handle special cases for SELECT and non-SELECT events
        //if (event.getType() != Event.Type.SELECT) {
        if (event.getType() != world.events.getEventType("SELECT")) {
            event.setTarget(event.getFirstEvent().getTarget());
            event.setSecondaryTarget(event.getFirstEvent().getSecondaryTarget());
        } else {

            // Assign target Entities
            // <REFACTOR>
            Group<Entity> primaryBoundaries = world.entities.get().filterVisibility(true).filterWithComponents(Model.class, Boundary.class).sortByLayer().filterContains(event.getPosition());
            Group<Entity> secondaryBoundaries = world.entities.get().filterVisibility(true).filterWithComponents(Primitive.class, Boundary.class).filterContains(event.getPosition());
            // </REFACTOR>

            if (primaryBoundaries.size() > 0) {
                primaryTarget = primaryBoundaries.get(primaryBoundaries.size() - 1); // Get primary target from the top layer (will be last in the list of targets)
            } else {
                Group<Entity> cameras = world.entities.get().filterWithComponent(Camera.class);
                primaryTarget = cameras.get(0);
            }
            event.setTarget(primaryTarget);

            if (primaryTarget.hasComponent(Model.class)) { // Needed because entitiesWithBoundary like Camera without ModelBuilder component are also processed here.
                for (int i = 0; i < secondaryBoundaries.size(); i++) {
                    if (Model.getPrimitives(primaryTarget).contains(secondaryBoundaries.get(i))) {
                        event.setSecondaryTarget(secondaryBoundaries.get(i));
                    }
                }
            }
        }

        // Handle special bookkeeping storing previous target Entity
//        if (event.getType() == Event.Type.UNSELECT) {
        if (event.getType() == world.events.getEventType("UNSELECT")) {
            previousPrimaryTarget = event.getTarget();
        }
    }
}
