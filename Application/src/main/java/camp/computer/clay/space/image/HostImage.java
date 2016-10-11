package camp.computer.clay.space.image;

import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import camp.computer.clay.application.Application;
import camp.computer.clay.application.graphics.Display;
import camp.computer.clay.application.graphics.controls.Prompt;
import camp.computer.clay.model.Extension;
import camp.computer.clay.model.Group;
import camp.computer.clay.model.Host;
import camp.computer.clay.model.Path;
import camp.computer.clay.model.Port;
import camp.computer.clay.model.Portable;
import camp.computer.clay.model.action.Action;
import camp.computer.clay.model.action.ActionListener;
import camp.computer.clay.model.action.Camera;
import camp.computer.clay.model.action.Event;
import camp.computer.clay.model.profile.PortableProfile;
import camp.computer.clay.model.util.PathGroup;
import camp.computer.clay.model.util.PortGroup;
import camp.computer.clay.util.geometry.Circle;
import camp.computer.clay.util.geometry.Geometry;
import camp.computer.clay.util.geometry.Line;
import camp.computer.clay.util.geometry.Point;
import camp.computer.clay.util.geometry.Rectangle;
import camp.computer.clay.util.geometry.Vertex;
import camp.computer.clay.util.image.Image;
import camp.computer.clay.util.image.Shape;
import camp.computer.clay.util.image.Space;
import camp.computer.clay.util.image.Visibility;
import camp.computer.clay.util.image.util.ImageGroup;
import camp.computer.clay.util.image.util.ShapeGroup;

public class HostImage extends PortableImage {

    public List<List<Extension>> extensionAxes = new ArrayList<>();

    public HostImage(Host host) {
        super(host);
        setup();
    }

    private void setup() {
        setupGeometry();
        setupActions();

        extensionAxes.add(new ArrayList<Extension>());
        extensionAxes.add(new ArrayList<Extension>());
        extensionAxes.add(new ArrayList<Extension>());
        extensionAxes.add(new ArrayList<Extension>());
    }

