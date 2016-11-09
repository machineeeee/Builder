package camp.computer.clay.engine.component;

import java.util.UUID;

import camp.computer.clay.engine.Group;
import camp.computer.clay.engine.World;
import camp.computer.clay.engine.entity.Entity;

public class Path extends Component {

    public enum Direction {

        NONE(0),   // sourcePortUuid  |  destination
        OUTPUT(1), // sourcePortUuid --> destination
        INPUT(2),  // sourcePortUuid <-- destination
        BOTH(3);   // sourcePortUuid <-> destination

        // TODO: Change the index to a UUID?
        int index;

        Direction(int index) {
            this.index = index;
        }
    }

    public enum Mode {

        NONE(0),
        ELECTRONIC(1),
        BLUETOOTH(2),
        MESH(3),
        INTERNET(4);

        // TODO: NONE, ELECTRONIC, MESH, INTERNET, BLUETOOTH
        // TODO: TCP, UDP, HTTP, HTTPS

        // TODO: Change the index to a UUID?
        int index;

        Mode(int index) {
            this.index = index;
        }

        public static Mode getNext(Mode currentType) {
            return Mode.values()[(currentType.index + 1) % Mode.values().length];
        }
    }

    // TODO: none, 5v, 3.3v, (data) I2C, SPI, (monitor) A2D, voltage, current
    public enum Type {
        NONE,
        SWITCH,
        PULSE,
        WAVE,
        POWER_REFERENCE,
        POWER_CMOS,
        POWER_TTL; // TODO: Should contain parameters for voltage (5V, 3.3V), current (constant?).

        public static Path.Type getNext(Path.Type currentType) {
            Path.Type[] values = Path.Type.values();
            int currentIndex = java.util.Arrays.asList(values).indexOf(currentType);
            return values[(currentIndex + 1) % values.length];
        }
    }

    // TODO: public enum Protocol (i.e., BLUETOOTH, TCP, UDP, HTTP, HTTPS)

    private Mode mode = Mode.NONE;

    private Type type = Type.NONE;

    private Direction direction = Direction.NONE;

    private UUID sourcePortUuid;

    private UUID targetPortUuid;

//    public boolean IS_EDITING = false;

    public enum State {
        NONE,
        EDITING
    }

    public State state = State.NONE;

    public Path() {
        super();
        setup();
    }

    private void setup() {
        this.mode = Mode.ELECTRONIC;
        this.type = Type.NONE; // Default to ELECTRONIC
        this.direction = Direction.BOTH; // Default to BOTH

        // TODO: PathEntity.connectPath(sourcePortUuid, destination) and do what the following constructor does... auto-configure Ports and PathEntity
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

//    public void setMode(Port.Type type) {
//        // Update type of Ports in PathEntity (BUT NOT DIRECTION)
//        // <FILTER>
//        // TODO: Make PathEntity.Filter
//        Group<Entity> ports = getPorts();
//        for (int i = 0; i < ports.size(); i++) {
//            Entity portEntity = ports.get(i);
//            portEntity.getComponent(Port.class).setType(type);
//        }
//        // </FILTER>
//    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void set(Entity sourcePort, Entity targetPort) {

        this.mode = Mode.ELECTRONIC; // Default to ELECTRONIC
        if (this.type == Type.NONE) {
            this.type = Type.getNext(this.type);
        }
        this.direction = Direction.BOTH; // Default to BOTH

        this.sourcePortUuid = sourcePort.getUuid();
        this.targetPortUuid = targetPort.getUuid();

        // Update sourcePortUuid PortEntity configuration
        if (Port.getDirection(sourcePort) == Port.Direction.NONE) {
            Port.setDirection(sourcePort, Port.Direction.BOTH); // Default to BOTH
        }
        if (Port.getType(sourcePort) == Port.Type.NONE) {
            Port.setType(sourcePort, Port.Type.getNext(Port.getType(sourcePort)));
        }

        // Update targetPortUuid PortEntity configuration
        if (Port.getDirection(targetPort) == Port.Direction.NONE) {
            Port.setDirection(targetPort, Port.Direction.BOTH); // Default to BOTH
        }
        if (Port.getType(targetPort) == Port.Type.NONE) {
            Port.setType(targetPort, Port.getType(sourcePort));
        }
    }

    public void setSource(Entity source) {
        if (source == null) {
            this.sourcePortUuid = null;
        } else {
            this.sourcePortUuid = source.getUuid();
        }
    }

    public Entity getSource() {
        return World.getWorld().Manager.getEntities().get(sourcePortUuid);
    }

    public void setTarget(Entity target) {
        if (target == null) {
            this.targetPortUuid = null;
        } else {
            this.targetPortUuid = target.getUuid();
        }
    }

    public Entity getTarget() {
        return World.getWorld().Manager.getEntities().get(targetPortUuid);
    }

    public Group<Entity> getPorts() {
        Group<Entity> ports = new Group<>();
        if (getSource() != null) {
            ports.add(getSource());
        }
        if (getTarget() != null) {
            ports.add(getTarget());
        }
        return ports;
    }

    public Entity getHost() {
        if (getSource().getParent().hasComponent(Host.class)) {
            return getSource().getParent();
        } else if (getTarget().getParent().hasComponent(Host.class)) {
            return getTarget().getParent();
        }
        return null;
    }

    public Entity getExtension() {
        if (getSource().getParent().hasComponent(Extension.class)) {
            return getSource().getParent();
        } else if (getTarget().getParent().hasComponent(Extension.class)) {
            return getTarget().getParent();
        }
        return null;
    }

    public Entity getHostPort() {
        if (getSource().getParent().hasComponent(Host.class)) {
            return getSource();
        } else if (getTarget().getParent().hasComponent(Host.class)) {
            return getTarget();
        }
        return null;
    }

    public boolean contains(Entity port) {
        if (this.sourcePortUuid == port.getUuid() || this.targetPortUuid == port.getUuid()) {
            return true;
        } else {
            return false;
        }
    }
}