    private void setupGeometry() {
        Rectangle rectangle;
        Circle circle;

        // Board
        rectangle = new Rectangle<>(getHost());
        rectangle.setWidth(250);
        rectangle.setHeight(250);
        rectangle.setCornerRadius(20.0);
        rectangle.setLabel("Substrate");
        rectangle.setColor("#1f1f1e"); // #f7f7f7
        rectangle.setOutlineThickness(1);
        addShape(rectangle);

        // Headers
        final double headerWidth = 6.0 * (2.54 * 3);

        rectangle = new Rectangle(headerWidth, 14);
        rectangle.setLabel("Header 1"); // or index 1 (top)
        rectangle.setPosition(0, -132);
        rectangle.setRotation(0);
        rectangle.setColor("#404040");
        rectangle.setOutlineThickness(0);
        addShape(rectangle);

        rectangle = new Rectangle(headerWidth, 14);
        rectangle.setLabel("Header 2"); // or index 2 (right)
        rectangle.setPosition(132, 0);
        rectangle.setRotation(90);
        rectangle.setColor("#404040");
        rectangle.setOutlineThickness(0);
        addShape(rectangle);

        rectangle = new Rectangle(headerWidth, 14);
        rectangle.setLabel("Header 3"); // or index 3 (bottom)
        rectangle.setPosition(0, 132);
        rectangle.setRotation(0);
        rectangle.setColor("#404040"); // #3b3b3b
        rectangle.setOutlineThickness(0);
        addShape(rectangle);

        rectangle = new Rectangle(headerWidth, 14);
        rectangle.setLabel("Header 4"); // or index 4 (left)
        rectangle.setPosition(-132, 0);
        rectangle.setRotation(90);
        rectangle.setColor("#404040");
        rectangle.setOutlineThickness(0);
        addShape(rectangle);

        final double contactSeparation = 6.0 * 2.54;

        headerContactPositions.add(new Vertex(new Point(-contactSeparation, 132)));
        headerContactPositions.add(new Vertex(new Point(0, 132)));
        headerContactPositions.add(new Vertex(new Point(contactSeparation, 132)));

        headerContactPositions.add(new Vertex(new Point(132, contactSeparation)));
        headerContactPositions.add(new Vertex(new Point(132, 0)));
        headerContactPositions.add(new Vertex(new Point(132, -contactSeparation)));

        headerContactPositions.add(new Vertex(new Point(contactSeparation, -132)));
        headerContactPositions.add(new Vertex(new Point(0, -132)));
        headerContactPositions.add(new Vertex(new Point(-contactSeparation, -132)));

        headerContactPositions.add(new Vertex(new Point(-132, -contactSeparation)));
        headerContactPositions.add(new Vertex(new Point(-132, 0)));
        headerContactPositions.add(new Vertex(new Point(-132, contactSeparation)));

        for (int i = 0; i < headerContactPositions.size(); i++) {
            addShape(headerContactPositions.get(i));
        }

        // Lights
        List<Point> lightPositions = new ArrayList<>();
        lightPositions.add(new Point(-20, 105));
        lightPositions.add(new Point(0, 105));
        lightPositions.add(new Point(20, 105));
        lightPositions.add(new Point(105, 20));
        lightPositions.add(new Point(105, 0));
        lightPositions.add(new Point(105, -20));
        lightPositions.add(new Point(20, -105));
        lightPositions.add(new Point(0, -105));
        lightPositions.add(new Point(-20, -105));
        lightPositions.add(new Point(-105, -20));
        lightPositions.add(new Point(-105, 0));
        lightPositions.add(new Point(-105, 20));

        List<Double> lightRotations = new ArrayList<>();
        lightRotations.add(0.0);
        lightRotations.add(0.0);
        lightRotations.add(0.0);
        lightRotations.add(90.0);
        lightRotations.add(90.0);
        lightRotations.add(90.0);
        lightRotations.add(180.0);
        lightRotations.add(180.0);
        lightRotations.add(180.0);
        lightRotations.add(270.0);
        lightRotations.add(270.0);
        lightRotations.add(270.0);

        for (int i = 0; i < lightPositions.size(); i++) {
            rectangle = new Rectangle(12, 20);
            rectangle.setPosition(lightPositions.get(i));
            rectangle.setRotation(lightRotations.get(i));
            rectangle.setCornerRadius(3.0);
            rectangle.setLabel("LED " + (i + 1));
            addShape(rectangle);
        }

        // Mounting Holes
        final double holeDiameter = 6.0 * 2.9; // 2.9 mm diameter
        final double holeRadius = holeDiameter / 2.0; // 2.9 mm diameter
        final double holeDistanceFromEdge = 125 - (6.0 * 3.5);

        List<Point> mountingHolePositions = new ArrayList<>();
        mountingHolePositions.add(new Point(-holeDistanceFromEdge, -holeDistanceFromEdge)); // TODO: make hole centers 5 mm (or so) from the edge of the PCB
        mountingHolePositions.add(new Point(holeDistanceFromEdge, -holeDistanceFromEdge));
        mountingHolePositions.add(new Point(holeDistanceFromEdge, holeDistanceFromEdge));
        mountingHolePositions.add(new Point(-holeDistanceFromEdge, holeDistanceFromEdge));

        for (int i = 0; i < mountingHolePositions.size(); i++) {
            circle = new Circle<>(holeRadius);
            circle.setPosition(mountingHolePositions.get(i));
            circle.setLabel("Mount " + (i + 1));
            circle.setColor("#ffffff");
            circle.setOutlineThickness(0);
            circle.getVisibility().setReference(getShape("Substrate").getVisibility());
            addShape(circle);
        }

        // Setup Ports
        List<Point> portCirclePositions = new ArrayList<>();
        portCirclePositions.add(new Point(-90, 200));
        portCirclePositions.add(new Point(0, 200));
        portCirclePositions.add(new Point(90, 200));
        portCirclePositions.add(new Point(200, 90));
        portCirclePositions.add(new Point(200, 0));
        portCirclePositions.add(new Point(200, -90));
        portCirclePositions.add(new Point(90, -200));
        portCirclePositions.add(new Point(0, -200));
        portCirclePositions.add(new Point(-90, -200));
        portCirclePositions.add(new Point(-200, -90));
        portCirclePositions.add(new Point(-200, 0));
        portCirclePositions.add(new Point(-200, 90));

        for (int i = 0; i < getPortable().getPorts().size(); i++) {

            // Circle
            circle = new Circle<>(getHost().getPort(i));
            circle.setLabel("Port " + (i + 1));
            circle.setPosition(portCirclePositions.get(i));
            circle.setRadius(40);
            // circle.setRotation(0);
            circle.setColor("#efefef");
            circle.setOutlineThickness(0);
            circle.setVisibility(Visibility.Value.INVISIBLE);
            addShape(circle);

            if (i < 3) {
                circle.setRotation(0);
            } else if (i < 6) {
                circle.setRotation(90);
            } else if (i < 9) {
                circle.setRotation(180);
            } else if (i < 12) {
                circle.setRotation(270);
            }

            // Line (Port Data Plot)
            /*
            Line line = new Line();
            addShape(line);
            line.setReferencePoint(circle.getPosition()); // Remove this? Weird to have a line with a center...
            line.setSource(new Point(-circle.getRadius(), 0, line.getPosition()));
            line.setTarget(new Point(circle.getRadius(), 0, line.getPosition()));
            line.setRotation(90);
            line.setOutlineColor("#ff000000");
            line.getVisibility().setReferencePoint(circle.getVisibility());
            */

            /*
            // TODO: Replace the lines with a Polyline/Plot(numPoints)/Plot(numSegments) w. source and destination and calculate paths to be equal lengths) + setData() function to map onto y axis points with most recent data
            Line previousLine = null;
            int segmentCount = 10;
            for (int j = 0; j < segmentCount; j++) {
                Line line = new Line();
                addShape(line);
                line.setReferencePoint(circle.getPosition()); // Remove this? Weird to have a line with a center...

                if (previousLine == null) {
                    line.setSource(new Point(-circle.getRadius(), 0, line.getPosition()));
                } else {
                    line.setSource(new Point(previousLine.getTarget().getX(), previousLine.getTarget().getY(), line.getPosition()));
                }
                if (j < (segmentCount - 1)) {
                    double segmentLength = (circle.getRadius() * 2) / segmentCount;
                    line.setTarget(new Point(line.getSource().getX() + segmentLength, Probability.generateRandomInteger(-(int) circle.getRadius(), (int) circle.getRadius()), line.getPosition()));

//                    Log.v("OnUpdate", "ADDING onUpdateListener");
//                    final Circle finalCircle = circle;
//                    line.setOnUpdateListener(new OnUpdateListener<Line>() {
//                        @Override
//                        public void onUpdate(Line line)
//                        {
//                            line.getTarget().setY(Probability.generateRandomInteger(-(int) finalCircle.getRadius(), (int) finalCircle.getRadius()));
//                        }
//                    });

                } else {
                    line.setTarget(new Point(circle.getRadius(), 0, line.getPosition()));
                }

                line.setRotation(90);
                line.setOutlineColor("#ff000000");
                line.setOutlineThickness(3.0);
                line.getVisibility().setReferencePoint(circle.getVisibility());

                previousLine = line;
            }
            */
        }
    }

    private void setupActions() {
        setOnActionListener(new ActionListener() {
                                @Override
                                public void onAction(Action action) {

                                    final Event event = action.getLastEvent();

                                    final Camera camera = event.getActor().getCamera();

                                    if (event.getType() == Event.Type.NONE) {

                                    } else if (event.getType() == Event.Type.SELECT) {

                                    } else if (event.getType() == Event.Type.HOLD) {

                                    } else if (event.getType() == Event.Type.MOVE) {

                                        if (action.getFirstEvent().getTargetShape() == null) {
                                            return;
                                        }

                                        if (action.getFirstEvent().getTargetShape().getLabel().equals("Substrate")) {

                                            // Holding
                                            if (action.isHolding()) {

                                                // Update position of Host image
                                                setPosition(event.getPosition());

                                                // Camera
                                                camera.setFocus(getHost());

                                            } else {

                                                // Update position of prototype Extension
                                                parentSpace.setExtensionPrototypePosition(event.getPosition());

                                                getPortShapes().setVisibility(Visibility.Value.INVISIBLE);
                                                setPathVisibility(Visibility.Value.INVISIBLE);
                                                setDockVisibility(Visibility.Value.VISIBLE);

                                                parentSpace.setExtensionPrototypeVisibility(Visibility.Value.VISIBLE);

                                            }

                                        } else if (action.getFirstEvent().getTargetShape().getLabel().startsWith("Port")) {

                                            if (action.isDragging()) {

                                                // Prototype Path Visibility
                                                parentSpace.setPathPrototypeSourcePosition(action.getFirstEvent().getTargetShape().getPosition());
                                                parentSpace.setPathPrototypeDestinationPosition(event.getPosition());
                                                parentSpace.setPathPrototypeVisibility(Visibility.Value.VISIBLE);

                                                // Prototype Extension Visibility
                                                boolean isCreateExtensionAction = true;
                                                ImageGroup imageGroup = parentSpace.getImages(Host.class, Extension.class);
                                                for (int i = 0; i < imageGroup.size(); i++) {
                                                    Image otherImage = imageGroup.get(i);

                                                    // Update style of nearby Hosts
                                                    double distanceToHostImage = Geometry.distance(
                                                            event.getPosition(),
                                                            otherImage.getPosition()
                                                    );

                                                    if (distanceToHostImage < 500) {
                                                        isCreateExtensionAction = false;
                                                        break;
                                                    }

                                                    // TODO: if distance > 800: connect to cloud service and show "cloud portable" image
                                                }

                                                if (isCreateExtensionAction) {
                                                    parentSpace.setExtensionPrototypeVisibility(Visibility.Value.VISIBLE);
                                                    parentSpace.setPathPrototypeSourcePosition(action.getFirstEvent().getTargetShape().getPosition());
                                                    parentSpace.setExtensionPrototypePosition(event.getPosition());
                                                } else {
                                                    parentSpace.setExtensionPrototypeVisibility(Visibility.Value.INVISIBLE);
                                                }

                                                // Show Ports of nearby Hosts and Extensions
                                                Port sourcePort = (Port) action.getFirstEvent().getTargetShape().getEntity();
                                                Event lastEvent = action.getLastEvent();

                                                // Show Ports of nearby Hosts and Extensions
                                                double nearbyRadiusThreshold = 200 + 60;
                                                ImageGroup nearbyPortableImages = imageGroup.filterArea(lastEvent.getPosition(), nearbyRadiusThreshold);

                                                for (int i = 0; i < imageGroup.size(); i++) {
                                                    PortableImage portableImage = (PortableImage) imageGroup.get(i);

                                                    if (portableImage.getPortable() == sourcePort.getPortable() || nearbyPortableImages.contains(portableImage)) {

//                                                        // <HACK>
                                                        PortableImage nearbyImage = portableImage;
                                                        nearbyImage.setTransparency(1.0f);
                                                        nearbyImage.getPortShapes().setVisibility(Visibility.Value.VISIBLE);

                                                        // Add additional Port to Extension if it has no more available Ports
                                                        if (portableImage.getPortable().getProfile() == null) {
                                                            if (portableImage instanceof ExtensionImage) {
                                                                Portable extensionPortable = portableImage.getPortable();

                                                                boolean addPrototypePort = true;
                                                                for (int j = 0; j < extensionPortable.getPorts().size(); j++) {
                                                                    Port existingPort = extensionPortable.getPorts().get(j);
                                                                    if (existingPort.getType() == Port.Type.NONE) {
                                                                        addPrototypePort = false;
                                                                        break;
                                                                    }
                                                                }

                                                                if (addPrototypePort) {
                                                                    Port port = new Port();
                                                                    port.setIndex(extensionPortable.getPorts().size());
                                                                    extensionPortable.addPort(port);
                                                                }
                                                            }
                                                        }

                                                        // </HACK>

                                                    } else {

                                                        PortableImage nearbyFigure = portableImage;
                                                        nearbyFigure.setTransparency(0.1f);
                                                        nearbyFigure.getPortShapes().setVisibility(Visibility.Value.INVISIBLE);

                                                    }
                                                }

                                            } else if (action.isHolding()) {

//                                                // Holding and dragging

                                            }

                                            // Camera
                                            Port sourcePort = (Port) action.getFirstEvent().getTargetShape().getEntity();
                                            camera.setFocus(sourcePort, event.getPosition());

                                        }

                                    } else if (event.getType() == Event.Type.UNSELECT) {

                                        // <HACK>
                                        // TODO: Refactor so this doesn't have to be here! It's messy this way... standardize the way "null shapes" are handled
                                        if (action.getFirstEvent().getTargetShape() == null) {
                                            return;
                                        }
                                        // </HACK>

                                        if (action.getFirstEvent().getTargetShape().getLabel().equals("Substrate")) {

                                            if (action.isTap()) {

                                                // Focus on touched form
                                                setPathVisibility(Visibility.Value.VISIBLE);
                                                getPortShapes().setVisibility(Visibility.Value.VISIBLE);
                                                //setDockVisibility(Visibility.Value.INVISIBLE);

                                                setTransparency(1.0);

                                                // Show ports and paths of touched form
                                                for (int i = 0; i < getHost().getPorts().size(); i++) {
                                                    PathGroup paths = getHost().getPort(i).getPaths();

                                                    for (int j = 0; j < paths.size(); j++) {
                                                        Path path = paths.get(j);

                                                        // Show source and target ports in path
                                                        parentSpace.getShape(path.getSource()).setVisibility(Visibility.Value.VISIBLE);
                                                        parentSpace.getShape(path.getTarget()).setVisibility(Visibility.Value.VISIBLE);

                                                        // Show path connection
                                                        parentSpace.getImage(path).setVisibility(Visibility.Value.VISIBLE);

//                                                        // Show Extensions connected to Port
//                                                        if (path.getSource().getExtension() != null) {
//                                                            Extension extension = path.getSource().getExtension();
//                                                            parentSpace.getImage(extension).setVisibility(Visibility.Value.VISIBLE);
//                                                        }
//
//                                                        if (path.getTarget().getExtension() != null) {
//                                                            Extension extension = path.getSource().getExtension();
//                                                            parentSpace.getImage(extension).setVisibility(Visibility.Value.VISIBLE);
//                                                        }
                                                    }
                                                }

                                                // Camera
                                                camera.setFocus(getHost());

                                                if (getHost().getExtensions().size() > 0) {
                                                    parentSpace.getImages(getHost().getExtensions()).setTransparency(1.0);

                                                    // <HACK>
                                                    // TODO: Replace ASAP. This is shit.
                                                    // TODO: Use "rectangle" or "circular" extension layout algorithms
                                                    setExtensionDistance(500);
                                                    // </HACK>
                                                }

                                                // Title
                                                parentSpace.setTitleText("Host");
                                                parentSpace.setTitleVisibility(Visibility.Value.VISIBLE);

                                            } else {

                                                // TODO: Release longer than tap!

                                                if (event.getTargetImage() instanceof HostImage) {

                                                    // If getFirstEvent queueAction was on the same form, then respond
                                                    if (action.getFirstEvent().isPointing() && action.getFirstEvent().getTargetImage() instanceof HostImage) {

                                                        // Host
//                                                        event.getTargetImage().queueAction(action);

                                                        // Camera
//                                                        camera.setFocus();
                                                    }

                                                } else if (event.getTargetImage() instanceof Space) {

                                                    // Host
//                                                        action.getFirstEvent().getTargetImage().queueAction(action);

                                                }

                                            }

                                            // Check if connecting to a extension
                                            if (parentSpace.getExtensionPrototypeVisibility().getValue() == Visibility.Value.VISIBLE) {

                                                parentSpace.setExtensionPrototypeVisibility(Visibility.Value.INVISIBLE);

                                                // Get cached extension profiles (and retrieve additional from Internet store)
                                                List<PortableProfile> portableProfiles = Application.getView().getClay().getPortableProfiles();


                                                if (portableProfiles.size() == 0) {

                                                    // Show "default" DIY extension builder (or info about there being no extensionAxes)

                                                } else if (portableProfiles.size() > 0) {

                                                    // Prompt User to select an Extension from the Store
                                                    // i.e., Prompt to select extension to use! Then use that profile to create and configure ports for the extension.
                                                    Application.getView().getActionPrompts().promptSelection(portableProfiles, new Prompt.OnActionListener<PortableProfile>() {
                                                        @Override
                                                        public void onComplete(PortableProfile portableProfile) {

                                                            // Add Extension from Profile
                                                            Extension extension = addExtension(portableProfile, event.getPosition());

                                                            double rangle = getRelativeAngle(event.getPosition());
                                                            Log.v("RelativeAngle", "rel. angle: " + rangle);

                                                            // Update Camera
                                                            camera.setFocus(extension);
                                                        }
                                                    });
                                                    // Application.getView().promptTasks();
                                                }
                                            }

                                        } else if (action.getFirstEvent().getTargetShape().getLabel().startsWith("Port")) {

                                            if (action.getLastEvent().getTargetShape() != null && action.getLastEvent().getTargetShape().getLabel().startsWith("Port")) {

                                                // (Host.Port, ..., Host.Port) Action Pattern

                                                if (action.getFirstEvent().getTargetShape() == action.getLastEvent().getTargetShape()) { // if (action.isTap()) {

                                                    // (Host.Port A, ..., Host.Port A) Action Pattern
                                                    // i.e., The action's first and last events address the same port. Therefore, it must be either a tap or a hold.

                                                    // Get port associated with the touched port shape
                                                    Port port = (Port) action.getFirstEvent().getTargetShape().getEntity();
                                                    int portIndex = getHost().getPorts().indexOf(port);

                                                    if (port.getExtension() == null || port.getExtension().getProfile() == null) {

                                                        if (port.getType() == Port.Type.NONE) {

                                                            Log.v("TouchPort", "A");

                                                            port.setDirection(Port.Direction.INPUT);
                                                            port.setType(Port.Type.next(port.getType()));

                                                        } else if (!port.hasForwardPath() && port.getAncestorPaths().size() == 0) {

                                                            Log.v("TouchPort", "B");

                                                            Port.Type nextType = port.getType();
                                                            while ((nextType == Port.Type.NONE) || (nextType == port.getType())) {
                                                                nextType = Port.Type.next(nextType);
                                                            }
                                                            port.setType(nextType);

                                                        } else if (!hasVisiblePaths(portIndex) && !hasVisibleAncestorPaths(portIndex)) {

                                                            Log.v("TouchPort", "C");

                                                            // Hide Ports on the non-selected Hosts
                                                            ImageGroup hostImages = parentSpace.getImages(Host.class);
                                                            for (int i = 0; i < hostImages.size(); i++) {
                                                                HostImage hostImage = (HostImage) hostImages.get(i);
                                                                hostImage.getPortShapes().setVisibility(Visibility.Value.INVISIBLE);
                                                                hostImage.setPathVisibility(Visibility.Value.INVISIBLE);
                                                                hostImage.setDockVisibility(Visibility.Value.VISIBLE);

                                                                // Make non-selected Hosts transparent
                                                                // i.e., Get shapes in image matching labels "Substrate", "Header <index>", and "LED <index>"
                                                                ShapeGroup shapes = hostImage.getShapes().filterLabel("^Substrate$", "^Header (1|2|3|4)$", "^LED (1[0-2]|[1-9])$");
                                                                shapes.setTransparency(0.1);
//                                                                hostImage.setTransparency(0.1);
                                                            }

                                                            // Hide Ports on the non-selected Extensions (like above, but for Extensions)
                                                            ImageGroup extensionImages = parentSpace.getImages().filterType(Extension.class);
                                                            for (int i = 0; i < extensionImages.size(); i++) {
                                                                ExtensionImage extensionImage = (ExtensionImage) extensionImages.get(i);
//                                                            if (extensionImage.getExtension() != getParentImage().getEntity()) {

                                                                extensionImage.setTransparency(0.1);

                                                                extensionImage.getPortShapes().setVisibility(Visibility.Value.INVISIBLE);
                                                                extensionImage.setPathVisibility(Visibility.Value.INVISIBLE);
//                                                            }
                                                            }

                                                            // Show the Port's Paths
                                                            setVisibility(Visibility.Value.VISIBLE);
                                                            setPathVisibility(port, Visibility.Value.VISIBLE);
                                                            setDockVisibility(port, Visibility.Value.INVISIBLE);

                                                            PathGroup paths = port.getPaths();
                                                            for (int i = 0; i < paths.size(); i++) {
                                                                Path path = paths.get(i);

                                                                // Show Ports
                                                                parentSpace.getShape(path.getSource()).setVisibility(Visibility.Value.VISIBLE);
                                                                parentSpace.getShape(path.getTarget()).setVisibility(Visibility.Value.VISIBLE);

                                                                // Show Path
                                                                parentSpace.getImage(path).setVisibility(Visibility.Value.VISIBLE);
                                                            }

                                                            // Show all Paths also connected to the Extension to which the touched Port is connected
                                                            if (port.getExtension() != null) {
                                                                Group<Port> siblingPorts = port.getExtension().getPorts();
                                                                for (int i = 0; i < siblingPorts.size(); i++) {
                                                                    Port siblingPort = siblingPorts.get(i);
                                                                    if (siblingPort != port) {
                                                                        PathGroup siblingPaths = siblingPort.getPaths();
                                                                        for (int j = 0; j < siblingPaths.size(); j++) {
                                                                            Path siblingPath = siblingPaths.get(j);

                                                                            // Show Ports
                                                                            parentSpace.getShape(siblingPath.getSource()).setVisibility(Visibility.Value.VISIBLE);
                                                                            parentSpace.getShape(siblingPath.getTarget()).setVisibility(Visibility.Value.VISIBLE);

                                                                            // Show Path
                                                                            parentSpace.getImage(siblingPath).setVisibility(Visibility.Value.VISIBLE);
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            // <HACK>
                                                            // TODO: Put this code in Camera
                                                            // Camera
                                                            Group<Port> pathPorts = paths.getPorts();
                                                            ShapeGroup pathPortShapes = parentSpace.getShapes().filterEntity(pathPorts);
                                                            camera.adjustScale(pathPortShapes.getBoundingBox());
                                                            camera.setPosition(pathPortShapes.getCenterPosition());
                                                            // </HACK>

                                                        } else if (hasVisiblePaths(portIndex) || hasVisibleAncestorPaths(portIndex)) {

                                                            Log.v("TouchPort", "D");

                                                            // Paths are being shown. Touching a port changes the port type. This will also
                                                            // updates the corresponding path requirement.

                                                            Port.Type nextType = port.getType();
                                                            while ((nextType == Port.Type.NONE) || (nextType == port.getType())) {
                                                                nextType = Port.Type.next(nextType);
                                                            }
                                                            port.setType(nextType);

                                                        }

                                                        parentSpace.setPathPrototypeVisibility(Visibility.Value.INVISIBLE);
                                                    }

                                                } else if (action.getFirstEvent().getTargetShape() != action.getLastEvent().getTargetShape()) {

                                                    // (Host.Port A, ..., Host.Port B) Action Pattern
                                                    // i.e., The Action's first and last Events address different Ports.

                                                    Shape sourcePortShape = event.getAction().getFirstEvent().getTargetShape();

                                                    if (action.isDragging()) {

                                                        Log.v("Events", "B");

                                                        Port sourcePort = (Port) sourcePortShape.getEntity();
                                                        Port targetPort = null;

                                                        Shape targetPortShape = parentSpace.getShapes(Port.class).remove(sourcePortShape).filterContains(event.getPosition()).get(0);
                                                        targetPort = (Port) targetPortShape.getEntity();

                                                        if (targetPort == null) {

                                                            // targetPort is null, meaning that a target Port shape was not found

                                                            Log.v("Events", "C.1");

                                                            // Update source port configuration
                                                            if (sourcePort.getDirection() == Port.Direction.NONE) {
                                                                sourcePort.setDirection(Port.Direction.INPUT);
                                                            }
                                                            if (sourcePort.getType() == Port.Type.NONE) {
                                                                sourcePort.setType(Port.Type.next(sourcePort.getType()));
                                                            }

                                                        } else {

                                                            // targetPort is not null, meaning a target Port shape was found

                                                            Log.v("Events", "C.2");

                                                            // Update source port configuration
                                                            if (sourcePort.getDirection() == Port.Direction.NONE) {
                                                                sourcePort.setDirection(Port.Direction.INPUT);
                                                            }
                                                            if (sourcePort.getType() == Port.Type.NONE) {
                                                                sourcePort.setType(Port.Type.next(sourcePort.getType())); // (machineSprite.channelTypes.getEvent(i) + 1) % machineSprite.channelTypeColors.length
                                                            }

                                                            // Update target port configuration
                                                            if (targetPort.getDirection() == Port.Direction.NONE) {
                                                                targetPort.setDirection(Port.Direction.OUTPUT);
                                                            }
                                                            if (targetPort.getType() == Port.Type.NONE) {
                                                                targetPort.setType(sourcePort.getType());
                                                            }

                                                            if (!sourcePort.hasAncestor(targetPort)) {

                                                                Log.v("Events", "D.1");

                                                                // Create and configure new Path
                                                                Path path = new Path(sourcePort, targetPort);

                                                                // Set Path type
                                                                if (sourcePort.getParent() instanceof Extension || targetPort.getParent() instanceof Extension) {
                                                                    path.setType(Path.Type.ELECTRONIC);
                                                                } else {
                                                                    path.setType(Path.Type.MESH);
                                                                }

                                                                // Set Port types
                                                                if (sourcePort.getParent() instanceof Extension) {
                                                                    Extension extension = (Extension) sourcePort.getParent();
                                                                    if (extension.getProfile() == null) {
                                                                        targetPort.setType(sourcePort.getType());
                                                                    } else {
                                                                        sourcePort.setType(targetPort.getType());
                                                                    }
                                                                } else if (targetPort.getParent() instanceof Extension) {
                                                                    Extension extension = (Extension) targetPort.getParent();
                                                                    if (extension.getProfile() == null) {
                                                                        Log.v("AddPath", "NO profile");
                                                                        targetPort.setType(sourcePort.getType());
                                                                    } else {
                                                                        Log.v("AddPath", "has profile");
                                                                        sourcePort.setType(targetPort.getType());
                                                                    }
                                                                }

                                                                sourcePort.addForwardPath(path);

                                                                parentSpace.addEntity(path);
                                                            }

                                                            // Remove focus from other forms and their ports
                                                            ImageGroup hostImages = getSpace().getImages(Host.class);
                                                            for (int i = 0; i < hostImages.size(); i++) {
                                                                HostImage hostImage = (HostImage) hostImages.get(i);
                                                                hostImage.setTransparency(0.05f);
                                                                hostImage.getPortShapes().setVisibility(Visibility.Value.INVISIBLE);
                                                                hostImage.setPathVisibility(Visibility.Value.INVISIBLE);
                                                                hostImage.setDockVisibility(Visibility.Value.VISIBLE);
                                                            }

                                                            // Show Path and all contained Ports
                                                            PathGroup paths = sourcePort.getPaths();
                                                            parentSpace.getShapes(paths.getPorts()).setVisibility(Visibility.Value.VISIBLE);
                                                            parentSpace.getImages(paths).setVisibility(Visibility.Value.VISIBLE);

                                                            // Camera
                                                            event.getActor().getCamera().setFocus(paths);
                                                        }

                                                        parentSpace.setPathPrototypeVisibility(Visibility.Value.INVISIBLE);

                                                    }

                                                }

                                            } else if (action.getLastEvent().getTargetShape() == null
                                                    // TODO: && action.getLastEvent().getTargetImage().getLabel().startsWith("Space")) {
                                                    && action.getLastEvent().getTargetImage() == parentSpace) {

                                                // (Host.Port, ..., Space) Action Pattern

                                                if (parentSpace.getExtensionPrototypeVisibility().getValue() == Visibility.Value.VISIBLE) {

                                                    Shape hostPortShape = event.getAction().getFirstEvent().getTargetShape();
                                                    Port hostPort = (Port) hostPortShape.getEntity();

                                                    // Create new Extension from scratch (for manual configuration/construction)
                                                    Extension extension = createExtension(hostPort, event.getPosition());

                                                    // Update Camera
                                                    camera.setFocus(extension);

                                                }

                                                // Update Image
                                                parentSpace.setPathPrototypeVisibility(Visibility.Value.INVISIBLE);
                                                parentSpace.setExtensionPrototypeVisibility(Visibility.Value.INVISIBLE);

                                            } else {

                                                // Get Port associated with the touched Port shape
                                                Port port = (Port) action.getFirstEvent().getTargetShape().getEntity();

                                                // Port type and flow direction
                                                if (port != null) {
                                                    // Update data model
                                                    if (port.getDirection() == Port.Direction.NONE) {
                                                        port.setDirection(Port.Direction.INPUT);
                                                    }
                                                    if (port.getType() == Port.Type.NONE) {
                                                        port.setType(Port.Type.next(port.getType()));
                                                    }
                                                }

                                                parentSpace.setPathPrototypeVisibility(Visibility.Value.INVISIBLE);
                                            }

                                        }
                                    }
                                }
                            }

        );
    }

    /**
     * Creates a new {@code Extension} connected to {@hostPort}.
     *
     * @param hostPort
     */
    private Extension createExtension(Port hostPort, Point initialPosition) {

        // TODO: Remove initialPosition... find the position by analyzing the geometry of the HostImage

        //Log.v("Extension", "Creating Extension from Port");

        //Shape hostPortShape = event.getAction().getFirstEvent().getTargetShape();
        Shape hostPortShape = getShape(hostPort);

        //Log.v("IASM", "(1) touch extension to select from store or (2) drag signal to base or (3) touch elsewhere to cancel");

        // Create the Extension
        // TODO: Extension extension = new Extension(PortableProfile); with PortableProfile without UUID?
        Extension extension = new Extension();

        // TODO: Prompt to select extension to use! Then use that profile to create and configure ports for the extension.

        // Create Ports and add them to the Extension
        int defaultPortCount = 1;
        for (int j = 0; j < defaultPortCount; j++) {
            Port port = new Port();
            port.setIndex(j);
            extension.addPort(port);
        }

        // Add Extension to Model
        parentSpace.getModel().addExtension(extension);

        // Add Extension to Space
        parentSpace.addEntity(extension);

        // Get the just-created Extension Image
        ExtensionImage extensionImage = (ExtensionImage) parentSpace.getImage(extension);
        extensionImage.setPosition(initialPosition);

        // <REFACTOR>
        // Update the Extension Image position and rotation
//        int headerIndex = getHeaderIndex(initialPosition);
//        updateExtensionLayout(extensionImage)
        // </REFACTOR>

        // Configure Host's Port (i.e., the Path's source Port)
//        Port hostPort = (Port) hostPortShape.getEntity();

        if (hostPort.getType() == Port.Type.NONE || hostPort.getDirection() == Port.Direction.NONE) {
            hostPort.setType(Port.Type.POWER_REFERENCE); // Set the default type to reference (ground)
            hostPort.setDirection(Port.Direction.BOTH);
        }

        // Configure Extension's Ports (i.e., the Path's target Port)
        Port extensionPort = extension.getPorts().get(0);
        extensionPort.setDirection(Port.Direction.INPUT);
        extensionPort.setType(hostPort.getType());

        // Create Path from Host to Extension
        Path path = new Path(hostPort, extensionPort);
        path.setType(Path.Type.ELECTRONIC);
        hostPort.addForwardPath(path);

        // Add Path to Space
        parentSpace.addEntity(path);

        // Remove focus from other Hosts and their Ports
        ImageGroup hostImages = getSpace().getImages(Host.class);
        for (int i = 0; i < hostImages.size(); i++) {
            HostImage hostImage = (HostImage) hostImages.get(i);
            hostImage.setTransparency(0.05f);
            hostImage.getPortShapes().setVisibility(Visibility.Value.INVISIBLE);
            hostImage.setPathVisibility(Visibility.Value.INVISIBLE);
            hostImage.setDockVisibility(Visibility.Value.VISIBLE);
        }

        // Show Path and all contained Ports
        PathGroup paths = hostPort.getPaths();
        parentSpace.getShapes(paths.getPorts()).setVisibility(Visibility.Value.VISIBLE);
        parentSpace.getImages(paths).setVisibility(Visibility.Value.VISIBLE);

        updateExtensionLayout();

        return extension;
    }

    private Extension addExtension(PortableProfile portableProfile, Point initialPosition) {
        // Log.v("IASM", "(1) touch extension to select from store or (2) drag signal to base or (3) touch elsewhere to cancel");

        // Create the Extension
        final Extension extension = new Extension(portableProfile);

        // Add Extension to Model
        parentSpace.getModel().addExtension(extension);

        // Add Extension to Space
        parentSpace.addEntity(extension);

        // Get the just-created Extension Image
        ExtensionImage extensionImage = (ExtensionImage) parentSpace.getImage(extension);

        // Update the Extension Image position and rotation
        extensionImage.setPosition(initialPosition);

        // <REFACTOR>
        // Update the Extension Image position and rotation
//        int headerIndex = getHeaderIndex(initialPosition);
//        extensionImage.adjustPgetHeaderIndeosition();
        // </REFACTOR>

        // Automatically select, connect paths to, and configure the Host's Ports
        for (int i = 0; i < portableProfile.getPorts().size(); i++) {

            // Select an available Host Port
            Port selectedHostPort = null;
            double distanceToSelectedPort = Double.MAX_VALUE;
            for (int j = 0; j < getHost().getPorts().size(); j++) {
                if (getHost().getPorts().get(j).getType() == Port.Type.NONE) {

                    double distanceToPort = Geometry.distance(
                            getPortShapes().filterEntity(getHost().getPorts().get(j)).get(0).getPosition(),
                            extensionImage.getPosition()
                    );

                    // Check if the port is the nearest
                    if (distanceToPort < distanceToSelectedPort) {
                        selectedHostPort = getHost().getPorts().get(j);
                        distanceToSelectedPort = distanceToPort;
                    }
                }
            }
            // TODO: selectedHostPort = (Port) getPortShapes().getNearestImage(extensionImage.getPosition()).getEntity();

            // Configure Host's Port
            selectedHostPort.setType(portableProfile.getPorts().get(i).getType());
            selectedHostPort.setDirection(portableProfile.getPorts().get(i).getDirection());

            // Create Path from Extension Port to Host Port
            Path path = new Path(selectedHostPort, extension.getPorts().get(i));
            path.setType(Path.Type.ELECTRONIC);

            selectedHostPort.addForwardPath(path);

            parentSpace.addEntity(path);
        }

        updateExtensionLayout();

        return extension;
    }

    // TODO: Remove this?
    public int getHeaderIndex(ExtensionImage extensionImage) {

        int[] indexCounts = new int[4];
        for (int i = 0; i < indexCounts.length; i++) {
            indexCounts[i] = 0;
        }

        Shape boardShape = getShape("Substrate");
        int segmentIndex = -1;
        List<Line> hostShapeSegments = boardShape.getSegments();

        PortGroup extensionPorts = extensionImage.getExtension().getPorts();
        for (int j = 0; j < extensionPorts.size(); j++) {

            Port extensionPort = extensionPorts.get(j);

            if (extensionPort == null) {
                continue;
            }

            if (extensionPort.getPaths().size() == 0 || extensionPort.getPaths().get(0) == null) {
                continue;
            }

            Port hostPort = extensionPort.getPaths().get(0).getHostPort(); // HACK b/c using index 0

            Log.v("Counts", "host port: " + hostPort);

            Point hostPortPosition = parentSpace.getShape(hostPort).getPosition();


            double minDistance = Double.MAX_VALUE;
            int nearestSegmentIndex = 0;
            for (int i = 0; i < hostShapeSegments.size(); i++) {

                Line segment = hostShapeSegments.get(i);
                Point segmentMidpoint = segment.getMidpoint();

                double distance = Geometry.distance(hostPortPosition, segmentMidpoint);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestSegmentIndex = i;
                }
            }

            indexCounts[nearestSegmentIndex]++;
        }

        // Get the segment with the most counts
        segmentIndex = 0;
        for (int i = 0; i < indexCounts.length; i++) {
            Log.v("Counts", "segment count " + i + ": " + indexCounts[i]);
            if (indexCounts[i] > indexCounts[segmentIndex]) {
                segmentIndex = i;
            }
        }

        return segmentIndex;
    }

//    public double distanceToExtensions = 500;
//
//    // TODO: Refactor this... it's really dumb right now.
//    private void updateExtensionLayout(ExtensionImage extensionImage, int segmentIndex) {
//
//        // <REFACTOR>
//        // Update the Extension Image position and rotation
//        //extensionImage.setPosition(event.getPosition());
//        if (segmentIndex == 0) {
//            extensionImage.getPosition().setReferencePoint(getPosition());
//            extensionImage.getPosition().setX(0);
//            extensionImage.getPosition().setY(-distanceToExtensions);
//        } else if (segmentIndex == 1) {
//            extensionImage.getPosition().setReferencePoint(getPosition());
//            extensionImage.getPosition().setX(distanceToExtensions);
//            extensionImage.getPosition().setY(0);
//        } else if (segmentIndex == 2) {
//            extensionImage.getPosition().setReferencePoint(getPosition());
//            extensionImage.getPosition().setX(0);
//            extensionImage.getPosition().setY(distanceToExtensions);
//        } else if (segmentIndex == 3) {
//            extensionImage.getPosition().setReferencePoint(getPosition());
//            extensionImage.getPosition().setX(-distanceToExtensions);
//            extensionImage.getPosition().setY(0);
//        }
//
//        //double extensionImageRotation = Geometry.getAngle(hostPortShape.getPosition(), extensionImage.getPosition());
//        if (segmentIndex == 0) {
//            extensionImage.setRotation(0);
//        } else if (segmentIndex == 1) {
//            extensionImage.setRotation(90);
//        } else if (segmentIndex == 2) {
//            extensionImage.setRotation(180);
//        } else if (segmentIndex == 3) {
//            extensionImage.setRotation(270);
//        }
//        // </REFACTOR>
//    }

    public Host getHost() {
        return (Host) getEntity();
    }

    ShapeGroup lightShapeGroup = null;

    public void update() {

        // Get LED shapes
        if (lightShapeGroup == null) {
            lightShapeGroup = getShapes().filterLabel("^LED (1[0-2]|[1-9])$");
        }

        // Update Port and LED shape styles
        for (int i = 0; i < getEntity().getPorts().size(); i++) {
            Port port = getEntity().getPorts().get(i);
            Shape portShape = getShape(port);

            // Update color of Port shape based on type
            portShape.setColor(camp.computer.clay.util.Color.getColor(port.getType()));

            // Update color of LED based on corresponding Port's type
            lightShapeGroup.get(i).setColor(portShape.getColor());
        }

        super.update();
    }

    protected double distanceToExtensions = 500;

    public void setExtensionDistance(double distance) {
        distanceToExtensions = distance;
        updateExtensionLayout();
    }

    public void updateExtensionLayout() {

        Group<Extension> extensions = getHost().getExtensions();
//        Log.v("ExtensionCount", "extensions: " + extensions.size());

        // Reset current layout
        for (int i = 0; i < extensionAxes.size(); i++) {
            extensionAxes.get(i).clear();
        }

        // Update each Extension's position
        for (int i = 0; i < extensions.size(); i++) {
            Extension extension = extensions.get(i);
            updateExtensionSegmentIndex(extension);
        }

        for (int segmentIndex = 0; segmentIndex < extensionAxes.size(); segmentIndex++) {

            for (int extensionIndex = 0; extensionIndex < extensionAxes.get(segmentIndex).size(); extensionIndex++) {

                Extension extension = extensionAxes.get(segmentIndex).get(extensionIndex);
                ExtensionImage extensionImage = (ExtensionImage) parentSpace.getImage(extension);

                if (extensionImage == null) {
                    continue;
                }

//            extensionImage.adjustPosition();

                final double extensionSeparationDistance = 25.0;
                double extensionWidth = 200;
                int extensionCount = extensionAxes.get(segmentIndex).size();
//                Log.v("ExtensionCount", "extension count: " + extensionCount);
                double offset = extensionIndex * 250 - (((extensionCount - 1) * (extensionWidth + extensionSeparationDistance)) / 2.0);

                // <REFACTOR>
                // Update the Extension Image position and rotation
                if (segmentIndex == 0) {
                    extensionImage.getPosition().x = 0 + offset;
                    extensionImage.getPosition().y = -distanceToExtensions;
                } else if (segmentIndex == 1) {
                    extensionImage.getPosition().x = distanceToExtensions;
                    extensionImage.getPosition().y = 0 + offset;
                } else if (segmentIndex == 2) {
                    extensionImage.getPosition().x = 0 + offset;
                    extensionImage.getPosition().y = distanceToExtensions;
                } else if (segmentIndex == 3) {
                    extensionImage.getPosition().x = -distanceToExtensions;
                    extensionImage.getPosition().y = 0 + offset;
                }

                //double extensionImageRotation = Geometry.getAngle(hostPortShape.getPosition(), extensionImage.getPosition());
                if (segmentIndex == 0) {
                    extensionImage.setRotation(0);
                } else if (segmentIndex == 1) {
                    extensionImage.setRotation(90);
                } else if (segmentIndex == 2) {
                    extensionImage.setRotation(180);
                } else if (segmentIndex == 3) {
                    extensionImage.setRotation(270);
                }
                // </REFACTOR>
            }
        }
    }

    // TODO: Refactor this... it's really dumb right now.
    public void updateExtensionSegmentIndex(Extension extension) {

        ExtensionImage extensionImage = (ExtensionImage) parentSpace.getImage(extension);

        if (extensionImage == null || extension.getHost().size() == 0) {
            return;
        }

        int segmentIndex = getHeaderIndex(extensionImage);

        extensionAxes.get(segmentIndex).add(extension);
    }

    public void draw(Display display) {
        if (isVisible()) {

            Canvas canvas = display.canvas;

            canvas.save();

//            canvas.translate(
//                    (float) position.x,
//                    (float) position.y
//            );
//
//            /*
//            // <HACK>
//            display.paint.setStyle(Paint.Style.FILL);
//            display.paint.setColor(Color.GREEN);
//            display.paint.setStrokeWidth(2.0f);
//            display.canvas.drawLine(
//                    (float) 0,
//                    (float) 0,
//                    (float) 250,
//                    (float) 0,
//                    display.paint
//            );
//
//            display.canvas.drawText(
//                    "0°",
//                    260,
//                    13,
//                    display.paint
//            );
//
//            display.paint.setStyle(Paint.Style.STROKE);
//            display.canvas.drawArc(
//                    -250,
//                    -250,
//                    250,
//                    250,
//                    0,
//                    (float) position.rotation,
//                    false,
//                    display.paint
//            );
//            // </HACK>
//            */
//
//            canvas.rotate((float) position.rotation);

            // Color
            for (int i = 0; i < shapes.size(); i++) {
                shapes.get(i).draw(display);
            }

            /*
            // <HACK>
            display.paint.setStyle(Paint.Style.FILL);
            display.paint.setColor(Color.RED);
            display.paint.setStrokeWidth(2.0f);
            display.canvas.drawLine(
                    (float) 0,
                    (float) 0,
                    (float) 250,
                    (float) 0,
                    display.paint
            );

            display.canvas.drawText(
                    "0°",
                    260,
                    13,
                    display.paint
            );
            // </HACK>
            */

//            // Labels
//            if (Application.ENABLE_GEOMETRY_LABELS) {
//                display.getPaint().setColor(Color.GREEN);
//                display.getPaint().setStyle(Paint.Style.STROKE);
//                Rectangle boardShape = (Rectangle) getShape("Substrate");
//                display.drawCircle(getPosition(), boardShape.getWidth(), 0);
//                display.drawCircle(getPosition(), boardShape.getWidth() / 2.0f, 0);
//            }

            canvas.restore();
        }
    }
}

